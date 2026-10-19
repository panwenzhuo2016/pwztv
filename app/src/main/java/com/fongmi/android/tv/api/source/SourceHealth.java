package com.fongmi.android.tv.api.source;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public final class SourceHealth {
    private static final SourceHealth INSTANCE = new SourceHealth(TimeUnit.MINUTES.toMillis(5), 4096, System::currentTimeMillis);
    private final Map<String, Long> failed = new LinkedHashMap<>();
    private final LongSupplier clock;
    private final int capacity;
    private long cooldown;

    // 创建有容量与时间边界的来源状态表。
    public SourceHealth(long cooldown, int capacity, LongSupplier clock) {
        this.cooldown = Math.max(1, cooldown);
        this.capacity = Math.max(1, capacity);
        this.clock = clock;
    }

    // 获取设备内共享的来源状态表。
    public static SourceHealth get() {
        return INSTANCE;
    }

    // 从订阅配置更新失败冷却时间。
    public synchronized void configure(long cooldown) {
        this.cooldown = Math.max(1, cooldown);
    }

    // 记录失败并淘汰最早的状态。
    public synchronized void fail(String key) {
        if (key == null || key.isEmpty()) return;
        failed.remove(key);
        failed.put(key, clock.getAsLong() + cooldown);
        while (failed.size() > capacity) failed.remove(failed.keySet().iterator().next());
    }

    // 成功播放后清除对应失败记录。
    public synchronized void success(String key) {
        failed.remove(key);
    }

    // 冷却结束后允许重新尝试来源。
    public synchronized boolean available(String key) {
        Long until = failed.get(key);
        if (until == null) return true;
        if (until > clock.getAsLong()) return false;
        failed.remove(key);
        return true;
    }

    // 有限遍历后选择下一条可用线路。
    public int next(List<String> urls, int current) {
        // 有限遍历并跳过本轮已失败的线路。
        return next(urls, current, Set.of());
    }

    // 有限遍历并跳过本轮已失败的线路。
    public int next(List<String> urls, int current, Set<String> attempted) {
        for (int step = 1; step < urls.size(); step++) {
            int index = Math.floorMod(current + step, urls.size());
            String key = urls.get(index).split("\\$", 2)[0];
            // 冷却结束后允许重新尝试来源。
            if (!attempted.contains(key) && available(key)) return index;
        }
        return -1;
    }
}
