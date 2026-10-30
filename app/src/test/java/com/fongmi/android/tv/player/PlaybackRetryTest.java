package com.fongmi.android.tv.player;

import com.fongmi.android.tv.App;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PlaybackRetryTest {
    // 反查连续百轮重试无任务堆积且至少等待三秒。
    @Test public void repeatedFailuresKeepOneDelayedTask() {
        Map<Runnable, Long> queue = new LinkedHashMap<>();
        try (MockedStatic<App> app = mockStatic(App.class)) {
            // 用可控队列替代主线程延迟回调。
            scheduler(app, queue);
            PlaybackRetry retry = new PlaybackRetry();
            AtomicInteger runs = new AtomicInteger();
            Runnable[] work = new Runnable[1];
            work[0] = () -> {
                runs.incrementAndGet();
                // 延迟启动下一轮且始终只保留一个重试任务。
                retry.schedule(work[0], 1);
            };
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(work[0]);
            for (int i = 0; i < 100; i++) {
                assertEquals(1, queue.size());
                assertEquals(Long.valueOf(3000), queue.values().iterator().next());
                // 执行并移除一条排队任务。
                runNext(queue);
            }
            assertEquals(100, runs.get());
            assertEquals(1, queue.size());
        }
    }

    // 反查替换取消及退出均使迟到的旧回调失效。
    @Test public void staleCallbacksNeverPlayPreviousTarget() {
        Map<Runnable, Long> queue = new LinkedHashMap<>();
        try (MockedStatic<App> app = mockStatic(App.class)) {
            // 用可控队列替代主线程延迟回调。
            scheduler(app, queue);
            PlaybackRetry retry = new PlaybackRetry();
            AtomicInteger old = new AtomicInteger(), current = new AtomicInteger();
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(old::incrementAndGet);
            Runnable stale = queue.keySet().iterator().next();
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(current::incrementAndGet);
            stale.run();
            assertEquals(0, old.get());
            assertEquals(1, queue.size());
            // 执行并移除一条排队任务。
            runNext(queue);
            assertEquals(1, current.get());
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(old::incrementAndGet);
            stale = queue.keySet().iterator().next();
            // 成功播放或切换目标后清除旧重试。
            retry.cancel();
            stale.run();
            assertTrue(queue.isEmpty());
            assertEquals(0, old.get());
            // 页面销毁后拒绝任何迟到的重试请求。
            retry.close();
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(old::incrementAndGet);
            // 用户继续播放时恢复等待中的重试。
            assertFalse(retry.resume());
            // 判断是否允许自动恢复当前播放。
            assertFalse(retry.active());
            assertTrue(queue.isEmpty());
        }
    }

    // 反查暂停和后台不会重试，恢复前台不取消用户暂停。
    @Test public void pauseAndForegroundPreservePlaybackIntent() {
        Map<Runnable, Long> queue = new LinkedHashMap<>();
        try (MockedStatic<App> app = mockStatic(App.class)) {
            // 用可控队列替代主线程延迟回调。
            scheduler(app, queue);
            PlaybackRetry retry = new PlaybackRetry();
            AtomicInteger runs = new AtomicInteger();
            // 延迟启动下一轮且始终只保留一个重试任务。
            retry.schedule(runs::incrementAndGet);
            Runnable stale = queue.keySet().iterator().next();
            // 用户暂停时保留恢复入口但不发起请求。
            retry.pause();
            stale.run();
            // 页面离开时停止请求且不覆盖用户暂停状态。
            retry.foreground(false);
            // 页面离开时停止请求且不覆盖用户暂停状态。
            retry.foreground(true);
            assertTrue(queue.isEmpty());
            assertEquals(0, runs.get());
            // 用户继续播放时恢复等待中的重试。
            assertTrue(retry.resume());
            assertEquals(1, queue.size());
            // 页面离开时停止请求且不覆盖用户暂停状态。
            retry.foreground(false);
            assertTrue(queue.isEmpty());
            // 页面离开时停止请求且不覆盖用户暂停状态。
            retry.foreground(true);
            assertEquals(1, queue.size());
            // 执行并移除一条排队任务。
            runNext(queue);
            assertEquals(1, runs.get());
            assertTrue(queue.isEmpty());
        }
    }

    // 用可控队列替代主线程延迟回调。
    private void scheduler(MockedStatic<App> app, Map<Runnable, Long> queue) {
        app.when(() -> App.post(any(Runnable.class), anyLong())).thenAnswer(call -> {
            queue.put(call.getArgument(0), call.getArgument(1));
            return null;
        });
        app.when(() -> App.removeCallbacks(any(Runnable.class))).thenAnswer(call -> {
            queue.remove(call.getArgument(0));
            return null;
        });
    }

    // 执行并移除一条排队任务。
    private void runNext(Map<Runnable, Long> queue) {
        Runnable work = queue.keySet().iterator().next();
        queue.remove(work);
        work.run();
    }
}
