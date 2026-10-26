package com.fongmi.android.tv.utils;

import android.util.Log;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DiagnosticLogFailureTest {
    // 验证诊断兼容错误或解析异常不阻断播放恢复。
    @Test public void diagnosticFailuresDoNotEscapeOrPersistBackgroundErrors() throws Exception {
        DiagnosticLog adapter = mock(DiagnosticLog.class);
        ScreenErrorContext context = mock(ScreenErrorContext.class);
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
        pending.set(adapter, context);
        current.set(null, adapter);
        String message = "java.net.SocketException: Connection reset";
        NoSuchMethodError missing = new NoSuchMethodError("String.lines");
        IllegalStateException invalid = new IllegalStateException("diagnostic parser failed");
        // 兼容旧安卓提取首行并匹配插件异常。
        doThrow(missing, invalid).when(context).rememberLogged(message);
        try (MockedStatic<Log> logs = mockStatic(Log.class)) {
            // 后台异常待匹配且诊断失败不影响播放。
            DiagnosticLog.record("player", message, true);
            // 后台异常待匹配且诊断失败不影响播放。
            DiagnosticLog.record("player", message, true);
            // 兼容旧安卓提取首行并匹配插件异常。
            verify(context, times(2)).rememberLogged(message);
            logs.verify(() -> Log.w("diagnostics", "session=logger diagnostic capture failed", missing));
            logs.verify(() -> Log.w("diagnostics", "session=logger diagnostic capture failed", invalid));
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals("", buffer.export());
        } finally {
            current.set(null, previous);
        }
    }
}
