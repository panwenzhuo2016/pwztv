package com.fongmi.android.tv.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiagnosticErrorTest {
    // 验证插件空原因提示与技术错误都能识别。
    @Test public void recognizesPopupFailuresWithoutTreatingSuccessAsError() {
        for (String message : new String[]{"NewCz-playContent:", "Other-searchContent: failed",
                "播放失败", "Value https of type java.lang.String cannot be converted to JSONObject",
                "java.net.SocketException: Connection reset", "org.json.JSONException: empty"}) {
            // 识别屏幕错误提示并排除普通成功通知。
            assertTrue(message, DiagnosticError.isPopupError(message));
        }
        for (String message : new String[]{"已复制", "收藏成功", "正在切换片源", "NewCz", "", "Empty/NULL log message"}) {
            // 识别屏幕错误提示并排除普通成功通知。
            assertFalse(message, DiagnosticError.isPopupError(message));
        }
    }

    // 验证首页原有网络分类仍不误判程序异常。
    @Test public void preservesSoftwareExceptionsWhenTheyWrapNetworkFailures() {
        String network = "okhttp3.internal.http2.StreamResetException: stream was reset: CANCEL";
        // 仅过滤网络异常链，混有程序异常时仍保留。
        assertTrue(DiagnosticError.isRoutineNetwork(network));
        // 仅过滤网络异常链，混有程序异常时仍保留。
        assertFalse(DiagnosticError.isRoutineNetwork("java.lang.IllegalStateException: software failure\nCaused by: " + network));
    }
}
