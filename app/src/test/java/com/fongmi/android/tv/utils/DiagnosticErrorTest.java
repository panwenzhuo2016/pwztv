package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.*;

public class DiagnosticErrorTest {
    // 验证长播放标识不会挤掉源名与底层原因。
    @Test public void prioritizesPlaybackRootCauseWithinOneHundredCharacters() {
        String prefix = "tag=19f4853c-7ce7-4c93-ad81-66cc4b4e15b2,key=热播@@@75204@@@1,code=3003 : ";
        String error = prefix + "x1.k: Source error\n\tat player.start(Player.java:1)\n"
                + "Caused by: java.io.IOException: request failed\n\tat network.read(Network.java:2)\n"
                + "Caused by: androidx.media3.datasource.HttpDataSource$InvalidResponseCodeException: Response code: 403\n"
                + "\tat http.read(Http.java:3)\n"
                + "\tSuppressed: java.net.SocketTimeoutException: retry timed out\n"
                + "\tCaused by: java.net.ConnectException: alternate route failed";
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("f", error, true);
        // 将有限日志编码为可恢复的本地快照。
        String stored = JsonParser.parseString(buffer.encode()).getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        assertTrue(stored, stored.contains("source=热播,code=3003"));
        assertTrue(stored, stored.contains("Response code: 403"));
        assertFalse(stored.contains("19f4853c"));
        assertFalse(stored.contains("75204"));
        assertFalse(stored.contains("alternate route"));
        assertTrue(stored.substring("f: ".length()).length() <= RecentLogBuffer.MAX_BACKGROUND_CHARS);
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(buffer.encode());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
    }

    // 验证播放器缺少底层异常时仅保留已有原因。
    @Test public void retainsAvailablePlaybackReasonWithoutInventingCause() {
        // 优先保留播放源、错误码与主异常链根因。
        assertEquals("source=热播,code=3003 | x1.k: Source error",
                DiagnosticError.backgroundSummary("tag=session,key=热播@@@75204@@@1,code=3003 : x1.k: Source error\n\tat player.start(Player.java:1)"));
        // 优先保留播放源、错误码与主异常链根因。
        assertEquals("source=直播,code=2001 | SocketException: Connection reset",
                DiagnosticError.backgroundSummary("tag=session,key=直播,code=2001 : x1.k: Source error\nCaused by: java.net.SocketException: Connection reset"));
        String summary = "result=recovered,key=热播,failures={empty=3, timeout=1}";
        // 优先保留播放源、错误码与主异常链根因。
        assertEquals(summary, DiagnosticError.backgroundSummary(summary));
    }

    // 验证后台程序异常提取根因且不残留无效堆栈。
    @Test public void extractsProgramCauseAndPreservesRedactionAndScreenDetails() {
        String error = "java.util.concurrent.ExecutionException: wrapper\n\tat worker.run(Worker.java:1)\n"
                + "Caused by: java.lang.NumberFormatException: For input string: \"\"\n\tat plugin.parse(Plugin.java:2)";
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer buffer = new RecentLogBuffer(() -> 10_000_000);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("SpiderDebug", error, true);
        // 区分业务摘要与异常并保留诊断信息。
        buffer.append("f", "tag=session,key=热播@@@1,code=3003 : x1.k: Source error\n"
                + "Caused by: java.io.IOException: https://user:pass@video.example/v?token=private", true);
        // 仅屏幕异常保留详情，后台正文最多百字。
        buffer.append("vod-screen", error, true, true);
        // 将有限日志编码为可恢复的本地快照。
        JsonArray saved = JsonParser.parseString(buffer.encode()).getAsJsonArray();
        assertEquals("SpiderDebug: java.lang.NumberFormatException: For input string: \"\"",
                saved.get(0).getAsJsonObject().get("text").getAsString());
        String player = saved.get(1).getAsJsonObject().get("text").getAsString();
        assertTrue(player, player.contains("video.example/v"));
        assertFalse(player.contains("user:pass"));
        assertFalse(player.contains("private"));
        assertEquals("vod-screen: " + error, saved.get(2).getAsJsonObject().get("text").getAsString());
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(() -> 10_000_000);
        // 恢复仍在一小时范围内的日志快照。
        restored.restore(buffer.encode());
        // 导出按时间排列的最近一小时诊断文本。
        assertEquals(buffer.export(), restored.export());
    }

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
