package com.fongmi.android.tv.model;

import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

final class HomeDiagnostics {
    private final Map<String, Integer> failures = new LinkedHashMap<>();
    private String example;

    // 汇总本轮各类来源失败次数。
    synchronized void failed(String reason) {
        failures.merge(reason, 1, Integer::sum);
    }

    // 保存首个真实异常及片源域名。
    synchronized void capture(String key, String api, Exception error) {
        if (example != null || Thread.currentThread().isInterrupted()
                || error instanceof InterruptedException || error instanceof CancellationException
                || (error instanceof InterruptedIOException && !(error instanceof SocketTimeoutException))
                || "Canceled".equals(error.getMessage()) || "Home request canceled".equals(error.getMessage())) return;
        String host;
        try {
            host = URI.create(api).getHost();
        } catch (IllegalArgumentException invalid) {
            host = null;
        }
        StringWriter stack = new StringWriter();
        error.printStackTrace(new PrintWriter(stack));
        example = "example key=" + key + ",host=" + (host == null ? "plugin" : host) + "\n" + stack;
    }

    // 每轮只导出一次结果汇总及代表异常。
    synchronized String report(String recoveredKey) {
        if (failures.isEmpty()) return "";
        return "result=" + (recoveredKey == null ? "unavailable" : "recovered,key=" + recoveredKey)
                + ",failures=" + failures + (example == null ? "" : "\n" + example);
    }
}
