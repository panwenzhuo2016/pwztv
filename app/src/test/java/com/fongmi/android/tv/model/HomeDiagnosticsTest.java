package com.fongmi.android.tv.model;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;

import static org.junit.Assert.*;

public class HomeDiagnosticsTest {
    // 验证批量失败只有一次汇总且保留真实域名。
    @Test public void summarizesManyFailuresWithOneRepresentativeStack() {
        HomeDiagnostics logs = new HomeDiagnostics();
        for (int i = 0; i < 30; i++) {
            // 优先保存程序异常并保留片源域名。
            logs.capture("source-" + i, "https://video" + i + ".example/api?token=secret", new UnknownHostException("DNS failed " + i));
            // 汇总本轮各类来源失败次数。
            logs.failed("UnknownHostException");
        }
        // 汇总本轮各类来源失败次数。
        logs.failed("empty");
        // 每轮只导出一次结果汇总及代表异常。
        String report = logs.report(null);
        assertTrue(report.contains("result=unavailable"));
        assertTrue(report.contains("UnknownHostException=30"));
        assertTrue(report.contains("empty=1"));
        assertTrue(report.contains("key=source-0,host=video0.example"));
        assertTrue(report.contains("UnknownHostException: DNS failed 0"));
        assertTrue(report.contains("HomeDiagnosticsTest.java:"));
        assertFalse(report.contains("source-1"));
        assertFalse(report.contains("token=secret"));
    }

    // 验证正常成功不记日志且恢复结果可辨识。
    @Test public void recordsOnlyFailuresAndIdentifiesRecovery() {
        HomeDiagnostics logs = new HomeDiagnostics();
        // 每轮只导出一次结果汇总及代表异常。
        assertEquals("", logs.report("good"));
        // 汇总本轮各类来源失败次数。
        logs.failed("timeout");
        // 每轮只导出一次结果汇总及代表异常。
        assertEquals("result=recovered,key=backup,failures={timeout=1}", logs.report("backup"));
    }

    // 验证正常取消不能替代真正的诊断异常。
    @Test public void ignoresCancellationAndKeepsNextRealException() {
        HomeDiagnostics logs = new HomeDiagnostics();
        // 优先保存程序异常并保留片源域名。
        logs.capture("canceled", "csp_test", new CancellationException());
        // 优先保存程序异常并保留片源域名。
        logs.capture("canceled", "csp_test", new IOException("Canceled"));
        // 优先保存程序异常并保留片源域名。
        logs.capture("plugin", "csp_test", new IllegalStateException("real failure"));
        // 汇总本轮各类来源失败次数。
        logs.failed("IllegalStateException");
        // 每轮只导出一次结果汇总及代表异常。
        String report = logs.report(null);
        assertTrue(report.contains("key=plugin,host=plugin"));
        assertTrue(report.contains("real failure"));
        assertFalse(report.contains("Canceled"));
    }

    // 验证网络故障不能占住程序异常的诊断位置。
    @Test public void prioritizesSoftwareErrorAfterNetworkFailure() {
        HomeDiagnostics logs = new HomeDiagnostics();
        // 优先保存程序异常并保留片源域名。
        logs.capture("network", "https://network.example/api", new UnknownHostException("DNS failed"));
        // 优先保存程序异常并保留片源域名。
        logs.capture("parser", "csp_test", new IllegalStateException("software failure"));
        // 优先保存程序异常并保留片源域名。
        logs.capture("later", "https://later.example/api", new UnknownHostException("later DNS"));
        // 汇总本轮各类来源失败次数。
        logs.failed("IllegalStateException");
        // 每轮只导出一次结果汇总及代表异常。
        String report = logs.report(null);
        assertTrue(report.contains("key=parser,host=plugin"));
        assertTrue(report.contains("IllegalStateException: software failure"));
        assertTrue(report.contains("HomeDiagnosticsTest.java:"));
        assertFalse(report.contains("DNS failed"));
        assertFalse(report.contains("later DNS"));
    }
}
