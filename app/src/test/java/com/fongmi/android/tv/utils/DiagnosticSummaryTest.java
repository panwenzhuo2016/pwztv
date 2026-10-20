package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.junit.Test;
import android.util.Log;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DiagnosticSummaryTest {
    // 验证诊断入口拒绝调试日志并保留警告异常。
    @Test public void adapterAcceptsOnlyWarningsAndErrors() {
        DiagnosticLog adapter = mock(DiagnosticLog.class, CALLS_REAL_METHODS);
        // 过滤业务调试日志仅接收警告和异常。
        assertFalse(adapter.isLoggable(Log.DEBUG, "search"));
        // 过滤业务调试日志仅接收警告和异常。
        assertFalse(adapter.isLoggable(Log.INFO, "home"));
        // 过滤业务调试日志仅接收警告和异常。
        assertTrue(adapter.isLoggable(Log.WARN, "parse"));
        // 过滤业务调试日志仅接收警告和异常。
        assertTrue(adapter.isLoggable(Log.ERROR, "player"));
    }

    // 验证业务正文和成功提示不进入诊断日志。
    @Test public void filtersBusinessMessagesAndNormalCancellation() {
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("search", "{\"list\":[{\"vod_name\":\"test\"}]}", false));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("home", "key=sony,fallback=empty", false));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("notice", "已复制", false));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("parse", "java.util.concurrent.CancellationException\n\tat parser.run(Parser.java:1)", true));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("parse", "java.lang.InterruptedException\n\tat parser.run(Parser.java:1)", true));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("parse", "java.io.IOException: Canceled\n\tat parser.run(Parser.java:1)", true));
        // 仅保留异常并排除正常取消与业务数据。
        assertFalse(RecentLogBuffer.useful("parse", "java.util.concurrent.ExecutionException: failed\nCaused by: java.io.IOException: Canceled\n\tat parser.run(Parser.java:1)", true));
        // 仅保留异常并排除正常取消与业务数据。
        assertTrue(RecentLogBuffer.useful("notice", "Value https of type java.lang.String cannot be converted to JSONObject", false));
        // 仅保留异常并排除正常取消与业务数据。
        assertTrue(RecentLogBuffer.useful("home", "result=unavailable,failures={timeout=1}", true));
        // 仅保留异常并排除正常取消与业务数据。
        assertTrue(RecentLogBuffer.useful("parse", "java.net.SocketTimeoutException: timeout", true));
    }

    // 验证重复异常合并计数且不同域名不会误合并。
    @Test public void mergesRepeatedErrorsWithoutMergingDistinctHosts() {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(now::get);
        for (int i = 0; i < 30; i++) {
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("home", "java.net.UnknownHostException: video.example", true);
            now.addAndGet(1000);
        }
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("home", "java.net.UnknownHostException: other.example", true);
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(2, saved.size());
        assertEquals(30, saved.get(0).getAsJsonObject().get("count").getAsInt());
        // 导出按时间排列的最近一小时诊断文本。
        assertTrue(buffer.export().contains("[count=30,last="));
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(now::get);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(buffer.encode());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
        now.addAndGet(TimeUnit.MINUTES.toMillis(1));
        // 区分业务摘要与异常并保留诊断信息。
        restored.append("home", "java.net.UnknownHostException: video.example", true);
        // 将有限日志编码为可恢复的本地快照。
        assertEquals(3, JsonParser.parseString(restored.encode()).getAsJsonArray().size());
        now.addAndGet(RecentLogBuffer.RETENTION_MS + 1);
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals("", restored.export());
    }

    // 验证旧快照里的业务刷屏升级后不再出现。
    @Test public void removesOldNoisyEntriesWhileRetainingRealFailures() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer old = new RecentLogBuffer(() -> 10_000_000);
        // 记录脱敏日志并限制单条与总容量。
        old.append("notice", "已复制");
        // 记录脱敏日志并限制单条与总容量。
        old.append("home", "key=sony,fallback=UnknownHostException");
        // 区分业务摘要与异常并保留诊断信息。
        old.append("parse", "java.lang.IllegalStateException: actual failure\n\tat parser.run(Parser.java:42)", true);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(old.encode());
        // 导出按时间排列的最近一小时诊断文本。
        String report = restored.export();
        assertFalse(report.contains("已复制"));
        assertFalse(report.contains("key=sony"));
        assertTrue(report.contains("actual failure"));
    }
}
