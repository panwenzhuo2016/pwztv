package com.fongmi.android.tv.ui.custom;

import java.util.concurrent.Executor;

// 仅限主线程调用，队列必须异步执行刷新。
public final class HomeRefresh {

    private final Executor mainQueue;
    private final Runnable refresh;
    private long generation;
    private boolean pending;

    // 绑定主线程队列与首页刷新操作。
    public HomeRefresh(Executor mainQueue, Runnable refresh) {
        this.mainQueue = mainQueue;
        this.refresh = refresh;
    }

    // 当前绘制结束后刷新，同帧重复请求合并。
    public void request() {
        if (pending) return;
        pending = true;
        long id = ++generation;
        mainQueue.execute(() -> {
            if (id != generation) return;
            pending = false;
            refresh.run();
        });
    }

    // 页面销毁时使未执行的刷新失效。
    public void cancel() {
        generation++;
        pending = false;
    }
}
