package com.fongmi.android.tv.player;

import com.fongmi.android.tv.App;

public final class PlaybackRetry {
    public static final long DELAY_MS = 3000;
    private enum Intent { PLAY, PAUSE, CLOSED }
    private Intent intent = Intent.PLAY;
    private boolean foreground = true;
    private Runnable action, pending;
    private long delay = DELAY_MS, generation;

    // 延迟启动下一轮且始终只保留一个重试任务。
    public void schedule(Runnable action) {
        // 按指定等待时间保留唯一的下一轮任务。
        schedule(action, DELAY_MS);
    }

    // 按指定等待时间保留唯一的下一轮任务。
    public void schedule(Runnable action, long delay) {
        if (intent == Intent.CLOSED) return;
        // 取消旧回调并使已出队的旧任务失效。
        unschedule();
        this.action = action;
        this.delay = Math.max(DELAY_MS, delay);
        // 仅在前台且用户允许播放时安排重试。
        enqueue();
    }

    // 判断是否允许自动恢复当前播放。
    public boolean active() {
        return intent == Intent.PLAY && foreground;
    }

    // 用户暂停时保留恢复入口但不发起请求。
    public void pause() {
        if (intent == Intent.CLOSED) return;
        intent = Intent.PAUSE;
        // 取消旧回调并使已出队的旧任务失效。
        unschedule();
    }

    // 用户继续播放时恢复等待中的重试。
    public boolean resume() {
        if (intent == Intent.CLOSED) return false;
        intent = Intent.PLAY;
        // 仅在前台且用户允许播放时安排重试。
        enqueue();
        return action != null;
    }

    // 页面离开时停止请求且不覆盖用户暂停状态。
    public void foreground(boolean foreground) {
        this.foreground = foreground;
        // 取消旧回调并使已出队的旧任务失效。
        unschedule();
        // 仅在前台且用户允许播放时安排重试。
        enqueue();
    }

    // 成功播放或切换目标后清除旧重试。
    public void cancel() {
        action = null;
        // 取消旧回调并使已出队的旧任务失效。
        unschedule();
    }

    // 页面销毁后拒绝任何迟到的重试请求。
    public void close() {
        intent = Intent.CLOSED;
        // 成功播放或切换目标后清除旧重试。
        cancel();
    }

    // 仅在前台且用户允许播放时安排重试。
    private void enqueue() {
        // 判断是否允许自动恢复当前播放。
        if (!active() || action == null || pending != null) return;
        long token = generation;
        pending = () -> {
            // 判断是否允许自动恢复当前播放。
            if (token != generation || !active()) return;
            Runnable work = action;
            pending = action = null;
            work.run();
        };
        App.post(pending, delay);
    }

    // 取消旧回调并使已出队的旧任务失效。
    private void unschedule() {
        generation++;
        if (pending != null) App.removeCallbacks(pending);
        pending = null;
    }
}
