package com.fongmi.android.tv.api.source;

import com.github.catvod.utils.Prefers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LiveLineStore {
    private static final String PREF_KEY = "live_rejected_lines";
    private final Set<String> blocked;
    private final Consumer<String> save;

    private static final class Holder {
        // 读取设备保存的内容不符线路标记。
        private static final LiveLineStore INSTANCE = new LiveLineStore(Prefers.getString(PREF_KEY), value -> Prefers.put(PREF_KEY, value));
    }

    // 读取设备保存的内容不符线路标记。
    public LiveLineStore(String stored, Consumer<String> save) {
        this.blocked = new LinkedHashSet<>();
        if (stored != null && !stored.isEmpty()) blocked.addAll(Arrays.asList(stored.split("\n")));
        this.save = save;
    }

    // 获取设备共享的直播线路标记。
    public static LiveLineStore get() {
        return Holder.INSTANCE;
    }

    // 以地址摘要标记线路且忽略展示名称。
    private static String key(String route) {
        String url = route == null ? "" : route.split("\\$", 2)[0];
        try {
            return new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8))).toString(16);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }

    // 查询地址是否被用户标记为内容不符。
    public synchronized boolean isBlocked(String route) {
        // 以地址摘要标记线路且忽略展示名称。
        return blocked.contains(key(route));
    }

    // 保存或解除内容不符标记并立即持久化。
    public synchronized void setBlocked(String route, boolean rejected) {
        if (route == null || route.isEmpty()) return;
        // 以地址摘要标记线路且忽略展示名称。
        String key = key(route);
        if (rejected) blocked.add(key);
        else blocked.remove(key);
        save.accept(String.join("\n", blocked));
    }

    // 有限选择未被标记且符合条件的备用线路。
    public int next(List<String> routes, int current, Predicate<String> available) {
        // 按指定方向遍历且不重新选择当前线路。
        return select(routes, current, 1, available);
    }

    // 反向选择未被标记且符合条件的备用线路。
    public int previous(List<String> routes, int current, Predicate<String> available) {
        // 按指定方向遍历且不重新选择当前线路。
        return select(routes, current, -1, available);
    }

    // 按指定方向遍历且不重新选择当前线路。
    private int select(List<String> routes, int current, int direction, Predicate<String> available) {
        for (int step = 1; step < routes.size(); step++) {
            int index = Math.floorMod(current + step * direction, routes.size());
            String route = routes.get(index);
            String url = route.split("\\$", 2)[0];
            // 查询地址是否被用户标记为内容不符。
            if (!isBlocked(route) && available.test(url)) return index;
        }
        return -1;
    }
}
