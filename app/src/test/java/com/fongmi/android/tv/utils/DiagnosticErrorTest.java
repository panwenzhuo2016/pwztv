package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiagnosticErrorTest {
    // 验证常见网络错误最多百字且不带协议堆栈。
    @Test public void limitsRoutineNetworkErrorsAndRestoresTheirSummaries() {
        for (String type : new String[]{"SocketException", "SocketTimeoutException", "UnknownHostException",
                "ConnectException", "NoRouteToHostException"}) {
            String error = "result=recovered,key=backup,failures={" + type + "=3}\n"
                    + "example key=source,host=video.example\njava.net." + type + ": " + "failure ".repeat(30)
                    + "\n\tat okhttp3.Connection.connect(Connection.java:42)\n".repeat(40)
                    + "Suppressed: java.net." + type + ": retry failed\n\tat retry.run(Retry.java:1)";
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("home", error, true);
            // 将有限日志编码为可恢复的本地快照。
            JsonObject saved = JsonParser.parseString(buffer.encode()).getAsJsonArray().get(0).getAsJsonObject();
            String body = saved.get("text").getAsString().substring("home: ".length());
            assertTrue(body, body.length() <= RecentLogBuffer.MAX_BACKGROUND_CHARS);
            assertTrue(body.startsWith("java.net." + type + ":"));
            assertFalse(body.contains("\n"));
            assertFalse(body.contains("okhttp3"));
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(buffer.encode());
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals(buffer.export(), restored.export());
        }
    }

    // 验证包装后的网络故障合并且旧堆栈被压缩。
    @Test public void compactsWrappedNetworkErrorsAndMigratesOldSnapshots() {
        String error = "java.util.concurrent.ExecutionException: request failed\n\tat worker.run(Worker.java:1)\n"
                + "Caused by: java.net.SocketException: Connection reset\n\tat network.read(Network.java:2)";
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (int i = 0; i < 3; i++) {
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("SpiderDebug", error, true);
        }
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals(1, saved.size());
        assertEquals(3, saved.get(0).getAsJsonObject().get("count").getAsInt());
        assertEquals("SpiderDebug: java.net.SocketException: Connection reset",
                saved.get(0).getAsJsonObject().get("text").getAsString());
        saved.get(0).getAsJsonObject().addProperty("text", "SpiderDebug: " + error);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(saved.toString());
        // 导出按时间排列的最近一小时诊断文本。
        String report = restored.export();
        assertTrue(report.contains("[count=3,last="));
        assertTrue(report.contains("SocketException: Connection reset"));
        assertFalse(report.contains("network.read"));
    }

    // 验证屏幕异常包含网络原因时仍完整保留。
    @Test public void preservesSoftwareErrorsAndUnknownIoFailures() {
        String network = "java.net.SocketException: Connection reset\n\tat network.read(Network.java:2)";
        String program = "java.lang.IllegalStateException: software bug\n\tat app.parse(Parser.java:4)\nCaused by: " + network;
        String suppressed = network + "\nSuppressed: org.json.JSONException: invalid response\n\tat app.json(Parser.java:8)";
        String messageOnly = "org.json.JSONException: Value java.net.SocketException cannot be converted to JSONObject\n\tat app.json(Parser.java:8)";
        for (String error : new String[]{program, suppressed, messageOnly,
                "a.b: obfuscated software exception\n\tat app.parse(Parser.java:4)\nCaused by: " + network,
                "java.lang.NullPointerException: parser bug\n\tat app.parse(Parser.java:4)\n".repeat(100),
                "java.io.IOException: disk failure\n\tat app.save(Store.java:5)\n".repeat(100),
                "javax.net.ssl.SSLHandshakeException: certificate configuration\n\tat app.ssl(Config.java:7)"}) {
            // 仅将明确的网络异常链归为普通故障。
            assertFalse(error, DiagnosticError.isRoutineNetwork(error));
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
            // 仅屏幕异常保留详情，后台正文最多百字。
            buffer.append("error", error, true, true);
            // 将有限日志编码为可恢复的本地快照。
            String stored = JsonParser.parseString(buffer.encode()).getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
            assertEquals("error: " + error, stored);
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(buffer.encode());
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals(buffer.export(), restored.export());
        }
    }
}
