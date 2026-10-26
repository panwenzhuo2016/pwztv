package com.fongmi.android.tv.player;

import android.os.SystemClock;

import androidx.media3.common.Player;

import com.fongmi.android.tv.App;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VodPlaybackWatchdogTest {
    // 反查三次卡顿后仅回调一次并停止旧观察任务。
    @Test public void pollsOneTaskAndDeliversStallRecovery() {
        AtomicLong clock = new AtomicLong();
        AtomicInteger state = new AtomicInteger(Player.STATE_READY);
        Map<Runnable, Long> pending = new LinkedHashMap<>();
        Player player = mock(Player.class);
        when(player.getPlaybackState()).thenAnswer(call -> state.get());
        when(player.getPlayWhenReady()).thenReturn(true);
        when(player.isPlaying()).thenReturn(true);
        Runnable stalled = mock(Runnable.class), stable = mock(Runnable.class);
        try (MockedStatic<App> app = mockStatic(App.class); MockedStatic<SystemClock> time = mockStatic(SystemClock.class)) {
            time.when(SystemClock::elapsedRealtime).thenAnswer(call -> clock.get());
            app.when(() -> App.post(any(Runnable.class), anyLong())).thenAnswer(call -> {
                pending.put(call.getArgument(0), call.getArgument(1));
                return null;
            });
            app.when(() -> App.removeCallbacks(any(Runnable.class))).thenAnswer(call -> {
                pending.remove(call.<Runnable>getArgument(0));
                return null;
            });
            // 复用播放器回调执行换线与稳定播放通知。
            VodPlaybackWatchdog watchdog = new VodPlaybackWatchdog(() -> player, stalled, stable);
            // 启用点播缓冲并遵守当前来源的自动换线设置。
            watchdog.configure(true);
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            for (int count = 1; count <= 3; count++) {
                state.set(Player.STATE_BUFFERING);
                clock.set(count * 5000L);
                // 状态变化时启动唯一的主线程观察任务。
                watchdog.changed();
                // 执行唯一待运行任务并验证调度数量。
                runPending(pending);
                clock.addAndGet(2000);
                // 执行唯一待运行任务并验证调度数量。
                runPending(pending);
                if (count < 3) {
                    state.set(Player.STATE_READY);
                    // 状态变化时启动唯一的主线程观察任务。
                    watchdog.changed();
                    // 执行唯一待运行任务并验证调度数量。
                    runPending(pending);
                }
            }
            verify(stalled, times(1)).run();
            verifyNoInteractions(stable);
            assertTrue(pending.isEmpty());
            // 停止观察并排除拖动进度及切集引起的缓冲。
            watchdog.reset();
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            clock.addAndGet(20_000);
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            verify(stalled, times(1)).run();
            // 停止观察并排除拖动进度及切集引起的缓冲。
            watchdog.reset();
            assertTrue(pending.isEmpty());
        }
    }

    // 执行唯一待运行任务并验证调度数量。
    private void runPending(Map<Runnable, Long> pending) {
        assertEquals(1, pending.size());
        Runnable task = pending.keySet().iterator().next();
        pending.remove(task);
        task.run();
    }

    // 反查稳定通知一次且直播及暂停不触发换线。
    @Test public void stablePlaybackCompletesRecoveryAndLiveDoesNotStartTimer() {
        AtomicLong clock = new AtomicLong();
        Map<Runnable, Long> pending = new LinkedHashMap<>();
        Player player = mock(Player.class);
        when(player.getPlaybackState()).thenReturn(Player.STATE_READY);
        when(player.getPlayWhenReady()).thenReturn(true);
        when(player.isPlaying()).thenReturn(true);
        Runnable stalled = mock(Runnable.class), stable = mock(Runnable.class);
        try (MockedStatic<App> app = mockStatic(App.class); MockedStatic<SystemClock> time = mockStatic(SystemClock.class)) {
            time.when(SystemClock::elapsedRealtime).thenAnswer(call -> clock.get());
            app.when(() -> App.post(any(Runnable.class), anyLong())).thenAnswer(call -> {
                pending.put(call.getArgument(0), call.getArgument(1));
                return null;
            });
            // 复用播放器回调执行换线与稳定播放通知。
            VodPlaybackWatchdog watchdog = new VodPlaybackWatchdog(() -> player, stalled, stable);
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            assertTrue(pending.isEmpty());
            // 启用点播缓冲并遵守当前来源的自动换线设置。
            watchdog.configure(true);
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            clock.set(60_000);
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            verify(stable).run();
            when(player.isCurrentMediaItemLive()).thenReturn(true);
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            assertTrue(pending.isEmpty());
            verifyNoInteractions(stalled);
            when(player.isCurrentMediaItemLive()).thenReturn(false);
            when(player.getPlayWhenReady()).thenReturn(false);
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            // 执行唯一待运行任务并验证调度数量。
            runPending(pending);
            assertTrue(pending.isEmpty());
        }
    }
}
