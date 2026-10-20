package com.fongmi.android.tv.model;

import org.junit.Test;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.junit.Assert.*;

public class HomeRequestTest {
    // 反查登记过的实际连接会收到取消而非仅取消线程。
    @Test public void cancelsRegisteredConnectionWithoutNetworkAccess() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain ->
                new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                        .code(200).message("OK").body(ResponseBody.create(new byte[0], null)).build()).build();
        Call call = client.newCall(new Request.Builder().url("https://example.invalid/").build());
        // 绑定单个首页来源的内容加载任务。
        HomeRequest request = new HomeRequest(connection -> null);
        // 登记连接使超时和换源能立即取消网络请求。
        try (Response response = request.execute(call)) {
            assertEquals(200, response.code());
            assertTrue(call.isExecuted());
            assertFalse(call.isCanceled());
            // 取消当前来源的网络请求。
            request.cancel();
            assertTrue(call.isCanceled());
        }
    }

    // 反查超时取消后新连接无法偷偷启动。
    @Test public void cancellationRejectsAndCancelsSubsequentConnections() {
        // 绑定单个首页来源的内容加载任务。
        HomeRequest request = new HomeRequest(connection -> null);
        // 取消当前来源的网络请求。
        request.cancel();
        Call late = new OkHttpClient().newCall(new Request.Builder().url("https://example.invalid/").build());
        // 登记连接使超时和换源能立即取消网络请求。
        assertThrows(IOException.class, () -> request.execute(late));
        assertTrue(late.isCanceled());
        assertFalse(late.isExecuted());
    }
}
