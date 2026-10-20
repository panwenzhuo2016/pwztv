package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
import com.fongmi.chaquo.Loader;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private Loader loader;
    private String recent;

    // 创建Python片源缓存且暂不启动运行时。
    public PyLoader() {
        spiders = new ConcurrentHashMap<>();
    }

    // 首次使用Python片源时启动运行时。
    private synchronized Loader loader() {
        if (loader == null) loader = new Loader();
        return loader;
    }

    // 清理已创建的Python片源实例。
    public void clear() {
        spiders.values().forEach(Spider::destroy);
        spiders.clear();
    }

    // 记录最近使用的Python片源。
    public void setRecent(String recent) {
        this.recent = recent;
    }

    // 获取Python片源或返回不可用结果。
    public Spider getSpider(String key, String api, String ext) {
        return spiders.computeIfAbsent(key, k -> {
            try {
                // 首次使用Python片源时启动运行时。
                Spider spider = loader().spider(api);
                spider.siteKey = key;
                spider.init(App.get(), ext);
                return spider;
            } catch (Throwable e) {
                e.printStackTrace();
                return new SpiderNull();
            }
        });
    }

    // 处理最近使用的Python片源代理请求。
    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
