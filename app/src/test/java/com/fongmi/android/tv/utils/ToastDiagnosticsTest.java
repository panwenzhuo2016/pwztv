package com.fongmi.android.tv.utils;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ToastDiagnosticsTest {
    // 验证应用自有提示不重复记录且结束后继续观察。
    @Test public void ownToastsDoNotDuplicateAndObservationResumesAfterward() throws Throwable {
        NotificationService service = mock(NotificationService.class);
        List<String> texts = new ArrayList<>();
        // 保留系统调用结果并在提交提示后观察文本。
        InvocationHandler handler = ToastDiagnostics.forwarding(service, texts::add);
        java.lang.reflect.Method method = NotificationService.class.getMethod("enqueueTextToast", String.class, CharSequence.class);
        Toast toast = mock(Toast.class);
        doAnswer(invocation -> handler.invoke(null, method, new Object[]{"app", "播放失败"})).when(toast).show();
        // 显示应用提示并避免系统观察入口重复记录。
        ToastDiagnostics.showOwn(toast);
        assertTrue(texts.isEmpty());
        handler.invoke(null, method, new Object[]{"app", "NewCz-playContent:"});
        assertEquals(List.of("NewCz-playContent:"), texts);
    }

    public interface NotificationService {
        boolean enqueueTextToast(String packageName, CharSequence text);
        boolean enqueueToast(String packageName, Callback callback);
    }

    public static final class Callback {
        private final View mNextView;

        // 模拟旧版系统提示回调中的待显示视图。
        Callback(View view) {
            mNextView = view;
        }
    }

    // 验证新旧系统提示文本都能提取且不误取包名。
    @Test public void capturesBothTextAndCustomToastsWhilePreservingSystemResults() throws Throwable {
        NotificationService service = mock(NotificationService.class);
        List<String> texts = new ArrayList<>();
        when(service.enqueueTextToast(anyString(), any())).thenReturn(true);
        when(service.enqueueToast(anyString(), any())).thenReturn(true);
        // 保留系统调用结果并在提交提示后观察文本。
        InvocationHandler handler = ToastDiagnostics.forwarding(service, texts::add);
        Object modern = handler.invoke(null, NotificationService.class.getMethod("enqueueTextToast", String.class, CharSequence.class),
                new Object[]{"com.example", "NewCz-playContent:"});
        assertEquals(true, modern);
        assertEquals(List.of("NewCz-playContent:"), texts);
        View view = mock(View.class);
        TextView label = mock(TextView.class);
        when(view.findViewById(android.R.id.message)).thenReturn(label);
        when(label.getText()).thenReturn("Value https cannot be converted to JSONObject");
        // 模拟旧版系统提示回调中的待显示视图。
        Callback callback = new Callback(view);
        Object legacy = handler.invoke(null, NotificationService.class.getMethod("enqueueToast", String.class, Callback.class),
                new Object[]{"com.example", callback});
        assertEquals(true, legacy);
        assertEquals(List.of("NewCz-playContent:", "Value https cannot be converted to JSONObject"), texts);
        verify(service).enqueueToast("com.example", callback);
    }

    // 验证诊断失败不影响提示且系统原异常原样传播。
    @Test public void observerFailureDoesNotChangeToastBehavior() throws Throwable {
        NotificationService service = mock(NotificationService.class);
        when(service.enqueueTextToast(anyString(), any())).thenReturn(true);
        try (MockedStatic<Log> logs = mockStatic(Log.class)) {
            // 保留系统调用结果并在提交提示后观察文本。
            InvocationHandler handler = ToastDiagnostics.forwarding(service, text -> { throw new IllegalStateException("observer failed"); });
            assertEquals(true, handler.invoke(null,
                    NotificationService.class.getMethod("enqueueTextToast", String.class, CharSequence.class), new Object[]{"app", "failed"}));
        }
        IllegalStateException original = new IllegalStateException("system failed");
        when(service.enqueueTextToast(anyString(), any())).thenThrow(original);
        // 保留系统调用结果并在提交提示后观察文本。
        InvocationHandler handler = ToastDiagnostics.forwarding(service, text -> fail("failed system call must not be recorded"));
        try {
            handler.invoke(null, NotificationService.class.getMethod("enqueueTextToast", String.class, CharSequence.class), new Object[]{"app", "failed"});
            fail("expected system exception");
        } catch (IllegalStateException error) {
            assertSame(original, error);
        }
    }
}
