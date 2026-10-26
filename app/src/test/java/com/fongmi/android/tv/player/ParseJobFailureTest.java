package com.fongmi.android.tv.player;

import android.util.Log;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.impl.ParseCallback;
import com.fongmi.android.tv.ui.custom.CustomWebView;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ParseJobFailureTest {
    // 验证回调及单页清理失败仍释放全部资源。
    @Test public void callbackFailureFallsBackAndCleansEveryWebView() throws Exception {
        ParseCallback callback = mock(ParseCallback.class);
        doThrow(new NoSuchMethodError("callback API")).when(callback).onParseSuccess(anyMap(), anyString(), anyString());
        doThrow(new IllegalStateException("failed callback")).when(callback).onParseError();
        CustomWebView first = mock(CustomWebView.class);
        CustomWebView second = mock(CustomWebView.class);
        doThrow(new IllegalStateException("destroyed view")).when(first).stop(false);
        doThrow(new NoSuchMethodError("destroy API")).when(first).destroy();
        ParseJob job = new ParseJob(callback);
        Field views = ParseJob.class.getDeclaredField("webViews");
        views.setAccessible(true);
        @SuppressWarnings("unchecked") List<CustomWebView> pending = (List<CustomWebView>) views.get(job);
        pending.add(first);
        pending.add(second);
        try (MockedStatic<App> app = mockStatic(App.class); MockedStatic<Log> logs = mockStatic(Log.class)) {
            app.when(() -> App.post(any(Runnable.class))).thenAnswer(call -> {
                call.<Runnable>getArgument(0).run();
                return null;
            });
            // 成功回调异常时仍通知失败并释放解析资源。
            job.onParseSuccess(Map.of(), "https://example.org/video", "test");
            verify(callback).onParseSuccess(anyMap(), anyString(), anyString());
            verify(callback).onParseError();
            verify(first).destroy();
            verify(second).stop(false);
            verify(second).destroy();
            assertTrue(pending.isEmpty());
            for (String field : new String[]{"executor", "infinite", "callback"}) {
                Field released = ParseJob.class.getDeclaredField(field);
                released.setAccessible(true);
                assertNull(released.get(job));
            }
            // 失败回调异常时仍释放本轮解析资源。
            job.onParseError();
            verify(callback, times(1)).onParseError();
        } finally {
            // 终止解析任务并逐个释放网页资源。
            job.stop();
        }
    }
}
