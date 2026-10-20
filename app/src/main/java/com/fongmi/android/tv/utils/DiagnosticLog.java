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
        // 恢复本地日志并定期保存有限快照。
        instance = new DiagnosticLog(context.getApplicationContext());
        Logger.addLogAdapter(instance);
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
        // 按异常标记写入脱敏诊断缓冲。
        record(tag, message, true);
    }

    // 记录额外诊断事件且不依赖系统日志权限。
    public static void record(String tag, String message) {
        // 按异常标记写入脱敏诊断缓冲。
        record(tag, message, false);
    }

    // 按异常标记写入脱敏诊断缓冲。
    public static void record(String tag, String message, boolean error) {
        // 过滤业务、运行、取消和纯网络错误。
        if (!RecentLogBuffer.useful(tag, message, error)) return;
        DiagnosticLog value = instance;
        if (value == null) return;
        // 区分业务摘要与异常并保留诊断信息。
        value.buffer.append(tag, message, true);
    }

    // 保留程序异常堆栈并短暂关联显示信息。
    public static void prepareVisible(String message, Throwable error) {
        // 序列化原始堆栈且不忽略域名解析异常。
        String details = ScreenErrorContext.stack(error);
        // 按异常标记写入脱敏诊断缓冲。
        record("error", message + "\n" + details, true);
        DiagnosticLog value = instance;
        // 过滤业务、运行、取消和纯网络错误。
        if (value != null && RecentLogBuffer.useful("error", details, true)) {
            // 短暂关联提示和原始堆栈，限制总容量。
            value.screenErrors.remember(message, details);
        }
    }

    // 实际显示异常时保留原始信息和显示调用链。
    public static void visible(String tag, String message, boolean error) {
        DiagnosticLog value = instance;
        if (value == null || message == null || message.isBlank()) return;
        // 实际显示时取出堆栈，避免后台误记完整日志。
        String details = value.screenErrors.take(message);
        String original = message + "\n" + (details == null ? "" : details);
        // 过滤业务、运行、取消和纯网络错误。
        if (!RecentLogBuffer.useful(tag, original, error)) return;
        // 序列化原始堆栈且不忽略域名解析异常。
        String caller = ScreenErrorContext.stack(new Throwable("Screen display call site"));
        // 仅记录有效异常并保留脱敏后的完整堆栈。
        value.buffer.append(tag, original + caller, true, true);
    }

    // 在后台原子保存已变化的日志快照。
    private void save() {
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
        value.worker.execute(() -> {
            // 在后台原子保存已变化的日志快照。
            value.save();
            // 导出按时间排列的最近一小时诊断文本。
            String logs = value.buffer.export();
            String report = "TV diagnostics\nversion=" + BuildConfig.VERSION_NAME + ",mode=" + BuildConfig.FLAVOR_mode
                    + ",abi=" + BuildConfig.FLAVOR_abi + "\nAndroid=" + Build.VERSION.RELEASE + ",device=" + Build.MANUFACTURER + " " + Build.MODEL
                    + "\nLast 1h; exceptions only; business/runtime/network logs excluded; full stacks within 96K; duplicates merged; credentials redacted.\n"
                    + (logs.isEmpty() ? "No diagnostic errors in this window.\n" : logs);
            App.post(() -> Util.copy(report));
        });
    }
}
