package com.fongmi.android.tv.utils;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DiagnosticSummaryTest {
    // 验证调试与运行日志不进入待显示异常匹配。
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
}
