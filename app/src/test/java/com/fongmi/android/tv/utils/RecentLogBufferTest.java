package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class RecentLogBufferTest {
    // 验证一小时内重复异常只留一份且重启可反查。
    @Test public void deduplicatesAcrossMinutesAndExpiresByLastOccurrence() {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(now::get);
        String error = "java.lang.IllegalStateException: repeated\n\tat app.parse(Parser.java:42)";
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("parse", error, true, true);
        now.addAndGet(50 * 60_000L);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("parse", error, true, true);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("parse", "java.lang.NullPointerException: distinct error", true, true);
        now.addAndGet(20 * 60_000L);
        // 将有限日志编码为可恢复的本地快照。
        String snapshot = buffer.encode();
        JsonArray saved = JsonParser.parseString(snapshot).getAsJsonArray();
        assertEquals(2, saved.size());
        assertEquals(2, saved.get(0).getAsJsonObject().get("count").getAsInt());
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(now::get);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(snapshot);
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
        now.addAndGet(40 * 60_000L + 1);
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals("", restored.export());
    }

    // 验证各种长度业务消息都不会写入诊断日志。
    @Test public void discardsBusinessMessagesRegardlessOfLength() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (int length : List.of(99, 100, 101, 10_000)) {
            // 记录脱敏日志并限制单条与总容量。
            buffer.append("search", "中".repeat(length));
        }
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(0, saved.size());
    }

    // 验证超大异常受容量限制且不截断表情字符。
    @Test public void boundsOversizedErrorsWithoutSplittingSurrogates() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("search", "java.lang.IllegalStateException: https://user:" + "private-password".repeat(20) + "@example.org/video?token=secret", true, true);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("search", "java.lang.IllegalStateException: " + "😀".repeat(RecentLogBuffer.MAX_CHARS), true, true);
        // 将有限日志编码为可恢复的本地快照。
        String saved = buffer.encode();
        assertFalse(saved.contains("private"));
        assertFalse(saved.contains("token=secret"));
        JsonArray entries = JsonParser.parseString(saved).getAsJsonArray();
        assertEquals(1, entries.size());
        String text = entries.get(0).getAsJsonObject().get("text").getAsString();
        assertTrue(text.endsWith(" [truncated]"));
        assertTrue(text.length() <= RecentLogBuffer.MAX_CHARS);
        int end = text.indexOf(" [truncated]");
        assertFalse(Character.isHighSurrogate(text.charAt(end - 1)));
    }

    // 验证超过四千字的异常及根因重启后仍完整。
    @Test public void preservesLongExceptionAcrossRestart() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String error = "java.lang.IllegalStateException: failed\n"
                + "\tat parser.resolve(Parser.java:42)\n".repeat(180)
                + "Caused by: org.json.JSONException: root cause at end";
        // 仅记录有效异常并保留脱敏后的完整堆栈。
        buffer.append("player", error, true, true);
        // 导出按时间排列的最近一小时诊断文本。
        assertTrue(buffer.export().contains(error));
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(buffer.encode());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
        assertFalse(restored.export().contains("[truncated]"));
    }

    // 验证旧快照过滤业务并保留完整程序异常。
    @Test public void migratesOldBusinessAndExceptionEntries() {
        JsonArray old = new JsonArray();
        String error = "SpiderDebug: java.lang.Exception: failed\n\tat parser.run(Parser.java:1)\n" + "x".repeat(5000);
        for (String text : List.of("search: " + "业务".repeat(2000), error)) {
            JsonObject item = new JsonObject();
            item.addProperty("time", 10_000_000);
            item.addProperty("text", text);
            item.addProperty("visible", text.equals(error));
            old.add(item);
        }
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        buffer.restore(old.toString());
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(1, saved.size());
        String stored = saved.get(0).getAsJsonObject().get("text").getAsString();
        assertTrue(stored.startsWith("SpiderDebug: java.lang.Exception: failed"));
        assertEquals(error, stored);
        assertTrue(saved.get(0).getAsJsonObject().get("visible").getAsBoolean());
        assertTrue(saved.get(0).getAsJsonObject().get("error").getAsBoolean());
    }

    // 验证刚好一小时的日志保留且超时后移除。
    @Test public void expiresOnlyEntriesOutsideTheHour() {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(now::get);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("parse", "java.lang.IllegalStateException: key=dytt first failure", true, true);
        now.addAndGet(RecentLogBuffer.RETENTION_MS);
        // 导出按时间排列的最近一小时诊断文本。
        assertTrue(buffer.export().contains("first failure"));
        now.incrementAndGet();
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("player", "java.lang.IllegalStateException: key=dytt second failure", true, true);
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("first failure"));
        assertTrue(report.contains("second failure"));
        // 将有限日志编码为可恢复的本地快照。
        assertFalse(buffer.encode().contains("first failure"));
    }

    // 验证容量淘汰最早日志且保留最新错误。
    @Test public void boundsLargeAndFrequentLogs() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (int i = 0; i < 3000; i++) {
            // 记录脱敏日志并限制单条与总容量。
            buffer.append("search", "java.lang.IllegalStateException: event=" + i + " " + "x".repeat(10_000), true, true);
        }
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("event=0 "));
        assertTrue(report.contains("event=2999 "));
        assertFalse(report.contains("[truncated]"));
        assertTrue(report.length() < RecentLogBuffer.MAX_CHARS + 2048 * 25);
    }

    // 验证导出与落盘快照均遮蔽常见凭据。
    @Test public void redactsCredentialsWithoutLosingSourceIdentity() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("player", "java.lang.IllegalStateException: key=dytt url=https://user:pass@example.org/v.m3u8?token=private#secret\n"
                + "headers={Authorization=Bearer access, Cookie=session-data}\npassword=hidden\n{\"token\":\"json-secret\"}", true, true);
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertTrue(report.contains("key=dytt"));
        assertTrue(report.contains("example.org/v.m3u8"));
        // 将有限日志编码为可恢复的本地快照。
        String saved = buffer.encode();
        for (String secret : List.of("user:pass", "token=private", "#secret", "Bearer access", "session-data", "hidden", "json-secret")) {
            assertFalse(secret, report.contains(secret));
            assertFalse(secret, saved.contains(secret));
        }
    }

    // 验证落盘后重建能反查日志并剔除过期数据。
    @Test public void restoresRecentSnapshotAfterRestart() throws Exception {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer original = new RecentLogBuffer(now::get);
        // 记录脱敏日志并限制单条与总容量。
        original.append("parse", "org.json.JSONException: key=dytt\n  at parser.resolve(Parser.java:42)", true, true);
        Path file = Files.createTempFile("diagnostic-log-", ".json");
        try {
            // 将有限日志编码为可恢复的本地快照。
            Files.writeString(file, original.encode(), StandardCharsets.UTF_8);
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(now::get);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(Files.readString(file, StandardCharsets.UTF_8));
            // 导出按时间排列的最近一小时诊断文本。
            assertTrue(restored.export().contains("parser.resolve(Parser.java:42)"));
            now.addAndGet(RecentLogBuffer.RETENTION_MS + 1);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(Files.readString(file, StandardCharsets.UTF_8));
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals("", restored.export());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    // 验证损坏快照不会清空仍可导出的内存日志。
    @Test public void invalidSnapshotDoesNotReplaceCurrentLogs() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("notice", "java.lang.IllegalStateException: key=dytt visible error", true, true);
        assertThrows(RuntimeException.class, () -> {
            // 恢复仍在一小时范围内的日志快照。
            buffer.restore("not-json");
        });
        // 导出按时间排列的最近一小时诊断文本。
        assertTrue(buffer.export().contains("visible error"));
    }

    // 验证后台并发写日志时不会丢失小容量事件。
    @Test public void concurrentWritersRemainReadable() throws Exception {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> jobs = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int worker = i;
                jobs.add(pool.submit(() -> {
                    for (int j = 0; j < 100; j++) {
                        // 记录脱敏日志并限制单条与总容量。
                        buffer.append("search", "java.lang.IllegalStateException: worker=" + worker + ",event=" + j + ";", true, true);
                    }
                }));
            }
            for (Future<?> job : jobs) job.get();
            // 导出按时间排列的最近一小时诊断文本。
            String report = buffer.export();
            for (int i = 0; i < 4; i++) for (int j = 0; j < 100; j++) assertTrue(report.contains("worker=" + i + ",event=" + j + ";"));
        } finally {
            pool.shutdownNow();
        }
    }
}
