package com.fongmi.android.tv.ui.custom;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class HomeRefreshTest {

    // 模拟绘制中触发刷新，反查旧页面不会被移除。
    @Test public void keepsChildrenUntilDrawingFinishes() {
        Queue<Runnable> mainQueue = new ArrayDeque<>();
        List<String> children = new ArrayList<>(Arrays.asList("home", "category"));
        AtomicInteger loads = new AtomicInteger();
        // 绑定主线程队列与首页刷新操作。
        HomeRefresh refresh = new HomeRefresh(mainQueue::add, () -> {
            children.clear();
            loads.incrementAndGet();
        });
        int childCount = children.size();
        for (int index = 0; index < childCount; index++) {
            assertNotNull(children.get(index));
            if (index == 0) {
                // 当前绘制结束后刷新，同帧重复请求合并。
                refresh.request();
            }
        }
        assertEquals(Arrays.asList("home", "category"), children);
        assertEquals(0, loads.get());
        mainQueue.remove().run();
        assertTrue(children.isEmpty());
        assertEquals(1, loads.get());
    }

    // 同帧多次刷新仅入队一次，执行后允许再次刷新。
    @Test public void coalescesPendingRequestsAndAllowsNextRefresh() {
        Queue<Runnable> mainQueue = new ArrayDeque<>();
        AtomicInteger loads = new AtomicInteger();
        // 绑定主线程队列与首页刷新操作。
        HomeRefresh refresh = new HomeRefresh(mainQueue::add, loads::incrementAndGet);
        for (int index = 0; index < 5; index++) {
            // 当前绘制结束后刷新，同帧重复请求合并。
            refresh.request();
        }
        assertEquals(1, mainQueue.size());
        mainQueue.remove().run();
        assertEquals(1, loads.get());
        // 当前绘制结束后刷新，同帧重复请求合并。
        refresh.request();
        mainQueue.remove().run();
        assertEquals(2, loads.get());
    }

    // 退出页面后排队回调不得继续重建首页。
    @Test public void cancelsRefreshWhenViewIsDestroyed() {
        Queue<Runnable> mainQueue = new ArrayDeque<>();
        AtomicInteger loads = new AtomicInteger();
        // 绑定主线程队列与首页刷新操作。
        HomeRefresh refresh = new HomeRefresh(mainQueue::add, loads::incrementAndGet);
        // 当前绘制结束后刷新，同帧重复请求合并。
        refresh.request();
        // 页面销毁时使未执行的刷新失效。
        refresh.cancel();
        mainQueue.remove().run();
        assertEquals(0, loads.get());
        assertTrue(mainQueue.isEmpty());
    }

    // 旧回调失效后不能干扰新页面的刷新请求。
    @Test public void staleCallbackCannotRunOrClearNewRequest() {
        Queue<Runnable> mainQueue = new ArrayDeque<>();
        AtomicInteger loads = new AtomicInteger();
        // 绑定主线程队列与首页刷新操作。
        HomeRefresh refresh = new HomeRefresh(mainQueue::add, loads::incrementAndGet);
        // 当前绘制结束后刷新，同帧重复请求合并。
        refresh.request();
        // 页面销毁时使未执行的刷新失效。
        refresh.cancel();
        // 当前绘制结束后刷新，同帧重复请求合并。
        refresh.request();
        mainQueue.remove().run();
        assertEquals(0, loads.get());
        // 当前绘制结束后刷新，同帧重复请求合并。
        refresh.request();
        assertEquals(1, mainQueue.size());
        mainQueue.remove().run();
        assertEquals(1, loads.get());
    }
}
