package com.fongmi.android.tv.api.config;

import com.fongmi.android.tv.api.Decoder;
import com.fongmi.android.tv.api.source.SourceCatalog;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.utils.Asset;

import java.util.concurrent.Callable;

public final class BuiltinConfig {
    public static final String URL = "builtin";
    public static final String BASE = "https://raw.githubusercontent.com/qist/tvbox/refs/heads/master/";

    // 统一读取内置资源与远程配置。
    public static String load(String url, String tag) throws Exception {
        // 统一读取内置资源与远程配置。
        return load(url, SourceCatalog::vod, asset -> {
            String text = Asset.read(asset.substring("assets://".length()));
            if (text.isEmpty()) throw new IllegalArgumentException("assets file not found");
            return Decoder.fixJson(UrlUtil.convert(asset), text);
        }, remote -> Decoder.getJson(UrlUtil.convert(remote), tag));
    }

    // 统一读取内置资源与远程配置。
    static String load(String url, Callable<String> builtin, Reader asset, Reader remote) throws Exception {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Configuration URL is empty");
        url = url.trim();
        // 使用有效点播快照并后台更新订阅。
        if (URL.equals(url)) return builtin.call();
        // 按配置地址读取原始内容。
        if (url.startsWith("assets://")) return asset.read(url);
        // 按配置地址读取原始内容。
        return remote.read(url);
    }

    @FunctionalInterface
    interface Reader {
        // 按配置地址读取原始内容。
        String read(String url) throws Exception;
    }
}
