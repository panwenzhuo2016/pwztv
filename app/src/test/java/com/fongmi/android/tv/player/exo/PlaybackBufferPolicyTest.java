package com.fongmi.android.tv.player.exo;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlaybackBufferPolicyTest {
    // 反查缓存时长合法且低内存设备始终保留较小额度。
    @Test public void boundsMemoryAndDurationsForEverySetting() {
        for (boolean constrained : new boolean[]{true, false}) {
            for (int multiplier : new int[]{Integer.MIN_VALUE, 0, 1, 2, 10, Integer.MAX_VALUE}) {
                // 增加恢复缓冲并限制点播缓存时长与内存。
                PlaybackBufferPolicy policy = PlaybackBufferPolicy.forDevice(multiplier, constrained);
                assertEquals(2500, policy.startupMs());
                assertEquals(8000, policy.rebufferMs());
                assertTrue(policy.minimumMs() >= policy.rebufferMs());
                assertTrue(policy.maximumMs() >= policy.minimumMs());
                assertTrue(policy.maximumMs() <= (constrained ? 60_000 : 120_000));
                assertEquals((constrained ? 12 : 32) * 1024 * 1024, policy.targetBytes());
            }
        }
    }
}
