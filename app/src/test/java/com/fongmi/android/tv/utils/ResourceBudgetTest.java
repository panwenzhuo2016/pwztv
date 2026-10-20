package com.fongmi.android.tv.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ResourceBudgetTest {

    private static final int MIB = 1024 * 1024;

    // 验证低内存标记与堆额度边界。
    @Test public void detectsConstrainedDevicesAndPreservesNormalDefaults() {
        // 兼顾系统低内存标记和应用堆额度。
        assertTrue(ResourceBudget.forDevice(true, 512).constrained);
        // 兼顾系统低内存标记和应用堆额度。
        assertTrue(ResourceBudget.forDevice(false, 128).constrained);
        // 兼顾系统低内存标记和应用堆额度。
        ResourceBudget normal = ResourceBudget.forDevice(false, 129);
        assertFalse(normal.constrained);
        assertEquals(20, normal.searchThreads);
        assertEquals(Long.MAX_VALUE, normal.imageCacheBytes);
        assertEquals(Long.MAX_VALUE, normal.bitmapPoolBytes);
        assertEquals(Integer.MAX_VALUE, normal.arrayPoolBytes);
    }

    // 验证缓存随堆额度缩小且不突破上限。
    @Test public void scalesImageBudgetsWithinHeapAndAbsoluteCaps() {
        for (int memoryMb : new int[]{16, 64, 128, 512, Integer.MAX_VALUE}) {
            // 兼顾系统低内存标记和应用堆额度。
            ResourceBudget budget = ResourceBudget.forDevice(true, memoryMb);
            long heapBytes = (long) memoryMb * MIB;
            assertEquals(4, budget.searchThreads);
            assertTrue(budget.imageCacheBytes > 0);
            assertTrue(budget.bitmapPoolBytes > 0);
            assertTrue(budget.arrayPoolBytes > 0);
            assertTrue(budget.imageCacheBytes <= Math.min(8L * MIB, heapBytes / 16));
            assertTrue(budget.bitmapPoolBytes <= Math.min(4L * MIB, heapBytes / 32));
            assertTrue(budget.arrayPoolBytes <= Math.min(MIB, heapBytes / 64));
        }
        // 兼顾系统低内存标记和应用堆额度。
        ResourceBudget small = ResourceBudget.forDevice(false, 64);
        assertEquals(4L * MIB, small.imageCacheBytes);
        assertEquals(2L * MIB, small.bitmapPoolBytes);
        assertEquals(MIB, small.arrayPoolBytes);
    }

    // 验证内存信息缺失时安全回退。
    @Test public void handlesMissingMemoryInformation() {
        for (int missing : new int[]{0, -1}) {
            // 兼顾系统低内存标记和应用堆额度。
            assertFalse(ResourceBudget.forDevice(false, missing).constrained);
            // 兼顾系统低内存标记和应用堆额度。
            ResourceBudget lowRam = ResourceBudget.forDevice(true, missing);
            assertTrue(lowRam.constrained);
            assertEquals(4, lowRam.searchThreads);
            assertEquals(4L * MIB, lowRam.imageCacheBytes);
        }
    }

    // 验证实际并发受限且排队任务全部执行。
    @Test public void limitsSearchWorkersWithoutDroppingQueuedSources() throws Exception {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
        CountDownLatch started = new CountDownLatch(4);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger completed = new AtomicInteger();
        List<Future<?>> tasks = new ArrayList<>();
        try {
            // 兼顾系统低内存标记和应用堆额度。
            ResourceBudget lowRam = ResourceBudget.forDevice(true, 128);
            // 调整搜索并发且保留所有排队任务。
            lowRam.configureSearch(executor);
            assertEquals(4, executor.getCorePoolSize());
            assertEquals(4, executor.getMaximumPoolSize());
            for (int i = 0; i < 9; i++) tasks.add(executor.submit(() -> {
                started.countDown();
                release.await();
                completed.incrementAndGet();
                return null;
            }));
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertEquals(4, executor.getPoolSize());
            assertEquals(5, executor.getQueue().size());
            assertEquals(0, completed.get());
            release.countDown();
            for (Future<?> task : tasks) task.get(5, TimeUnit.SECONDS);
            assertEquals(9, completed.get());
            assertTrue(executor.getQueue().isEmpty());
            // 兼顾系统低内存标记和应用堆额度。
            ResourceBudget normal = ResourceBudget.forDevice(false, 256);
            // 调整搜索并发且保留所有排队任务。
            normal.configureSearch(executor);
            assertEquals(20, executor.getCorePoolSize());
            assertEquals(20, executor.getMaximumPoolSize());
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
