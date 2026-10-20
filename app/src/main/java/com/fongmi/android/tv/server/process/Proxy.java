package com.fongmi.android.tv.server.process;

import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.server.Nano;
import com.fongmi.android.tv.server.impl.Process;
import com.fongmi.android.tv.utils.StringMap;

import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Proxy implements Process {

    @Override
    public boolean isRequest(IHTTPSession session, String url) {
        return url.startsWith("/proxy");
    }

    @Override
    // 校验插件响应头类型后创建代理响应。
    public Response doResponse(IHTTPSession session, String url, Map<String, String> files) {
        try {
            Map<String, String> params = session.getParms();
            params.putAll(session.getHeaders());
            params.putAll(files);
            // 按来源分派片源代理请求。
            Object[] rs = BaseLoader.get().proxy(params);
            if (rs[0] instanceof Response) return (Response) rs[0];
            Response response = NanoHTTPD.newChunkedResponse(Status.lookup((Integer) rs[0]), (String) rs[1], (InputStream) rs[2]);
            // 校验并复制字符串映射，保留空键值兼容性。
            Map<String, String> headers = StringMap.copy(rs.length > 3 ? rs[3] : null);
            for (Map.Entry<String, String> entry : headers.entrySet()) response.addHeader(entry.getKey(), entry.getValue());
            return response;
        } catch (Throwable e) {
            e.printStackTrace();
            return Nano.error(e.getMessage());
        }
    }
}
