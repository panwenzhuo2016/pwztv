package com.fongmi.android.tv.player;

import android.os.SystemClock;

import androidx.media3.common.Player;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.FailureGuard;

import java.util.function.Supplier;

public final class VodPlaybackWatchdog {
    private static final long CHECK_INTERVAL_MS = 1000;
    private final Supplier<? extends Player> player;
    private final Runnable stalled, stable, tick;
    // 使用单调时钟统计实际播放后的卡顿。
    private final PlaybackStallTracker tracker = new PlaybackStallTracker(SystemClock::elapsedRealtime);
    private boolean enabled, recover;

    // 复用播放器回调执行换线与稳定播放通知。
    public VodPlaybackWatchdog(Supplier<? extends Player> player, Runnable stalled, Runnable stable) {
        this.player = player;
        this.stalled = stalled;
        this.stable = stable;
        // 检查卡顿且失败时停止当前观察任务。
        tick = this::check;
    }

    // 启用点播缓冲并遵守当前来源的自动换线设置。
    public void configure(boolean recover) {
        enabled = true;
        this.recover = recover;
    }

    // 判断当前播放器是否使用点播优化。
    public boolean enabled() {
        return enabled;
    }

    // 状态变化时启动唯一的主线程观察任务。
    public void changed() {
        if (!enabled) return;
        App.post(tick, 0);
    }

    // 检查卡顿且失败时停止当前观察任务。
    private void check() {
        // 隔离可恢复异常并返回是否完成当前操作。
        if (!FailureGuard.run("vod.bufferWatchdog", () -> {
            Player current = player.get();
            if (current == null) return;
            int state = current.getPlaybackState();
            boolean eligible = current.getPlayWhenReady() && !current.isCurrentMediaItemLive()
                    && (state != Player.STATE_READY || current.isPlaying());
            // 识别反复卡顿及长卡顿并只触发一次恢复。
            PlaybackStallTracker.Decision decision = tracker.update(state, eligible, recover);
            if (decision == PlaybackStallTracker.Decision.SWITCH) {
                stalled.run();
                return;
            }
            if (decision == PlaybackStallTracker.Decision.STABLE) stable.run();
            if (eligible && (state == Player.STATE_READY || state == Player.STATE_BUFFERING)) App.post(tick, CHECK_INTERVAL_MS);
        })) {
            // 停止观察并排除拖动进度及切集引起的缓冲。
            reset();
        }
    }

    // 停止观察并排除拖动进度及切集引起的缓冲。
    public void reset() {
        App.removeCallbacks(tick);
        // 新媒体或用户操作后清除卡顿统计。
        tracker.reset();
    }
}
