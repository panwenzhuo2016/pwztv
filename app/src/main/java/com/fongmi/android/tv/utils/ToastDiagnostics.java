package com.fongmi.android.tv.utils;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

final class ToastDiagnostics {
    private static final String TAG = "toast-diagnostics";
    private static volatile boolean active;
    private static volatile boolean partial;
    private static final ThreadLocal<Boolean> ownToast = new ThreadLocal<>();

    // 显示应用提示并避免系统观察入口重复记录。
    static void showOwn(Toast toast) {
        Boolean previous = ownToast.get();
        ownToast.set(true);
        try {
            toast.show();
        } finally {
            if (previous == null) ownToast.remove();
            else ownToast.set(previous);
        }
    }

    // 观察本进程系统提示，失败时保留应用自有入口。
    static synchronized void install(Consumer<String> observer) {
        if (active) return;
        try {
            Method getter = Toast.class.getDeclaredMethod("getService");
            getter.setAccessible(true);
            Object original = getter.invoke(null);
            if (original == null) throw new IllegalStateException("Toast service unavailable");
            Field service = Toast.class.getDeclaredField("sService");
            service.setAccessible(true);
            // 保留系统调用结果并在提交提示后观察文本。
            InvocationHandler handler = forwarding(original, observer);
            Object proxy = Proxy.newProxyInstance(ToastDiagnostics.class.getClassLoader(),
                    new Class<?>[]{getter.getReturnType()}, handler);
            service.set(null, proxy);
            active = true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            Log.w(TAG, "Toast observation unavailable", error);
        }
    }

    // 明示插件提示观察入口是否安装成功。
    static String status() {
        return active ? (partial ? "partial" : "active") : "unavailable";
    }

    // 保留系统调用结果并在提交提示后观察文本。
    static InvocationHandler forwarding(Object original, Consumer<String> observer) {
        return (proxy, method, args) -> {
            Object result;
            try {
                result = method.invoke(original, args);
            } catch (InvocationTargetException error) {
                throw error.getCause();
            }
            if (!method.getName().startsWith("enqueue") || !method.getName().contains("Toast")) return result;
            if (Boolean.TRUE.equals(ownToast.get())) return result;
            try {
                // 兼容系统文本提示与旧版自定义提示视图。
                String text = text(method, args);
                if (text == null) partial = true;
                if (text != null && !text.isBlank()) observer.accept(text);
            } catch (ReflectiveOperationException | RuntimeException error) {
                partial = true;
                Log.w(TAG, "Toast text observation failed", error);
            }
            return result;
        };
    }

    // 兼容系统文本提示与旧版自定义提示视图。
    private static String text(Method method, Object[] args) throws ReflectiveOperationException {
        if (args == null) return null;
        Class<?>[] types = method.getParameterTypes();
        for (int index = 0; index < types.length; index++) {
            if (types[index] == CharSequence.class && args[index] instanceof CharSequence text) return text.toString();
        }
        for (Object argument : args) {
            if (argument == null) continue;
            Field next;
            try {
                next = argument.getClass().getDeclaredField("mNextView");
            } catch (NoSuchFieldException missing) {
                continue;
            }
            next.setAccessible(true);
            if (!(next.get(argument) instanceof View view)) continue;
            View message = view.findViewById(android.R.id.message);
            if (message instanceof TextView label) return label.getText().toString();
        }
        return null;
    }
}
