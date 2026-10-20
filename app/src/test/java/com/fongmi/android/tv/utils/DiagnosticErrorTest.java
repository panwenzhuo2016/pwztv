package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiagnosticErrorTest {
    // 验证错误等级也不能让业务和运行信息入库。
    @Test public void removesBusinessAndRuntimeMessagesEvenWhenMarkedAsErrors() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        for (String message : new String[]{"result=recovered,key=厂长,failures={empty=1}",
                "result=unavailable,failures={timeout=3}", "Empty/NULL log message", "NewCz-playContent:",
                "已复制", "source=sony groups=30", "{\"list\":[{\"vod_name\":\"test\"}]}"}) {
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("home", message, true);
        }
        // 将有限日志编码为可恢复的本地快照。
        assertEquals(0, JsonParser.parseString(buffer.encode()).getAsJsonArray().size());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals("", buffer.export());
    }

    // 验证直接、包装及屏幕网络错误全部过滤。
    @Test public void removesNetworkErrorsAcrossWrappersAndRestarts() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        JsonArray old = new JsonArray();
        for (String type : new String[]{"java.net.SocketException", "java.net.SocketTimeoutException",
                "java.net.UnknownHostException", "java.net.ConnectException", "java.net.NoRouteToHostException",
                "okhttp3.internal.http2.StreamResetException", "okhttp3.internal.http2.ConnectionShutdownException",
                "java.util.concurrent.TimeoutException", "javax.net.ssl.SSLHandshakeException",
                "androidx.media3.datasource.HttpDataSource$InvalidResponseCodeException"}) {
            for (String message : new String[]{type + ": network failed\n\tat network.read(Network.java:1)",
                    "java.util.concurrent.ExecutionException: failed\nCaused by: " + type + ": network failed",
                    "tag=session,key=热播@@@75204@@@1,code=3003 : x1.k: Source error\nCaused by: " + type + ": network failed"}) {
                // 区分业务摘要与异常并保留诊断信息。
                buffer.append("f", message, true);
                // 仅记录有效异常并保留脱敏后的完整堆栈。
                buffer.append("vod-screen", message + "\njava.lang.Throwable: Screen display call site\n\tat app.show(App.java:1)", true, true);
                JsonObject item = new JsonObject();
                item.addProperty("time", 10_000_000);
                item.addProperty("text", "f: " + message);
                item.addProperty("error", true);
                item.addProperty("visible", true);
                old.add(item);
            }
        }
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("f", "java.io.IOException: Connection reset", true);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("f", "source=热播,code=3003 | SocketException: Connection reset", true);
        // 将有限日志编码为可恢复的本地快照。
        assertEquals(0, JsonParser.parseString(buffer.encode()).getAsJsonArray().size());
        // 恢复仍在一小时范围内的日志快照。
        buffer.restore(old.toString());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals("", buffer.export());
    }

    // 验证程序错误混有网络原因时仍保留完整堆栈。
    @Test public void preservesSoftwareErrorsAndUnknownIoFailuresInFull() {
        String network = "java.net.SocketException: Connection reset\n\tat network.read(Network.java:2)";
        for (String error : new String[]{"java.lang.IllegalStateException: software bug\nCaused by: " + network,
                network + "\nSuppressed: org.json.JSONException: invalid response\n\tat app.json(Parser.java:8)",
                "org.json.JSONException: Value java.net.SocketException cannot be converted to JSONObject",
                "a.b: obfuscated software exception\n\tat app.parse(Parser.java:4)\nCaused by: " + network,
                "java.lang.NullPointerException: parser bug\n\tat app.parse(Parser.java:4)\n".repeat(100),
                "java.io.IOException: disk failure\n\tat app.save(Store.java:5)\n".repeat(100)}) {
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
            // 区分业务摘要与异常并保留诊断信息。
            buffer.append("error", error, true);
            // 将有限日志编码为可恢复的本地快照。
            JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
            assertEquals(error, 1, saved.size());
            assertEquals("error: " + error, saved.get(0).getAsJsonObject().get("text").getAsString());
            // 注入时钟便于验证一小时保留边界。
            RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
            // 恢复仍在一小时范围内的日志快照。
            restored.restore(buffer.encode());
            // 导出按时间排列的最近一小时诊断文本。
            assertEquals(buffer.export(), restored.export());
        }
    }

    // 验证首页只留下程序异常和来源定位信息。
    @Test public void stripsHomeSummaryWhileRetainingTheProgramException() {
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        String error = "java.lang.NumberFormatException: For input string: \"\"\n\tat plugin.parse(Plugin.java:42)";
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("home", "result=recovered,key=backup,failures={empty=1}\nexample key=parser,host=plugin\n" + error, true);
        // 导出按时间排列的最近一小时诊断文本。
        String report = buffer.export();
        assertFalse(report.contains("result="));
        assertFalse(report.contains("failures="));
        assertTrue(report.contains("example key=parser,host=plugin"));
        assertTrue(report.contains(error));
    }
}
