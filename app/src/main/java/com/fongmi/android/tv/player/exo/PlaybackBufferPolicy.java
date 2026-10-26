package com.fongmi.android.tv.player.exo;

public record PlaybackBufferPolicy(int minimumMs, int maximumMs, int startupMs, int rebufferMs, int targetBytes) {
    private static final int MIB = 1024 * 1024;
    private static final int STARTUP_MS = 2500;
    private static final int REBUFFER_MS = 8000;
    private static final int MINIMUM_MS = 30_000;
    private static final int MAXIMUM_MS = 60_000;
    private static final int CONSTRAINED_LIMIT_MS = 60_000;
    private static final int NORMAL_LIMIT_MS = 120_000;
    private static final int LIVE_STARTUP_MS = 1500;
    private static final int LIVE_REBUFFER_MS = 3000;
    private static final int LIVE_MINIMUM_MS = 10_000;
    private static final int LIVE_MAXIMUM_MS = 20_000;
    private static final int LIVE_CONSTRAINED_LIMIT_MS = 20_000;
    private static final int LIVE_NORMAL_LIMIT_MS = 30_000;

    // 增加恢复缓冲并限制点播缓存时长与内存。
    public static PlaybackBufferPolicy forDevice(int multiplier, boolean constrained) {
        int factor = Math.max(1, Math.min(10, multiplier));
        int limit = constrained ? CONSTRAINED_LIMIT_MS : NORMAL_LIMIT_MS;
        return new PlaybackBufferPolicy(Math.min(MINIMUM_MS * factor, limit),
                Math.min(MAXIMUM_MS * factor, limit), STARTUP_MS, REBUFFER_MS,
                (constrained ? 12 : 32) * MIB);
    }

    // 直播兼顾恢复速度与缓存额度并限制累计缓冲。
    public static PlaybackBufferPolicy forLiveDevice(int multiplier, boolean constrained) {
        int factor = Math.max(1, Math.min(10, multiplier));
        int limit = constrained ? LIVE_CONSTRAINED_LIMIT_MS : LIVE_NORMAL_LIMIT_MS;
        return new PlaybackBufferPolicy(Math.min(LIVE_MINIMUM_MS * factor, limit),
                Math.min(LIVE_MAXIMUM_MS * factor, limit), LIVE_STARTUP_MS, LIVE_REBUFFER_MS,
                (constrained ? 8 : 16) * MIB);
    }
}
