package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.api.LiveParser;
import com.fongmi.android.tv.bean.Live;
import com.github.catvod.crawler.SpiderDebug;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class LiveCatalog {
    // 识别内置或用户定义的直播聚合入口。
    public static boolean accepts(Live live) {
        // 获取用户配置的直播聚合订阅。
        return live.getUrl().startsWith("builtin://") || !live.getSources().isEmpty();
    }

    // 并行更新清单并用有效快照聚合频道。
    public static void load(Live target) {
        // 获取内置订阅与失败切换策略。
        SourceManifest policy = SourceManifest.get();
        List<SourceManifest.Entry> entries = new ArrayList<>();
        // 获取用户配置的直播聚合订阅。
        List<Live> custom = target.getSources();
        if (custom.isEmpty()) {
            for (SourceManifest.Entry entry : policy.live) {
                if (!target.getUrl().startsWith("builtin://source/") || target.getUrl().equals("builtin://source/" + entry.id)) entries.add(entry);
            }
        } else {
            for (Live source : custom.subList(0, Math.min(custom.size(), policy.maxSubscriptions))) {
                SourceManifest.Entry entry = new SourceManifest.Entry();
                entry.id = entry.name = source.getName();
                entry.url = source.getUrl();
                entry.headers = source.getHeaders();
                entries.add(entry);
            }
        }
        List<Future<String>> updates = new ArrayList<>();
        for (SourceManifest.Entry entry : entries) {
            // 去重安排订阅更新且不阻塞缓存读取。
            updates.add(SourceSubscriptions.refresh(entry, SourceValidator.Kind.LIVE));
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(policy.initialWaitSeconds);
        LiveMerger merger = new LiveMerger(target, policy.maxChannelRoutes);
        for (int i = 0; i < entries.size(); i++) {
            if (Thread.currentThread().isInterrupted()) return;
            SourceManifest.Entry entry = entries.get(i);
            // 获取有效缓存或随包提供的订阅快照。
            String text = SourceSubscriptions.cached(entry, SourceValidator.Kind.LIVE);
            Future<String> update = updates.get(i);
            if (text.isEmpty() && update != null && System.nanoTime() < deadline) {
                try {
                    text = update.get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception error) {
                    SpiderDebug.log("sources", "source=%s initial load failed: %s", entry.id, error.getClass().getSimpleName());
                }
            }
            if (text.isEmpty()) continue;
            Live source = new Live(entry.name, entry.url);
            LiveParser.text(source, text);
            // 合并同台线路并保留各源请求参数。
            merger.add(source, entry);
        }
        // 固定分类顺序并优先展示国内频道。
        merger.finish();
        SpiderDebug.log("sources", "source=%s groups=%d", target.getName(), target.getGroups().size());
    }
}
