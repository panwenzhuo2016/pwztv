package com.fongmi.android.tv.api.source;

import android.view.View;

import com.fongmi.android.tv.bean.Channel;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class LiveLineStoreTest {

    // 验证单线路仍显示来源并保留恢复入口。
    @Test public void singleLineKeepsRecoveryControlVisible() {
        Channel channel = new Channel();
        channel.setUrls(List.of("https://example.org/live.m3u8$Domestic backup"));
        // 单线路也保留标记与恢复入口。
        assertEquals(View.VISIBLE, channel.getLineVisible());
        // 单线路也显示来源名称或线路编号。
        assertEquals("Domestic backup", channel.getLineText());
        channel.setUrls(List.of());
        // 单线路也保留标记与恢复入口。
        assertEquals(View.GONE, channel.getLineVisible());
        // 单线路也显示来源名称或线路编号。
        assertEquals("", channel.getLineText());
    }

    // 反查标记持久化且改名后仍跳过同地址。
    @Test public void rejectionSurvivesReloadAndSourceRename() {
        AtomicReference<String> saved = new AtomicReference<>("");
        // 读取设备保存的内容不符线路标记。
        LiveLineStore store = new LiveLineStore(saved.get(), saved::set);
        String url = "https://example.org/wrong.m3u8?token=secret";
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked(url + "$Source A", true);
        // 查询地址是否被用户标记为内容不符。
        assertTrue(store.isBlocked(url));
        assertFalse(saved.get().contains(url));
        assertFalse(saved.get().contains("secret"));
        // 读取设备保存的内容不符线路标记。
        LiveLineStore reloaded = new LiveLineStore(saved.get(), saved::set);
        // 查询地址是否被用户标记为内容不符。
        assertTrue(reloaded.isBlocked(url + "$Renamed source"));
        // 查询地址是否被用户标记为内容不符。
        assertFalse(reloaded.isBlocked("https://example.org/correct.m3u8"));
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(2, reloaded.next(List.of("current", url + "$Renamed", "good"), 0, ignored -> true));
    }

    // 反查恢复后标记从存储移除并允许选择。
    @Test public void restoringLinePersistsRemovalWithoutClearingOtherMarks() {
        AtomicReference<String> saved = new AtomicReference<>("");
        // 读取设备保存的内容不符线路标记。
        LiveLineStore store = new LiveLineStore(saved.get(), saved::set);
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("wrong", true);
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("other", true);
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("wrong$New label", false);
        // 读取设备保存的内容不符线路标记。
        LiveLineStore reloaded = new LiveLineStore(saved.get(), saved::set);
        // 查询地址是否被用户标记为内容不符。
        assertFalse(reloaded.isBlocked("wrong"));
        // 查询地址是否被用户标记为内容不符。
        assertTrue(reloaded.isBlocked("other"));
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(1, reloaded.next(List.of("current", "wrong", "other"), 0, ignored -> true));
        // 保存或解除内容不符标记并立即持久化。
        reloaded.setBlocked("other", false);
        assertEquals("", saved.get());
        // 读取设备保存的内容不符线路标记。
        LiveLineStore cleared = new LiveLineStore(saved.get(), saved::set);
        // 查询地址是否被用户标记为内容不符。
        assertFalse(cleared.isBlocked("other"));
    }

    // 验证自动换线同时遵守冷却和本轮排除。
    @Test public void automaticFallbackHonorsAllExclusions() {
        // 读取设备保存的内容不符线路标记。
        LiveLineStore store = new LiveLineStore("", ignored -> {});
        // 创建有容量与时间边界的来源状态表。
        SourceHealth health = new SourceHealth(10, 20, () -> 0);
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("ad", true);
        // 记录失败并淘汰最早的状态。
        health.fail("cooling");
        List<String> routes = List.of("current", "ad$Source", "attempted", "cooling", "good");
        Set<String> attempted = Set.of("attempted");
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(4, store.next(routes, 0, url -> {
            // 冷却结束后允许重新尝试来源。
            return !attempted.contains(url) && health.available(url);
        }));
        // 成功播放后清除对应失败记录。
        health.success("ad");
        // 查询地址是否被用户标记为内容不符。
        assertTrue(store.isBlocked("ad"));
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.next(routes, 0, url -> false));
    }

    // 验证正反向轮换跳过标记且耗尽时不循环。
    @Test public void manualSelectionSkipsMarkedLinesInBothDirections() {
        // 读取设备保存的内容不符线路标记。
        LiveLineStore store = new LiveLineStore("", ignored -> {});
        List<String> routes = List.of("a", "ad", "b", "ad$Duplicate");
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("ad", true);
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(2, store.next(routes, 0, url -> true));
        // 反向选择未被标记且符合条件的备用线路。
        assertEquals(2, store.previous(routes, 0, url -> true));
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("b", true);
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.next(routes, 0, url -> true));
        // 保存或解除内容不符标记并立即持久化。
        store.setBlocked("a", true);
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.next(routes, 0, url -> true));
        // 反向选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.previous(routes, 0, url -> true));
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.next(List.of("a"), 0, url -> true));
        // 有限选择未被标记且符合条件的备用线路。
        assertEquals(-1, store.next(List.of(), 0, url -> true));
    }
}
