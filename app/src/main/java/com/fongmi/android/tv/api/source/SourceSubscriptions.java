package com.fongmi.android.tv.api.source;

import android.util.AtomicFile;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.Decoder;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Asset;
import com.github.catvod.utils.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

public final class SourceSubscriptions {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Map<String, Future<String>> TASKS = new HashMap<>();

    // 获取有效缓存或随包提供的订阅快照。
    public static String cached(SourceManifest.Entry entry, SourceValidator.Kind kind) {
        // 按订阅地址确定私有缓存位置。
        File file = cache(entry);
        try {
            // 获取内置订阅与失败切换策略。
            if (file.isFile() && file.length() <= SourceManifest.get().downloadLimitBytes) {
                String text;
                synchronized (TASKS) {
                    try (InputStream input = new AtomicFile(file).openRead()) {
                        // 有限读取响应并校验订阅结构。
                        text = read(input, SourceManifest.get().downloadLimitBytes);
                    }
                }
                // 校验响应结构避免把网页或网址当配置。
                if (SourceValidator.valid(text, kind)) return text;
            }
        } catch (IOException error) {
            SpiderDebug.log("sources", "source=%s cache read failed: %s", entry.id, error.getClass().getSimpleName());
        }
        String snapshot = entry.snapshot == null || entry.snapshot.isEmpty() ? "" : Asset.read(entry.snapshot);
        // 校验响应结构避免把网页或网址当配置。
        return SourceValidator.valid(snapshot, kind) ? snapshot : "";
    }

    // 去重安排订阅更新且不阻塞缓存读取。
    public static synchronized Future<String> refresh(SourceManifest.Entry entry, SourceValidator.Kind kind) {
        // 按订阅地址确定私有缓存位置。
        File file = cache(entry);
        // 获取内置订阅与失败切换策略。
        // 检查磁盘缓存自身是否有效。
        boolean fresh = file.isFile() && System.currentTimeMillis() - file.lastModified() < TimeUnit.HOURS.toMillis(SourceManifest.get().refreshHours)
                && validCache(entry, kind);
        // 获取设备内共享的来源状态表。
        // 冷却结束后允许重新尝试来源。
        if (fresh || !SourceHealth.get().available("subscription:" + entry.url)) return null;
        String key = kind + ":" + entry.url;
        Future<String> running = TASKS.get(key);
        if (running != null && !running.isDone()) return running;
        TASKS.entrySet().removeIf(item -> item.getValue().isDone());
        // 下载订阅并原子替换最后有效缓存。
        Future<String> task = POOL.submit(() -> download(entry, kind));
        TASKS.put(key, task);
        return task;
    }

    // 检查磁盘缓存自身是否有效。
    private static boolean validCache(SourceManifest.Entry entry, SourceValidator.Kind kind) {
        // 按订阅地址确定私有缓存位置。
        synchronized (TASKS) {
            try (InputStream input = new AtomicFile(cache(entry)).openRead()) {
                // 有限读取响应并校验订阅结构。
                // 校验响应结构避免把网页或网址当配置。
                return SourceValidator.valid(read(input, SourceManifest.get().downloadLimitBytes), kind);
            } catch (IOException error) {
                SpiderDebug.log("sources", "source=%s invalid cache: %s", entry.id, error.getClass().getSimpleName());
                return false;
            }
        }
    }

    // 按订阅地址确定私有缓存位置。
    private static File cache(SourceManifest.Entry entry) {
        File directory = new File(App.get().getFilesDir(), "source-cache");
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) throw new IllegalStateException("Cannot create source-cache");
        return new File(directory, Util.md5(entry.url) + ".txt");
    }

    // 下载订阅并原子替换最后有效缓存。
    private static String download(SourceManifest.Entry entry, SourceValidator.Kind kind) {
        List<String> urls = new ArrayList<>(List.of(entry.url));
        if (entry.mirrors != null) urls.addAll(entry.mirrors.stream().limit(2).toList());
        // 获取内置订阅与失败切换策略。
        SourceManifest policy = SourceManifest.get();
        for (String url : urls) {
            if (!url.startsWith("https://") && !url.startsWith("http://")) continue;
            try {
                Request request = new Request.Builder().url(url).headers(Headers.of(entry.headers == null ? Map.of() : entry.headers)).build();
                try (Response response = OkHttp.client().newBuilder().callTimeout(policy.fetchTimeoutSeconds, TimeUnit.SECONDS).build().newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) throw new IOException("HTTP " + response.code());
                    if (response.body().contentLength() > policy.downloadLimitBytes) throw new IOException("Subscription too large");
                    // 有限读取响应并校验订阅结构。
                    String text = read(response.body().byteStream(), policy.downloadLimitBytes);
                    // 校验响应结构避免把网页或网址当配置。
                    if (!SourceValidator.valid(text, kind)) throw new IOException("Invalid subscription format");
                    if (kind == SourceValidator.Kind.VOD) text = Decoder.fixJson(url, text);
                    // 按订阅地址确定私有缓存位置。
                    AtomicFile atomic = new AtomicFile(cache(entry));
                    FileOutputStream stream = null;
                    synchronized (TASKS) {
                        try {
                            stream = atomic.startWrite();
                            stream.write(text.getBytes(StandardCharsets.UTF_8));
                            atomic.finishWrite(stream);
                        } catch (IOException error) {
                            if (stream != null) atomic.failWrite(stream);
                            throw error;
                        }
                    }
                    // 获取设备内共享的来源状态表。
                    // 成功播放后清除对应失败记录。
                    SourceHealth.get().success("subscription:" + entry.url);
                    SpiderDebug.log("sources", "source=%s refreshed bytes=%d", entry.id, text.length());
                    return text;
                }
            } catch (IOException | RuntimeException error) {
                SpiderDebug.log("sources", "source=%s update failed: %s", entry.id, error.getClass().getSimpleName());
            }
        }
        // 获取设备内共享的来源状态表。
        // 记录失败并淘汰最早的状态。
        SourceHealth.get().fail("subscription:" + entry.url);
        return "";
    }

    // 有限读取响应并校验订阅结构。
    private static String read(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (output.size() + count > limit) throw new IOException("Subscription too large");
            output.write(buffer, 0, count);
        }
        return output.toString(StandardCharsets.UTF_8.name()).replace("\uFEFF", "");
    }
}
