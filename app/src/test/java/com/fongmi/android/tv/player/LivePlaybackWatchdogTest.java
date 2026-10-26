package com.fongmi.android.tv.player;

import android.os.SystemClock;

import androidx.media3.common.Player;

import com.fongmi.android.tv.App;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LivePlaybackWatchdogTest {
    // 反查带直播标识及普通流均能累计卡顿并换线一次。
    @Test public void recoversRepeatedStallsForLiveAndUnmarkedStreams() {
        for (boolean liveMetadata : new boolean[]{false, true}) {
            // 创建直播播放器及可控主线程观察环境。
            try (Fixture fixture = new Fixture()) {
                when(fixture.player.isCurrentMediaItemLive()).thenReturn(liveMetadata);
                // 推进播放器状态并执行唯一待运行检查。
                fixture.sample(0, Player.STATE_READY);
                for (int count = 1; count <= 3; count++) {
                    long start = count * 5000L;
                    // 推进播放器状态并执行唯一待运行检查。
                    fixture.sample(start, Player.STATE_BUFFERING);
                    // 推进播放器状态并执行唯一待运行检查。
                    fixture.sample(start + 2000, Player.STATE_BUFFERING);
                    if (count < 3) {
                        // 推进播放器状态并执行唯一待运行检查。
                        fixture.sample(start + 2500, Player.STATE_READY);
                        verifyNoInteractions(fixture.stalled);
                    }
                }
                verify(fixture.stalled, times(1)).run();
                verifyNoInteractions(fixture.stable);
                assertTrue(fixture.pending.isEmpty());
            }
        }
    }

    // 反查首次加载不换线且实际播放后长卡十五秒换线。
    @Test public void ignoresInitialLoadingAndRecoversLongStall() {
        // 创建直播播放器及可控主线程观察环境。
        try (Fixture fixture = new Fixture()) {
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(0, Player.STATE_BUFFERING);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(30_000, Player.STATE_BUFFERING);
            verifyNoInteractions(fixture.stalled);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(31_000, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(32_000, Player.STATE_BUFFERING);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(46_999, Player.STATE_BUFFERING);
            verifyNoInteractions(fixture.stalled);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(47_000, Player.STATE_BUFFERING);
            verify(fixture.stalled).run();
            assertTrue(fixture.pending.isEmpty());
        }
    }

    // 反查暂停及换台清空旧卡顿并在稳定一分钟后通知。
    @Test public void pauseAndChannelChangeDiscardOldStalls() {
        // 创建直播播放器及可控主线程观察环境。
        try (Fixture fixture = new Fixture()) {
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(0, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(1000, Player.STATE_BUFFERING);
            when(fixture.player.getPlayWhenReady()).thenReturn(false);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(20_000, Player.STATE_BUFFERING);
            assertTrue(fixture.pending.isEmpty());
            when(fixture.player.getPlayWhenReady()).thenReturn(true);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(21_000, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(22_000, Player.STATE_BUFFERING);
            // 停止观察并排除拖动进度及切集引起的缓冲。
            fixture.watchdog.reset();
            assertTrue(fixture.pending.isEmpty());
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(40_000, Player.STATE_BUFFERING);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(60_000, Player.STATE_BUFFERING);
            verifyNoInteractions(fixture.stalled);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(61_000, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(120_999, Player.STATE_READY);
            verifyNoInteractions(fixture.stable);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(121_000, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(122_000, Player.STATE_READY);
            verify(fixture.stable, times(1)).run();
        }
    }

    // 反查关闭自动换线不会中断播放且重开后正常恢复。
    @Test public void honorsRecoveryToggleWithoutUsingOldStalls() {
        // 创建直播播放器及可控主线程观察环境。
        try (Fixture fixture = new Fixture()) {
            // 按播放场景观察卡顿并遵守自动换线设置。
            fixture.watchdog.configure(PlaybackMode.LIVE, false);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(0, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(1000, Player.STATE_BUFFERING);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(30_000, Player.STATE_BUFFERING);
            verifyNoInteractions(fixture.stalled);
            assertEquals(1, fixture.pending.size());
            // 按播放场景观察卡顿并遵守自动换线设置。
            fixture.watchdog.configure(PlaybackMode.LIVE, true);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(31_000, Player.STATE_BUFFERING);
            verifyNoInteractions(fixture.stalled);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(32_000, Player.STATE_READY);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(33_000, Player.STATE_BUFFERING);
            // 推进播放器状态并执行唯一待运行检查。
            fixture.sample(48_000, Player.STATE_BUFFERING);
            verify(fixture.stalled).run();
            assertTrue(fixture.pending.isEmpty());
        }
    }

    private static final class Fixture implements AutoCloseable {
        final AtomicLong clock = new AtomicLong();
        final Map<Runnable, Long> pending = new LinkedHashMap<>();
        final Player player = mock(Player.class);
        final Runnable stalled = mock(Runnable.class), stable = mock(Runnable.class);
        final MockedStatic<App> app = mockStatic(App.class);
        final MockedStatic<SystemClock> time = mockStatic(SystemClock.class);
        final PlaybackWatchdog watchdog;

        // 创建直播播放器及可控主线程观察环境。
        Fixture() {
            when(player.getPlayWhenReady()).thenReturn(true);
            when(player.isPlaying()).thenReturn(true);
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
            watchdog = new PlaybackWatchdog(() -> player, stalled, stable);
            // 按播放场景观察卡顿并遵守自动换线设置。
            watchdog.configure(PlaybackMode.LIVE, true);
        }

        // 推进播放器状态并执行唯一待运行检查。
        void sample(long now, int state) {
            clock.set(now);
            when(player.getPlaybackState()).thenReturn(state);
            // 状态变化时启动唯一的主线程观察任务。
            watchdog.changed();
            assertEquals(1, pending.size());
            Runnable task = pending.keySet().iterator().next();
            pending.remove(task);
            task.run();
        }

        // 关闭静态替身并确保不遗留观察任务。
        @Override public void close() {
            // 停止观察并排除拖动进度及切集引起的缓冲。
            watchdog.reset();
            time.close();
            app.close();
            assertTrue(pending.isEmpty());
        }
    }
}
