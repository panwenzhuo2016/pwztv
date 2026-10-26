package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.Constant;

import org.junit.Test;
import org.mockito.MockedStatic;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HomeLoaderTest {
    // 验证建请求及校验失败后仍换源并清理旧任务。
    @Test public void requestAndValidationFailuresDoNotStopFallback() throws Exception {
        List<String> failed = new ArrayList<>();
        Request invalid = new Request(() -> "invalid");
        Request good = new Request(() -> "videos");
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, this::completed, value -> {
            if (value.equals("invalid")) throw new NoSuchMethodError("validator API");
            return true;
        });
        // 超时或空内容即换源并取消上一来源请求。
        var loaded = loader.load(List.of("broken", "invalid", "good"), key -> {
            if (key.equals("broken")) throw new IllegalArgumentException("source config");
            return key.equals("invalid") ? invalid : good;
        }, () -> true, (key, reason) -> failed.add(key));
        assertEquals(List.of("broken", "invalid"), failed);
        assertEquals("good", loaded.source());
        assertEquals("videos", loaded.value());
        assertTrue(invalid.canceled);
        assertTrue(good.canceled);
    }

    // 验证网络清理抛错后仍取消任务并加载下一源。
    @Test public void brokenCancellationStillCancelsFutureAndLoadsNextSource() throws Exception {
        HomeLoader.Request<String> request = mock(HomeLoader.Request.class);
        doThrow(new IllegalStateException("cancel transport")).when(request).cancel();
        Future<String> timeout = mock(Future.class);
        when(timeout.get(anyLong(), any())).thenThrow(new TimeoutException());
        Request good = new Request(() -> "videos");
        try (MockedStatic<Log> logs = mockStatic(Log.class)) {
            // 配置单源首页总耗时上限和内容校验。
            HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME,
                    task -> task == request ? timeout : completed(task), value -> true);
            // 超时或空内容即换源并取消上一来源请求。
            var loaded = loader.load(List.of("broken", "good"), key -> key.equals("broken") ? request : good,
                    () -> true, (key, reason) -> assertEquals("broken", key));
            verify(request).cancel();
            verify(timeout).cancel(true);
            assertEquals("videos", loaded.value());
            assertTrue(good.canceled);
        }
    }

    private static class Request implements HomeLoader.Request<String> {
        final Callable<String> content;
        boolean canceled;

        // 创建可反查取消状态的测试请求。
        Request(Callable<String> content) { this.content = content; }

        // 执行预设的来源响应。
        @Override public String call() throws Exception { return content.call(); }

        // 取消当前来源的网络请求。
        @Override public void cancel() { canceled = true; }
    }

    // 同步执行普通响应以稳定验证换源顺序。
    private Future<String> completed(Callable<String> request) {
        FutureTask<String> task = new FutureTask<>(request);
        task.run();
        return task;
    }

    // 反查八秒总时限并确认先取消旧源再启动下一源。
    @Test public void enforcesEightSecondsAndCancelsBeforeNextSource() throws Exception {
        // 创建可反查取消状态的测试请求。
        Request slow = new Request(() -> "late");
        // 创建可反查取消状态的测试请求。
        Request fast = new Request(() -> "videos");
        List<String> failures = new ArrayList<>();
        FutureTask<String> timeout = new FutureTask<>(slow) {
            // 模拟达到首页截止时间而不让测试等待八秒。
            @Override public String get(long duration, TimeUnit unit) throws TimeoutException {
                assertEquals(8000, unit.toMillis(duration));
                throw new TimeoutException();
            }
        };
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, request -> {
            if (request == slow) return timeout;
            assertTrue(slow.canceled);
            assertTrue(timeout.isCancelled());
            // 同步执行普通响应以稳定验证换源顺序。
            return completed(request);
        }, value -> !value.isEmpty());
        // 超时或空内容即换源并取消上一来源请求。
        var loaded = loader.load(List.of("sony", "next"), key -> key.equals("sony") ? slow : fast,
                () -> true, (key, reason) -> failures.add(key + ":" + reason));
        assertEquals("next", loaded.source());
        assertEquals("videos", loaded.value());
        assertEquals(List.of("sony:timeout"), failures);
        assertTrue(fast.canceled);
    }

    // 反查空内容及异常均换源且每源只尝试一次。
    @Test public void skipsEmptyAndFailedSourcesThenStopsOnSuccess() throws Exception {
        List<String> attempted = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, this::completed, value -> !value.isEmpty());
        // 超时或空内容即换源并取消上一来源请求。
        var loaded = loader.load(List.of("empty", "broken", "good", "unused"), key -> {
            attempted.add(key);
            // 创建可反查取消状态的测试请求。
            return new Request(() -> {
                if (key.equals("broken")) throw new IllegalStateException("invalid response");
                return key.equals("empty") ? "" : "videos";
            });
        }, () -> true, (key, reason) -> failed.add(key));
        assertEquals(List.of("empty", "broken", "good"), attempted);
        assertEquals(List.of("empty", "broken"), failed);
        assertEquals("good", loaded.source());
    }

    // 反查轮转去重并跳过冷却来源且手选来源可重试。
    @Test public void rotatesOnceAndHonorsAvailabilityForFallbacks() {
        // 按当前来源起点轮转且同轮仅尝试一次。
        assertEquals(List.of("b", "d", "a"), HomeLoader.ordered(List.of("a", "b", "c", "d", "a"), "b", key -> !key.equals("c")));
        // 按当前来源起点轮转且同轮仅尝试一次。
        assertEquals(List.of("b", "a"), HomeLoader.ordered(List.of("a", "b"), "b", key -> !key.equals("b")));
        // 按当前来源起点轮转且同轮仅尝试一次。
        assertTrue(HomeLoader.ordered(List.of(), null, key -> true).isEmpty());
    }

    // 反查全部失败后停止而非循环重试或返回空成功。
    @Test public void exhaustsCandidatesAndReturnsExplicitFailure() throws Exception {
        List<String> failed = new ArrayList<>();
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, this::completed, value -> !value.isEmpty());
        // 超时或空内容即换源并取消上一来源请求。
        var loaded = loader.load(List.of("a", "b"), key -> {
            // 创建可反查取消状态的测试请求。
            return new Request(() -> "");
        }, () -> true, (key, reason) -> failed.add(key));
        assertNull(loaded);
        assertEquals(List.of("a", "b"), failed);
    }

    // 反查手动切源后迟到成功不能发布或继续换源。
    @Test public void discardsLateSuccessWhenUserReplacesRequest() {
        AtomicBoolean current = new AtomicBoolean(true);
        // 创建可反查取消状态的测试请求。
        Request late = new Request(() -> { current.set(false); return "late videos"; });
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, this::completed, value -> true);
        // 超时或空内容即换源并取消上一来源请求。
        assertThrows(CancellationException.class, () -> loader.load(List.of("old", "next"), key -> {
            assertEquals("old", key);
            return late;
        }, current::get, (key, reason) -> fail("Replaced requests must not mark sources failed")));
        assertTrue(late.canceled);
    }

    // 反查生命周期中断会取消连接及任务且不尝试下一源。
    @Test public void cancelsTransportWhenCoordinatorIsInterrupted() {
        // 创建可反查取消状态的测试请求。
        Request request = new Request(() -> "videos");
        FutureTask<String> interrupted = new FutureTask<>(request) {
            // 模拟协调线程被页面销毁或新请求中断。
            @Override public String get(long duration, TimeUnit unit) throws InterruptedException {
                throw new InterruptedException();
            }
        };
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<String, String> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, task -> interrupted, value -> true);
        // 超时或空内容即换源并取消上一来源请求。
        assertThrows(InterruptedException.class, () -> loader.load(List.of("old", "next"), key -> request,
                () -> true, (key, reason) -> fail("Cancellation must not mark sources failed")));
        assertTrue(request.canceled);
        assertTrue(interrupted.isCancelled());
    }
}
