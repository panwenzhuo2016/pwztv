package com.fongmi.android.tv.utils;

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
        for (int i = 0; i < 100; i++) {
            // 记录脱敏日志并限制单条与总容量。
            buffer.append("search", "event=" + i + " " + "x".repeat(10_000));
        }
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("event=0 "));
        assertTrue(report.contains("event=99 "));
        assertTrue(report.contains("[truncated]"));
        assertTrue(report.length() < RecentLogBuffer.MAX_CHARS + 2048 * 25);
    }

    // 验证导出与落盘快照均遮蔽常见凭据。
    @Test public void redactsCredentialsWithoutLosingSourceIdentity() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        buffer.append("player", "key=dytt url=https://user:pass@example.org/v.m3u8?token=private#secret\n"
                + "headers={Authorization=Bearer access, Cookie=session-data}\npassword=hidden\n{\"token\":\"json-secret\"}");
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
