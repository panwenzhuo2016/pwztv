package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VisibleDiagnosticTest {
    // 验证后台程序异常一律限百字并保留重复计数。
    @Test public void limitsEveryBackgroundErrorAndRestoresShortRecords() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String error = "java.lang.ArrayIndexOutOfBoundsException: length=1; index=1\n"
                + "\tat plugin.search(Plugin.java:42)\n".repeat(100);
        for (int i = 0; i < 3; i++) {
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("SpiderDebug", error, true);
        }
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(1, saved.size());
        JsonObject record = saved.get(0).getAsJsonObject();
        assertEquals(3, record.get("count").getAsInt());
        assertFalse(record.get("visible").getAsBoolean());
        assertTrue(record.get("text").getAsString().substring("SpiderDebug: ".length()).length() <= 100);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(buffer.encode());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
    }

    // 验证相同文本的屏幕异常不与后台日志合并。
    @Test public void keepsScreenAndBackgroundVisibilitySeparate() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String error = "org.json.JSONException: empty response";
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("error", error, true);
        // 仅屏幕异常保留详情，后台正文最多百字。
        buffer.append("error", error, true, true);
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(2, saved.size());
        assertFalse(saved.get(0).getAsJsonObject().get("visible").getAsBoolean());
        assertTrue(saved.get(1).getAsJsonObject().get("visible").getAsBoolean());
    }

    // 验证真实显示后才保留原始异常与显示调用链。
    @Test public void retainsFullDetailsOnlyAfterScreenReportsError() throws Exception {
        DiagnosticLog adapter = mock(DiagnosticLog.class);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        Field current = DiagnosticLog.class.getDeclaredField("instance");
        Field contents = DiagnosticLog.class.getDeclaredField("buffer");
        Field pending = DiagnosticLog.class.getDeclaredField("screenErrors");
        current.setAccessible(true);
        contents.setAccessible(true);
        pending.setAccessible(true);
        Object previous = current.get(null);
        contents.set(adapter, buffer);
        // 注入时钟以限制待显示异常的保留时间。
        pending.set(adapter, new ScreenErrorContext(() -> 10_000_000));
        current.set(null, adapter);
        try {
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("notice", "已复制", false);
            // 将有限日志编码为可恢复的本地快照。
            assertEquals(0, JsonParser.parseString(buffer.encode()).getAsJsonArray().size());
            String message = "读取配置失败";
            IllegalStateException error = new IllegalStateException("root failure " + "x".repeat(200));
            // 先记后台摘要，短暂保存待显示的原始异常。
            DiagnosticLog.prepareVisible(message, error);
            // 将有限日志编码为可恢复的本地快照。
            JsonArray before = JsonParser.parseString(buffer.encode()).getAsJsonArray();
            assertEquals(1, before.size());
            assertFalse(before.get(0).getAsJsonObject().get("visible").getAsBoolean());
            assertTrue(before.get(0).getAsJsonObject().get("text").getAsString().substring("error: ".length()).length() <= 100);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("notice", message, false);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("vod-screen", "播放失败", true);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("notice", "Value https of type java.lang.String cannot be converted to JSONObject", false);
            // 将有限日志编码为可恢复的本地快照。
            JsonArray after = JsonParser.parseString(buffer.encode()).getAsJsonArray();
            assertEquals(4, after.size());
            String original = after.get(1).getAsJsonObject().get("text").getAsString();
            assertTrue(after.get(1).getAsJsonObject().get("visible").getAsBoolean());
            assertTrue(original.contains(error.getMessage()));
            assertTrue(original.contains("VisibleDiagnosticTest.java:"));
            assertTrue(original.contains("Screen display call site"));
            assertTrue(after.get(2).getAsJsonObject().get("text").getAsString().contains("播放失败"));
            assertTrue(after.get(3).getAsJsonObject().get("text").getAsString().contains("cannot be converted to JSONObject"));
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(buffer.encode());
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals(buffer.export(), restored.export());
        } finally {
            current.set(null, previous);
        }
    }
}
