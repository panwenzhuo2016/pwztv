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
    // 验证业务正文含截断标记仍不超过一百字。
    @Test public void businessMessagesHaveOneHundredCharacterLimit() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (int length : List.of(99, 100, 101, 10_000)) {
            // 记录脱敏日志并限制单条与总容量。
            buffer.append("search", "中".repeat(length));
        }
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        for (int i = 0; i < saved.size(); i++) {
            String body = saved.get(i).getAsJsonObject().get("text").getAsString().substring("search: ".length());
            assertEquals(i == 0 ? 99 : 100, body.length());
            assertEquals(i >= 2, body.endsWith(" [truncated]"));
        }
    }

    // 验证先脱敏再摘要且不截断表情字符。
    @Test public void businessSummaryDoesNotExposePartialCredentials() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("search", "https://user:" + "private-password".repeat(20) + "@example.org/video?token=secret");
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("search", "x".repeat(87) + "😀".repeat(20));
        // 将有限日志编码为可恢复的本地快照。
        String saved = buffer.encode();
        assertFalse(saved.contains("private"));
        assertFalse(saved.contains("token=secret"));
        JsonArray entries = JsonParser.parseString(saved).getAsJsonArray();
        String text = entries.get(1).getAsJsonObject().get("text").getAsString();
        assertEquals("search: " + "x".repeat(87) + " [truncated]", text);
    }

    // 验证超过四千字的异常及根因重启后仍完整。
    @Test public void preservesLongExceptionAcrossRestart() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String error = "java.lang.IllegalStateException: failed\n"
                + "\tat parser.resolve(Parser.java:42)\n".repeat(180)
                + "Caused by: org.json.JSONException: root cause at end";
        // 仅屏幕异常保留详情，后台正文最多百字。
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

    // 验证旧快照过滤业务并将后台异常限百字。
    @Test public void migratesOldBusinessAndExceptionEntries() {
        JsonArray old = new JsonArray();
        String error = "SpiderDebug: java.lang.Exception: failed\n\tat parser.run(Parser.java:1)\n" + "x".repeat(5000);
        for (String text : List.of("search: " + "业务".repeat(2000), error)) {
            JsonObject item = new JsonObject();
            item.addProperty("time", 10_000_000);
            item.addProperty("text", text);
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
        assertTrue(stored.substring("SpiderDebug: ".length()).length() <= RecentLogBuffer.MAX_BACKGROUND_CHARS);
        assertEquals("SpiderDebug: java.lang.Exception: failed", stored);
        assertFalse(saved.get(0).getAsJsonObject().get("visible").getAsBoolean());
        assertTrue(saved.get(0).getAsJsonObject().get("error").getAsBoolean());
    }

    // 验证刚好一小时的日志保留且超时后移除。
    @Test public void expiresOnlyEntriesOutsideTheHour() {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(now::get);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("parse", "key=dytt first failure");
        now.addAndGet(RecentLogBuffer.RETENTION_MS);
        // 导出按时间排列的最近一小时诊断文本。
        assertTrue(buffer.export().contains("first failure"));
        now.incrementAndGet();
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("player", "key=dytt recovered");
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("first failure"));
        assertTrue(report.contains("recovered"));
        // 将有限日志编码为可恢复的本地快照。
        assertFalse(buffer.encode().contains("first failure"));
    }

    // 验证容量淘汰最早日志且保留最新错误。
    @Test public void boundsLargeAndFrequentLogs() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (int i = 0; i < 3000; i++) {
            // 记录脱敏日志并限制单条与总容量。
            buffer.append("search", "event=" + i + " " + "x".repeat(10_000));
        }
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("event=0 "));
        assertTrue(report.contains("event=2999 "));
        assertTrue(report.contains("[truncated]"));
        assertTrue(report.length() < RecentLogBuffer.MAX_CHARS + 2048 * 25);
    }

    // 验证导出与落盘快照均遮蔽常见凭据。
    @Test public void redactsCredentialsWithoutLosingSourceIdentity() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("player", "key=dytt url=https://user:pass@example.org/v.m3u8?token=private#secret\n"
                + "headers={Authorization=Bearer access, Cookie=session-data}\npassword=hidden\n{\"token\":\"json-secret\"}", true);
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
        original.append("parse", "key=dytt JSONException\n  at parser.resolve(Parser.java:42)");
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
        buffer.append("notice", "key=dytt visible error");
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
                        buffer.append("search", "worker=" + worker + ",event=" + j + ";");
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
