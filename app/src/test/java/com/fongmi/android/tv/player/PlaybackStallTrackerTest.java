package com.fongmi.android.tv.player;

import androidx.media3.common.Player;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.fongmi.android.tv.player.PlaybackStallTracker.Decision.*;
import static org.junit.Assert.*;

public class PlaybackStallTrackerTest {
    private final AtomicLong clock = new AtomicLong();
    // 使用单调时钟统计实际播放后的卡顿。
    private final PlaybackStallTracker tracker = new PlaybackStallTracker(clock::get);

    // 推进时间并反查播放器状态对应的恢复决定。
    private PlaybackStallTracker.Decision sample(long time, int state) {
        clock.set(time);
        // 识别反复卡顿及长卡顿并只触发一次恢复。
        return tracker.update(state, true, true);
    }

    // 首次加载及不足两秒的波动不会触发换线。
    @Test public void ignoresInitialLoadAndBriefBuffering() {
        assertEquals(NONE, sample(0, Player.STATE_BUFFERING));
        assertEquals(NONE, sample(30_000, Player.STATE_BUFFERING));
        assertEquals(NONE, sample(31_000, Player.STATE_READY));
        for (long time = 32_000; time < 40_000; time += 2000) {
            assertEquals(NONE, sample(time, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(time + 1999, Player.STATE_READY));
        }
    }

    // 一分钟内三次有效卡顿只触发一次换线。
    @Test public void switchesOnceAfterThreeStallsWithoutCountingDuplicateStates() {
        sample(0, Player.STATE_READY);
        for (int count = 1; count <= 3; count++) {
            long start = count * 5000L;
            assertEquals(NONE, sample(start, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(start + 1000, Player.STATE_BUFFERING));
            assertEquals(count == 3 ? SWITCH : NONE, sample(start + 2000, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(start + 3000, Player.STATE_READY));
        }
        assertEquals(NONE, sample(40_000, Player.STATE_BUFFERING));
    }

    // 旧卡顿过期且单次连续十五秒仍可换线。
    @Test public void expiresOldStallsAndDetectsLongStall() {
        sample(0, Player.STATE_READY);
        sample(1000, Player.STATE_BUFFERING);
        sample(3000, Player.STATE_READY);
        sample(5000, Player.STATE_BUFFERING);
        sample(7000, Player.STATE_READY);
        assertEquals(NONE, sample(70_000, Player.STATE_BUFFERING));
        assertEquals(NONE, sample(72_000, Player.STATE_BUFFERING));
        assertEquals(NONE, sample(84_999, Player.STATE_BUFFERING));
        assertEquals(SWITCH, sample(85_000, Player.STATE_BUFFERING));
    }

    // 暂停直播或主动重置后不沿用之前的卡顿。
    @Test public void resetAndIneligiblePlaybackDiscardPreviousStalls() {
        for (boolean explicitReset : new boolean[]{false, true}) {
            sample(0, Player.STATE_READY);
            sample(1000, Player.STATE_BUFFERING);
            sample(3000, Player.STATE_READY);
            sample(4000, Player.STATE_BUFFERING);
            sample(6000, Player.STATE_READY);
            if (explicitReset) {
                // 新媒体或用户操作后清除卡顿统计。
                tracker.reset();
            } else {
                // 识别反复卡顿及长卡顿并只触发一次恢复。
                assertEquals(NONE, tracker.update(Player.STATE_READY, false, true));
            }
            assertEquals(NONE, sample(7000, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(30_000, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(31_000, Player.STATE_READY));
            assertEquals(NONE, sample(32_000, Player.STATE_BUFFERING));
            assertEquals(NONE, sample(34_000, Player.STATE_READY));
            // 新媒体或用户操作后清除卡顿统计。
            tracker.reset();
        }
    }

    // 关闭换线时仍能在连续稳定一分钟后恢复状态。
    @Test public void honorsDisabledRecoveryAndRequiresContinuousStablePlayback() {
        sample(0, Player.STATE_READY);
        sample(1000, Player.STATE_BUFFERING);
        clock.set(30_000);
        // 识别反复卡顿及长卡顿并只触发一次恢复。
        assertEquals(NONE, tracker.update(Player.STATE_BUFFERING, true, false));
        for (long time : new long[]{31_000, 90_999, 91_000, 92_000}) {
            clock.set(time);
            // 识别反复卡顿及长卡顿并只触发一次恢复。
            assertEquals(time == 91_000 ? STABLE : NONE, tracker.update(Player.STATE_READY, true, false));
        }
    }
}
