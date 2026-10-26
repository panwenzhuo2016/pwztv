package com.fongmi.android.tv.player;

import androidx.media3.common.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

public final class PlaybackStallTracker {
    public static final long WINDOW_MS = 60_000;
    public static final long MIN_STALL_MS = 2000;
    public static final long LONG_STALL_MS = 15_000;
    private static final int MAX_STALLS = 3;
    private final LongSupplier clock;
    private final Deque<Long> stalls = new ArrayDeque<>();
    private long bufferingSince = -1, readySince = -1;
    private boolean played, counted, triggered, stable;

    public enum Decision { NONE, SWITCH, STABLE }

    // 使用单调时钟统计实际播放后的卡顿。
    public PlaybackStallTracker(LongSupplier clock) {
        this.clock = clock;
    }

    // 识别反复卡顿及长卡顿并只触发一次恢复。
    public Decision update(int state, boolean eligible, boolean recover) {
        if (!eligible || (state != Player.STATE_READY && state != Player.STATE_BUFFERING)) {
            // 新媒体或用户操作后清除卡顿统计。
            reset();
            return Decision.NONE;
        }
        if (triggered) return Decision.NONE;
        long now = clock.getAsLong();
        while (!stalls.isEmpty() && now - stalls.peekFirst() > WINDOW_MS) stalls.removeFirst();
        if (bufferingSince >= 0 && !counted && now - bufferingSince >= MIN_STALL_MS) {
            stalls.addLast(now);
            counted = true;
        }
        if (recover && (stalls.size() >= MAX_STALLS || bufferingSince >= 0 && now - bufferingSince >= LONG_STALL_MS)) {
            triggered = true;
            return Decision.SWITCH;
        }
        if (state == Player.STATE_BUFFERING) {
            readySince = -1;
            stable = false;
            if (played && bufferingSince < 0) {
                bufferingSince = now;
                counted = false;
            }
        } else {
            played = true;
            bufferingSince = -1;
            if (readySince < 0) readySince = now;
            if (!stable && now - readySince >= WINDOW_MS) {
                stable = true;
                stalls.clear();
                return Decision.STABLE;
            }
        }
        return Decision.NONE;
    }

    // 新媒体或用户操作后清除卡顿统计。
    public void reset() {
        stalls.clear();
        bufferingSince = readySince = -1;
        played = counted = triggered = stable = false;
    }
}
