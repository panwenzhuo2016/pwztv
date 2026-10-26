package com.fongmi.android.tv.utils;

import android.content.Context;
import android.os.Build;
import android.util.AtomicFile;
import android.util.Log;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.BuildConfig;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class DiagnosticLog implements LogAdapter {
    private static final String TAG = "diagnostics";
    private static final int MAX_FILE_BYTES = 2 * 1024 * 1024;
    private static volatile DiagnosticLog instance;
    private final RecentLogBuffer buffer;
    // 注入时钟以限制待显示异常的保留时间。
    private final ScreenErrorContext screenErrors = new ScreenErrorContext(System::currentTimeMillis);
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private final AtomicFile file;
    private String savedSnapshot;

    // 恢复本地日志并定期保存有限快照。
    private DiagnosticLog(Context context) {
        // 注入时钟便于验证一小时保留边界。
        buffer = new RecentLogBuffer(System::currentTimeMillis);
        file = new AtomicFile(new File(context.getFilesDir(), "recent-diagnostics.json"));
        try {
            if (file.getBaseFile().exists()) {
                try (InputStream input = file.openRead()) {
                    // 限量读取日志快照并兼容安卓七以上版本。
                    byte[] bytes = readSnapshot(input);
                    // 恢复仍在一小时范围内的日志快照。
                    buffer.restore(new String(bytes, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | RuntimeException error) {
            Log.w(TAG, "session=restore failed", error);
        }
        // 在后台原子保存已变化的日志快照。
        worker.scheduleWithFixedDelay(this::save, 30, 30, TimeUnit.SECONDS);
    }

    // 限量读取日志快照并兼容安卓七以上版本。
    private static byte[] readSnapshot(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] block = new byte[8192];
        int count;
        while ((count = input.read(block)) != -1) {
            if (bytes.size() + count > MAX_FILE_BYTES) throw new IOException("Diagnostic snapshot too large");
            bytes.write(block, 0, count);
        }
        return bytes.toByteArray();
    }

    // 安装日志适配器并记录当前应用会话。
    public static synchronized void install(Context context) {
        if (instance != null) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("diagnostics.install", () -> {
            // 恢复本地日志并定期保存有限快照。
            instance = new DiagnosticLog(context.getApplicationContext());
            Logger.addLogAdapter(instance);
            // 观察本进程系统提示，失败时保留应用自有入口。
            ToastDiagnostics.install(text -> {
                // 实际显示异常时保留原始信息和显示调用链。
                visible("plugin-toast", text, false);
            });
        });
    }

    // 过滤业务调试日志仅接收警告和异常。
    @Override
    public boolean isLoggable(int priority, String tag) {
        return priority >= Log.WARN;
    }

    // 按日志等级区分业务摘要与完整异常。
    @Override
    public void log(int priority, String tag, String message) {
        // 过滤业务调试日志仅接收警告和异常。
        if (!isLoggable(priority, tag)) return;
        // 后台异常待匹配且诊断失败不影响播放。
        record(tag, message, true);
    }

    // 记录额外诊断事件且不依赖系统日志权限。
    public static void record(String tag, String message) {
        // 后台异常待匹配且诊断失败不影响播放。
        record(tag, message, false);
    }

    // 后台异常待匹配且诊断失败不影响播放。
    public static void record(String tag, String message, boolean error) {
        DiagnosticLog value = instance;
        if (value == null) return;
        try {
            // 去除播放日志前缀后识别真实异常内容。
            if (!DiagnosticError.containsException(message)) return;
            // 兼容旧安卓提取首行并匹配插件异常。
            value.screenErrors.rememberLogged(message);
        } catch (RuntimeException | LinkageError failure) {
            Log.w(TAG, "session=logger diagnostic capture failed", failure);
        }
    }

    // 原始异常仅在对应错误实际显示后写入日志。
    public static void prepareVisible(String message, Throwable error) {
        if (message == null || error == null) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("diagnostics.prepare", () -> {
            // 序列化原始堆栈且不忽略域名解析异常。
            String details = ScreenErrorContext.stack(error);
            DiagnosticLog value = instance;
            if (value != null) {
                // 短暂关联提示和原始堆栈，限制总容量。
                value.screenErrors.remember(message, details);
            }
        });
    }

    // 实际显示异常时保留原始信息和显示调用链。
    public static void visible(String tag, String message, boolean error) {
        // 优先记录事件原始异常并兼容插件文字匹配。
        visible(tag, message, error, null);
    }

    // 优先记录事件原始异常并兼容插件文字匹配。
    public static void visible(String tag, String message, boolean error, Throwable cause) {
        DiagnosticLog value = instance;
        if (value == null || message == null) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("diagnostics.visible", () -> {
            if (message.trim().isEmpty()) return;
            String details;
            if (cause != null) {
                // 序列化原始堆栈且不忽略域名解析异常。
                details = ScreenErrorContext.stack(cause);
            } else {
                // 实际显示时取出堆栈，避免后台误记完整日志。
                details = value.screenErrors.take(message);
            }
            // 识别屏幕错误提示并排除普通成功通知。
            if (!error && details == null && !DiagnosticError.isPopupError(message)) return;
            String original = message + "\n" + (details == null ? "Original exception unavailable; display call site follows.\n" : details);
            // 序列化原始堆栈且不忽略域名解析异常。
            String caller = ScreenErrorContext.stack(new Throwable("Screen display call site"));
            // 仅记录有效异常并保留脱敏后的完整堆栈。
            value.buffer.append(tag, original + caller, true, true);
        });
    }

    // 在后台原子保存已变化的日志快照。
    private void save() {
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("diagnostics.save", this::writeSnapshot);
    }

    // 编码并原子保存快照且回滚失败写入。
    private void writeSnapshot() {
        // 将有限日志编码为可恢复的本地快照。
        String snapshot = buffer.encode();
        if (snapshot.equals(savedSnapshot)) return;
        FileOutputStream output = null;
        try {
            byte[] bytes = snapshot.getBytes(StandardCharsets.UTF_8);
            output = file.startWrite();
            output.write(bytes);
            file.finishWrite(output);
            savedSnapshot = snapshot;
        } catch (IOException | RuntimeException error) {
            if (output != null) file.failWrite(output);
            Log.w(TAG, "session=save failed", error);
        }
    }

    // 后台生成最近一小时日志并复制到剪贴板。
    public static void copyRecent() {
        DiagnosticLog value = instance;
        if (value == null) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("diagnostics.submitCopy", () -> value.worker.execute(() -> {
            // 隔离可恢复异常并返回是否完成当前操作。
            FailureGuard.run("diagnostics.copy", () -> {
                // 在后台原子保存已变化的日志快照。
                value.save();
                // 导出按时间排列的最近一小时诊断文本。
                String logs = value.buffer.export();
                String report = "TV diagnostics\nversion=" + BuildConfig.VERSION_NAME + ",mode=" + BuildConfig.FLAVOR_mode
                        + ",abi=" + BuildConfig.FLAVOR_abi + "\nAndroid=" + Build.VERSION.RELEASE + ",device=" + Build.MANUFACTURER + " " + Build.MODEL
                        + "\nLast 1h; screen errors only; background logs excluded; full stacks within 96K; duplicates merged; credentials redacted.\n"
                        // 明示插件提示观察入口是否安装成功。
                        + "toastObserver=" + ToastDiagnostics.status() + "\n"
                        + (logs.isEmpty() ? "No diagnostic errors in this window.\n" : logs);
                App.post(() -> {
                    // 隔离可恢复异常并返回是否完成当前操作。
                    FailureGuard.run("diagnostics.clipboard", () -> Util.copy(report));
                });
            });
        }));
    }
}
