package com.fongmi.android.tv.utils;

import java.util.concurrent.ThreadPoolExecutor;

public final class ResourceBudget {

    private static final int MIB = 1024 * 1024;
    private static final int LOW_MEMORY_CLASS_MB = 128;
    private static final int UNKNOWN_MEMORY_CLASS_MB = 64;
    private static final int LOW_SEARCH_THREADS = 4;
    public static final int DEFAULT_SEARCH_THREADS = 20;

    public final boolean constrained;
    public final int searchThreads;
    public final long imageCacheBytes;
    public final long bitmapPoolBytes;
    public final int arrayPoolBytes;

    // 按设备内存生成搜索和图片缓存预算。
    private ResourceBudget(boolean constrained, int memoryClassMb) {
        this.constrained = constrained;
        searchThreads = constrained ? LOW_SEARCH_THREADS : DEFAULT_SEARCH_THREADS;
        long heapBytes = (long) (memoryClassMb > 0 ? memoryClassMb : UNKNOWN_MEMORY_CLASS_MB) * MIB;
        imageCacheBytes = constrained ? Math.min(8L * MIB, heapBytes / 16) : Long.MAX_VALUE;
        bitmapPoolBytes = constrained ? Math.min(4L * MIB, heapBytes / 32) : Long.MAX_VALUE;
        arrayPoolBytes = constrained ? (int) Math.min(MIB, heapBytes / 64) : Integer.MAX_VALUE;
    }

    // 兼顾系统低内存标记和应用堆额度。
    public static ResourceBudget forDevice(boolean lowRam, int memoryClassMb) {
        boolean constrained = lowRam || (memoryClassMb > 0 && memoryClassMb <= LOW_MEMORY_CLASS_MB);
        // 按设备内存生成搜索和图片缓存预算。
        return new ResourceBudget(constrained, memoryClassMb);
    }

    // 调整搜索并发且保留所有排队任务。
    public void configureSearch(ThreadPoolExecutor executor) {
        executor.setCorePoolSize(Math.min(executor.getCorePoolSize(), searchThreads));
        executor.setMaximumPoolSize(searchThreads);
        executor.setCorePoolSize(searchThreads);
    }
}
