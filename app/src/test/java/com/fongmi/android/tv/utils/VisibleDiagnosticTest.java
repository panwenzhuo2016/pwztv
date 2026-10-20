package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.fongmi.android.tv.event.ErrorEvent;

import androidx.media3.common.PlaybackException;

import org.greenrobot.eventbus.EventBus;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VisibleDiagnosticTest {
    // 验证同文案事件原始异常隔离且仅显示后持久化。
    @Test public void carriesPlaybackCausesWithoutMixingIdenticalScreenMessages() throws Exception {
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
        EventBus bus = mock(EventBus.class);
        try (MockedStatic<EventBus> buses = mockStatic(EventBus.class);
             MockedStatic<android.os.SystemClock> clocks = mockStatic(android.os.SystemClock.class)) {
            buses.when(EventBus::getDefault).thenReturn(bus);
            PlaybackException first = new PlaybackException("Source error",
                    new java.io.FileNotFoundException("first manifest " + "x".repeat(200)), 3004);
            PlaybackException second = new PlaybackException("Source error",
                    new java.io.IOException("second segment rejected"), 3004);
            // 传递屏幕提示及其原始异常。
            ErrorEvent.extract("vod", "HTTP 404", first);
            // 传递屏幕提示及其原始异常。
            ErrorEvent.extract("live", "HTTP 404", second);
            ArgumentCaptor<Object> events = ArgumentCaptor.forClass(Object.class);
            verify(bus, times(2)).post(events.capture());
            ErrorEvent vod = (ErrorEvent) events.getAllValues().get(0);
            ErrorEvent live = (ErrorEvent) events.getAllValues().get(1);
            // 获取当前事件携带的原始异常。
            assertSame(first, vod.getError());
            // 获取当前事件携带的原始异常。
            assertSame(second, live.getError());
            assertEquals("vod", vod.getTag());
            assertEquals("live", live.getTag());
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals("", buffer.export());
            // 原始异常仅在对应错误实际显示后写入日志。
            DiagnosticLog.prepareVisible("HTTP 404", new IllegalStateException("unrelated pending error"));
            // 获取当前事件携带的原始异常。
            Throwable vodCause = vod.getError();
            // 优先记录事件原始异常并兼容插件文字匹配。
            DiagnosticLog.visible("vod-screen", vod.getMsg(), true, vodCause);
            // 获取当前事件携带的原始异常。
            Throwable liveCause = live.getError();
            for (int repeat = 0; repeat < 2; repeat++) {
                // 优先记录事件原始异常并兼容插件文字匹配。
                DiagnosticLog.visible("live-screen", live.getMsg(), true, liveCause);
            }
            // 将有限日志编码为可恢复的本地快照。
            String snapshot = buffer.encode();
            JsonArray saved = JsonParser.parseString(snapshot).getAsJsonArray();
            assertEquals(2, saved.size());
            String vodText = saved.get(0).getAsJsonObject().get("text").getAsString();
            String liveText = saved.get(1).getAsJsonObject().get("text").getAsString();
            assertTrue(vodText.contains(first.getCause().getMessage()));
            assertTrue(vodText.contains("Caused by: java.io.FileNotFoundException"));
            assertTrue(vodText.contains("Screen display call site"));
            assertFalse(vodText.contains("second segment"));
            assertTrue(liveText.contains("second segment rejected"));
            assertFalse(liveText.contains("first manifest"));
            assertEquals(2, saved.get(1).getAsJsonObject().get("count").getAsInt());
            assertFalse(snapshot.contains("unrelated pending error"));
            assertFalse(snapshot.contains("Original exception unavailable"));
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(snapshot);
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals(buffer.export(), restored.export());
        } finally {
            current.set(null, previous);
        }
    }

    // 验证旧后台异常迁移后不会混进屏幕诊断日志。
    @Test public void removesOldBackgroundErrorsAndRetainsScreenFailures() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String background = "java.lang.IllegalStateException: hidden background";
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("background", background, true);
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals("", buffer.export());
        String old = "[{\"time\":10000000,\"text\":\"error: " + background + "\",\"error\":true},"
                + "{\"time\":10000000,\"text\":\"notice: NewCz-playContent:\",\"error\":true,\"visible\":true}]";
        // 恢复仍在一小时范围内的日志快照。
        buffer.restore(old);
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("hidden background"));
        assertTrue(report.contains("NewCz-playContent:"));
    }

    // 验证只在屏幕报错时写入原始堆栈与显示链。
    @Test public void persistsOnlyScreenErrorsAndMatchesPluginFailureContexts() throws Exception {
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
            // 后台异常只短暂待匹配，不写复制日志。
            DiagnosticLog.record("home", "result=recovered,key=厂长,failures={empty=1}", true);
            // 后台异常只短暂待匹配，不写复制日志。
            DiagnosticLog.record("SpiderDebug", "java.lang.IllegalStateException: background", true);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("notice", "已复制", false);
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals("", buffer.export());
            IllegalStateException original = new IllegalStateException("root cause " + "x".repeat(200));
            // 原始异常仅在对应错误实际显示后写入日志。
            DiagnosticLog.prepareVisible("读取配置失败", original);
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals("", buffer.export());
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("notice", "读取配置失败", false);
            String plugin = "java.lang.NullPointerException\n\tat com.github.catvod.spider.NewCz.playerContent(NewCz.java:42)";
            // 后台异常只短暂待匹配，不写复制日志。
            DiagnosticLog.record("SpiderDebug", plugin, true);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("plugin-toast", "NewCz-playContent:", false);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("plugin-toast", "Other-playContent:", false);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("vod-screen", "播放失败", true);
            // 实际显示异常时保留原始信息和显示调用链。
            DiagnosticLog.visible("plugin-toast", "java.net.SocketException: Connection reset", false);
            // 将有限日志编码为可恢复的本地快照。
            JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
            assertEquals(5, saved.size());
            String first = saved.get(0).getAsJsonObject().get("text").getAsString();
            assertTrue(first.contains(original.getMessage()));
            assertTrue(first.contains("Screen display call site"));
            String second = saved.get(1).getAsJsonObject().get("text").getAsString();
            assertTrue(second.contains(plugin));
            String unmatched = saved.get(2).getAsJsonObject().get("text").getAsString();
            assertTrue(unmatched.contains("Original exception unavailable"));
            assertFalse(unmatched.contains("NewCz.java:42"));
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
