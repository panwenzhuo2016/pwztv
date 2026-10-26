package com.fongmi.android.tv.utils;

import android.util.Log;

public final class FailureGuard {
    // 仅允许通过静态入口隔离失败。
    private FailureGuard() {}

    // 隔离可恢复异常并返回是否完成当前操作。
    public static boolean run(String operation, Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException | LinkageError error) {
            // 使用系统日志避免再次进入诊断适配器。
            report(operation, error);
            return false;
        }
    }

    // 使用系统日志避免再次进入诊断适配器。
    public static void report(String operation, Throwable error) {
        Log.w("recovery", "operation=" + operation, error);
    }
}
