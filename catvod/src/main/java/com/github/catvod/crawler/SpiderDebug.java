package com.github.catvod.crawler;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;

public class SpiderDebug {

    private static final String TAG = SpiderDebug.class.getSimpleName();

    // 将异常堆栈同时交给本机诊断适配器。
    public static void log(Throwable th) {
        if (th == null) return;
        th.printStackTrace();
        Logger.t(TAG).e(android.util.Log.getStackTraceString(th));
    }

    public static void log(String msg) {
        if (!TextUtils.isEmpty(msg)) Logger.t(TAG).d(msg);
    }

    public static void log(String tag, String msg, Object... args) {
        if (!TextUtils.isEmpty(msg)) Logger.t(tag).d(msg, args);
    }
}
