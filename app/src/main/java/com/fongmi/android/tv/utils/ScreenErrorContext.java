package com.fongmi.android.tv.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ScreenErrorContext {
    private static final int MAX_PENDING = 8;
    private static final long RETENTION_MS = TimeUnit.MINUTES.toMillis(1);
    private static final Pattern PLUGIN_FRAME = Pattern.compile("(?m)^[\\t ]*at com\\.github\\.catvod\\.spider\\.([\\w$]+)\\.(playerContent|searchContent|homeContent|homeVideoContent|detailContent|categoryContent)\\(");
    private final Map<String, Entry> pending = new LinkedHashMap<>();
    private final LongSupplier clock;

    private record Entry(String details, long time) {}

    // 注入时钟以限制待显示异常的保留时间。
    ScreenErrorContext(LongSupplier clock) {
        this.clock = clock;
    }

    // 短暂关联提示和原始堆栈，限制总容量。
    synchronized void remember(String message, String details) {
        message = message.trim().replace(": ", ":");
        pending.remove(message);
        if (message.length() + details.length() <= RecentLogBuffer.MAX_CHARS) {
            pending.put(message, new Entry(details, clock.getAsLong()));
        }
        // 清理过期上下文并淘汰超限的最早记录。
        trim();
    }

    // 实际显示时取出堆栈，避免后台误记完整日志。
    synchronized String take(String message) {
        // 清理过期上下文并淘汰超限的最早记录。
        trim();
        Entry entry = pending.remove(message.trim().replace(": ", ":"));
        if (entry != null) pending.values().removeIf(value -> value.details().equals(entry.details()));
        return entry == null ? null : entry.details();
    }

    // 兼容旧安卓提取首行并匹配插件异常。
    synchronized void rememberLogged(String details) {
        int end = 0;
        while (end < details.length() && details.charAt(end) != '\n' && details.charAt(end) != '\r') end++;
        String first = details.substring(0, end).trim();
        int separator = first.indexOf(":");
        String reason = separator < 0 ? "" : first.substring(separator + 1).trim();
        // 短暂关联提示和原始堆栈，限制总容量。
        remember(first, details);
        if (!reason.isEmpty()) {
            // 短暂关联提示和原始堆栈，限制总容量。
            remember(reason, details);
        }
        Matcher frame = PLUGIN_FRAME.matcher(details);
        if (frame.find() && !frame.group(1).equals("BaseSpiderGuard")) {
            String method = frame.group(2).equals("playerContent") ? "playContent" : frame.group(2);
            // 短暂关联提示和原始堆栈，限制总容量。
            remember(frame.group(1) + "-" + method + ":" + reason, details);
        }
    }

    // 清理过期上下文并淘汰超限的最早记录。
    private void trim() {
        long cutoff = clock.getAsLong() - RETENTION_MS;
        pending.entrySet().removeIf(entry -> entry.getValue().time() < cutoff);
        int chars = pending.entrySet().stream().mapToInt(entry -> entry.getKey().length() + entry.getValue().details().length()).sum();
        while (pending.size() > MAX_PENDING || chars > RecentLogBuffer.MAX_CHARS) {
            String key = pending.keySet().iterator().next();
            chars -= key.length() + pending.remove(key).details().length();
        }
    }

    // 序列化原始堆栈且不忽略域名解析异常。
    static String stack(Throwable error) {
        StringWriter output = new StringWriter();
        error.printStackTrace(new PrintWriter(output));
        return output.toString();
    }
}
