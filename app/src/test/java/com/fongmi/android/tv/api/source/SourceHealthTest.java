package com.fongmi.android.tv.api.source;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class SourceHealthTest {
    // 反查失败冷却与到期后恢复状态。
    @Test public void cooldownExpiresAndSuccessClearsFailure() {
        AtomicLong time = new AtomicLong(100);
        // 创建有容量与时间边界的来源状态表。
        SourceHealth health = new SourceHealth(10, 2, time::get);
        // 记录失败并淘汰最早的状态。
        health.fail("a");
        // 冷却结束后允许重新尝试来源。
        assertFalse(health.available("a"));
        time.set(110);
        // 冷却结束后允许重新尝试来源。
        assertTrue(health.available("a"));
        // 记录失败并淘汰最早的状态。
        health.fail("a");
        // 成功播放后清除对应失败记录。
        health.success("a");
        // 冷却结束后允许重新尝试来源。
        assertTrue(health.available("a"));
    }

    // 反查容量淘汰与有限环形线路选择。
    @Test public void skipsFailedRoutesAndNeverRepeatsExhaustedRound() {
        AtomicLong time = new AtomicLong();
        // 创建有容量与时间边界的来源状态表。
        SourceHealth health = new SourceHealth(10, 2, time::get);
        // 记录失败并淘汰最早的状态。
        health.fail("a");
        // 记录失败并淘汰最早的状态。
        health.fail("b");
        // 记录失败并淘汰最早的状态。
        health.fail("c");
        // 冷却结束后允许重新尝试来源。
        assertTrue(health.available("a"));
        List<String> routes = List.of("a$one", "b$two", "c$three");
        // 有限遍历后选择下一条可用线路。
        assertEquals(0, health.next(routes, 2));
        time.set(100);
        // 有限遍历并跳过本轮已失败的线路。
        assertEquals(-1, health.next(routes, 2, Set.of("a", "b", "c")));
        // 有限遍历后选择下一条可用线路。
        assertEquals(-1, health.next(List.of("a"), 0));
        // 有限遍历后选择下一条可用线路。
        assertEquals(-1, health.next(List.of(), 0));
    }
}
