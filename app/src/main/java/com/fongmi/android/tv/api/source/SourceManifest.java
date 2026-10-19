package com.fongmi.android.tv.api.source;

import com.github.catvod.utils.Asset;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SourceManifest {
    public int refreshHours = 6;
    public int fetchTimeoutSeconds = 8;
    public int initialWaitSeconds = 12;
    public int downloadLimitBytes = 8 * 1024 * 1024;
    public int failureCooldownSeconds = 300;
    public int maxVideoAttempts = 20;
    public int maxChannelRoutes = 24;
    public int maxSubscriptions = 20;
    public Entry vod;
    public List<Entry> live = Collections.emptyList();

    private static class Holder {
        // 读取内置订阅与失败切换策略。
        static final SourceManifest VALUE = read();
    }

    // 获取内置订阅与失败切换策略。
    public static SourceManifest get() {
        return Holder.VALUE;
    }

    // 读取内置订阅与失败切换策略。
    private static SourceManifest read() {
        SourceManifest value = new Gson().fromJson(Asset.read("sources.json"), SourceManifest.class);
        if (value == null || value.vod == null || value.live == null) throw new IllegalStateException("Invalid sources.json");
        if (value.fetchTimeoutSeconds < 1 || value.fetchTimeoutSeconds > 30 || value.refreshHours < 1
                || value.initialWaitSeconds < 1 || value.initialWaitSeconds > 20 || value.failureCooldownSeconds < 1
                || value.maxVideoAttempts < 1 || value.maxVideoAttempts > 100 || value.maxChannelRoutes < 1
                || value.maxChannelRoutes > 100 || value.maxSubscriptions < 1 || value.maxSubscriptions > 30
                || value.downloadLimitBytes < 1024 || value.downloadLimitBytes > 16 * 1024 * 1024) {
            throw new IllegalStateException("Source policy exceeds limits");
        }
        // 获取设备内共享的来源状态表。
        // 从订阅配置更新失败冷却时间。
        SourceHealth.get().configure(value.failureCooldownSeconds * 1000L);
        return value;
    }

    public static final class Entry {
        public String id = "", name = "", url = "", snapshot = "";
        public boolean domestic;
        public List<String> mirrors = Collections.emptyList();
        public Map<String, String> headers = Collections.emptyMap();
    }
}
