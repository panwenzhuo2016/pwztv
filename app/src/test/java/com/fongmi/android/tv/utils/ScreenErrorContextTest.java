package com.fongmi.android.tv.utils;

import org.junit.Test;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class ScreenErrorContextTest {
    // 验证原始堆栈只在匹配显示时取出一次。
    @Test public void retrievesMatchingStackOnceAndExpiresUnshownErrors() {
        AtomicLong now = new AtomicLong(10_000_000);
        // 注入时钟以限制待显示异常的保留时间。
        ScreenErrorContext context = new ScreenErrorContext(now::get);
        // 短暂关联提示和原始堆栈，限制总容量。
        context.remember("shown", "original stack");
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("other"));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertEquals("original stack", context.take("shown"));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("shown"));
        // 短暂关联提示和原始堆栈，限制总容量。
        context.remember("not shown", "expired stack");
        now.addAndGet(TimeUnit.MINUTES.toMillis(1) + 1);
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("not shown"));
    }

    // 验证未显示上下文有条数与总容量限制。
    @Test public void boundsPendingCountAndCharacters() {
        // 注入时钟以限制待显示异常的保留时间。
        ScreenErrorContext context = new ScreenErrorContext(() -> 10_000_000);
        for (int i = 0; i < 10; i++) {
            // 短暂关联提示和原始堆栈，限制总容量。
            context.remember("error-" + i, "stack-" + i);
        }
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("error-0"));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("error-1"));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertEquals("stack-9", context.take("error-9"));
        for (int i = 0; i < 4; i++) {
            // 短暂关联提示和原始堆栈，限制总容量。
            context.remember("large-" + i, "x".repeat(30_000));
        }
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("large-0"));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertEquals("x".repeat(30_000), context.take("large-3"));
        // 短暂关联提示和原始堆栈，限制总容量。
        context.remember("oversized", "x".repeat(RecentLogBuffer.MAX_CHARS + 1));
        // 实际显示时取出堆栈，避免后台误记完整日志。
        assertNull(context.take("oversized"));
    }

    // 验证域名解析异常也能保留原始调用链。
    @Test public void includesUnknownHostStack() {
        // 序列化原始堆栈且不忽略域名解析异常。
        String details = ScreenErrorContext.stack(new UnknownHostException("video.example"));
        assertTrue(details.contains("UnknownHostException: video.example"));
        assertTrue(details.contains("ScreenErrorContextTest.java:"));
    }
}
