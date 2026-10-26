package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.utils.FailureGuard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public final class HomeLoader<S, R> {
    private final long timeoutMillis;
    private final Function<Callable<R>, Future<R>> submit;
    private final Predicate<R> usable;

    public interface Request<R> extends Callable<R> {
        // 取消当前来源的网络请求。
        void cancel();
    }

    public record Outcome<S, R>(S source, R value) {}

    // 配置单源首页总耗时上限和内容校验。
    public HomeLoader(long timeoutMillis, Function<Callable<R>, Future<R>> submit, Predicate<R> usable) {
        if (timeoutMillis <= 0) throw new IllegalArgumentException("Home timeout must be positive");
        this.timeoutMillis = timeoutMillis;
        this.submit = submit;
        this.usable = usable;
    }

    // 按当前来源起点轮转且同轮仅尝试一次。
    public static <S> List<S> ordered(List<S> sources, S current, Predicate<S> available) {
        LinkedHashSet<S> ordered = new LinkedHashSet<>();
        if (current != null) ordered.add(current);
        int start = current == null ? -1 : sources.indexOf(current);
        for (int offset = 1; offset <= sources.size(); offset++) {
            S source = sources.get(Math.floorMod(start + offset, sources.size()));
            if (available.test(source)) ordered.add(source);
        }
        return new ArrayList<>(ordered);
    }

    // 超时或空内容即换源并取消上一来源请求。
    public Outcome<S, R> load(List<S> sources, Function<S, Request<R>> requests,
                              BooleanSupplier current, BiConsumer<S, String> failed) throws InterruptedException {
        for (S source : sources) {
            if (!current.getAsBoolean()) throw new CancellationException("Home request replaced");
            Request<R> request = null;
            Future<R> task = null;
            try {
                request = requests.apply(source);
                task = submit.apply(request);
                R value = task.get(timeoutMillis, TimeUnit.MILLISECONDS);
                if (!current.getAsBoolean()) throw new CancellationException("Home request replaced");
                if (value != null && usable.test(value)) return new Outcome<>(source, value);
                failed.accept(source, "empty");
            } catch (TimeoutException e) {
                if (!current.getAsBoolean()) throw new CancellationException("Home request replaced");
                failed.accept(source, "timeout");
            } catch (ExecutionException e) {
                if (!current.getAsBoolean()) throw new CancellationException("Home request replaced");
                failed.accept(source, e.getCause().getClass().getSimpleName());
            } catch (RuntimeException | LinkageError e) {
                if (!current.getAsBoolean()) throw new CancellationException("Home request replaced");
                failed.accept(source, e.getClass().getSimpleName());
            } finally {
                // 取消当前来源的网络请求。
                cancel(request, task);
            }
        }
        return null;
    }

    // 取消当前来源的网络请求。
    private void cancel(Request<R> request, Future<R> task) {
        // 隔离可恢复异常并返回是否完成当前操作。
        if (request != null) FailureGuard.run("home.cancelRequest", request::cancel);
        // 隔离可恢复异常并返回是否完成当前操作。
        if (task != null) FailureGuard.run("home.cancelTask", () -> {
            if (!task.isDone()) task.cancel(true);
        });
    }
}
