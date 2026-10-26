package com.fongmi.android.tv.utils;

import android.util.Log;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FailureGuardTest {
    // 验证可恢复错误不逃逸且后续任务仍能执行。
    @Test public void isolatesFailuresAndAllowsFollowingWork() {
        AtomicInteger completed = new AtomicInteger();
        try (MockedStatic<Log> logs = mockStatic(Log.class)) {
            for (Throwable error : new Throwable[]{new IllegalStateException("callback"), new NoSuchMethodError("old Android")}) {
                // 隔离可恢复异常并返回是否完成当前操作。
                assertFalse(FailureGuard.run("test", () -> {
                    if (error instanceof RuntimeException runtime) throw runtime;
                    throw (LinkageError) error;
                }));
                logs.verify(() -> Log.w("recovery", "operation=test", error));
            }
            // 隔离可恢复异常并返回是否完成当前操作。
            assertTrue(FailureGuard.run("next", completed::incrementAndGet));
            assertEquals(1, completed.get());
        }
    }

    // 验证内存耗尽等致命错误不会被误吞。
    @Test public void fatalErrorsRemainFatal() {
        OutOfMemoryError fatal = new OutOfMemoryError("test only");
        // 隔离可恢复异常并返回是否完成当前操作。
        assertSame(fatal, assertThrows(OutOfMemoryError.class, () -> FailureGuard.run("fatal", () -> { throw fatal; })));
    }
}
