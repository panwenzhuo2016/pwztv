package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RecentLogBuffer {
    public static final long RETENTION_MS = TimeUnit.HOURS.toMillis(1);
    public static final int MAX_CHARS = 96 * 1024;
    public static final int MAX_BACKGROUND_CHARS = 100;
    private static final int MAX_ENTRIES = 2048;
    private static final String TRUNCATED = " [truncated]";
    private static final long MERGE_MS = TimeUnit.MINUTES.toMillis(1);
    private static final Pattern CANCELED = Pattern.compile("(?m)^(?:Caused by: )?java\\.(?:lang\\.InterruptedException|util\\.concurrent\\.CancellationException)\\b|(?:^|\\n)(?:Caused by: )?(?:java\\.io\\.(?:InterruptedIOException|IOException): )?(?:Canceled|Home request canceled|interrupted)(?:\\s|$)");
    private static final Pattern STACK = Pattern.compile("(?m)^\\s*(?:at [\\w.$/]+\\([^\\r\\n]*\\)|Caused by: |Suppressed: )");
    private static final Pattern URL = Pattern.compile("https?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET = Pattern.compile("(?i)(authorization|(?:set-)?cookie|password|token|secret|api[_-]?key|access[_-]?token)([\"']?\\s*[:=]\\s*)[^\\r\\n,}]+");
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    private final Map<String, Entry> repeated = new HashMap<>();
    private final LongSupplier clock;
    private int chars;

    private static final class Entry {
        final long time;
        final String text;
        final boolean error;
        final boolean visible;
        long last;
        int count;

        // 保存异常显示状态及重复计数时间范围。
        Entry(long time, String text, boolean error, boolean visible, int count, long last) {
            this.time = time;
            this.text = text;
            this.error = error;
            this.visible = visible;
            this.count = count;
            this.last = last;
        }
    }

    // 注入时钟便于验证一小时保留边界。
    public RecentLogBuffer(LongSupplier clock) {
        this.clock = clock;
    }

    // 记录脱敏日志并限制单条与总容量。
    public void append(String tag, String message) {
        // 区分业务摘要与异常并保留诊断信息。
        append(tag, message, false);
    }

    // 区分业务摘要与异常并保留诊断信息。
    public synchronized void append(String tag, String message, boolean error) {
        // 仅屏幕异常保留详情，后台正文最多百字。
        append(tag, message, error, false);
    }

    // 仅屏幕异常保留详情，后台正文最多百字。
    public synchronized void append(String tag, String message, boolean error, boolean visible) {
        if (message == null || message.isBlank()) return;
        String source = tag == null ? "app" : tag;
        // 识别旧日志中的异常标签与堆栈。
        error |= isError(source, message);
        // 统一整理摘要与异常并遮蔽敏感信息。
        String text = prepare(source, message, visible);
        // 淘汰过期条目与超出容量的最早日志。
        trim();
        long now = clock.getAsLong();
        // 合并一分钟内内容相同的异常。
        // 保存异常显示状态及重复计数时间范围。
        store(new Entry(now, text, error, visible, 1, now));
        // 淘汰过期条目与超出容量的最早日志。
        trim();
    }

    // 仅保留异常并排除正常取消与业务数据。
    public static boolean useful(String tag, String message, boolean error) {
        if (message == null || message.isBlank() || CANCELED.matcher(message).find()) return false;
        // 识别旧日志中的异常标签与堆栈。
        // 识别提示中的异常文本而非普通操作提示。
        return error || isError(tag, message) || ("notice".equals(tag) && DiagnosticError.isException(message));
    }

    // 合并一分钟内内容相同的异常。
    private void store(Entry entry) {
        String key = entry.visible + "\0" + entry.text;
        Entry previous = repeated.get(key);
        if (entry.error && previous != null && previous.error && entry.last - previous.time <= MERGE_MS) {
            previous.count = (int) Math.min(Integer.MAX_VALUE, (long) previous.count + entry.count);
            previous.last = Math.max(previous.last, entry.last);
            return;
        }
        entries.addLast(entry);
        repeated.put(key, entry);
        chars += entry.text.length();
    }

    // 识别旧日志中的异常标签与堆栈。
    private static boolean isError(String tag, String message) {
        return "error".equals(tag) || STACK.matcher(message).find();
    }

    // 统一整理摘要与异常并遮蔽敏感信息。
    private static String prepare(String tag, String message, boolean visible) {
        String text = tag.equals("aesX") || tag.equals("rsaX") ? "[crypto details omitted]" : message;
        // 遮蔽链接凭据与常见认证字段。
        text = redact(text);
        // 仅将明确的网络异常链归为普通故障。
        if (!visible && DiagnosticError.isRoutineNetwork(text)) {
            // 摘出网络错误原因并附带原始业务上下文。
            text = DiagnosticError.networkSummary(text);
        }
        // 按字符预算截断且保留完整字符。
        if (!visible) text = shorten(text, MAX_BACKGROUND_CHARS);
        // 按字符预算截断且保留完整字符。
        return shorten(tag + ": " + text, MAX_CHARS);
    }

    // 按字符预算截断且保留完整字符。
    private static String shorten(String text, int limit) {
        if (text.length() <= limit) return text;
        int end = limit - TRUNCATED.length();
        if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) end--;
        return text.substring(0, end) + TRUNCATED;
    }

    // 淘汰过期条目与超出容量的最早日志。
    private void trim() {
        long cutoff = clock.getAsLong() - RETENTION_MS;
        while (!entries.isEmpty() && (entries.peekFirst().time < cutoff || chars > MAX_CHARS || entries.size() > MAX_ENTRIES)) {
            Entry removed = entries.removeFirst();
            chars -= removed.text.length();
            repeated.remove(removed.visible + "\0" + removed.text, removed);
        }
    }

    // 导出按时间排列的最近一小时诊断文本。
    public synchronized String export() {
        // 淘汰过期条目与超出容量的最早日志。
        trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        StringBuilder text = new StringBuilder();
        for (Entry entry : entries) {
            text.append(format.format(new Date(entry.time)));
            if (entry.count > 1) text.append(" [count=").append(entry.count).append(",last=").append(format.format(new Date(entry.last))).append(']');
            text.append(' ').append(entry.text).append('\n');
        }
        return text.toString();
    }

    // 将有限日志编码为可恢复的本地快照。
    public synchronized String encode() {
        // 淘汰过期条目与超出容量的最早日志。
        trim();
        JsonArray array = new JsonArray();
        for (Entry entry : entries) {
            JsonObject item = new JsonObject();
            item.addProperty("time", entry.time);
            item.addProperty("text", entry.text);
            item.addProperty("error", entry.error);
            item.addProperty("visible", entry.visible);
            item.addProperty("count", entry.count);
            item.addProperty("last", entry.last);
            array.add(item);
        }
        return array.toString();
    }

    // 恢复仍在一小时范围内的日志快照。
    public synchronized void restore(String snapshot) {
        JsonArray array = JsonParser.parseString(snapshot).getAsJsonArray();
        // 注入时钟便于验证一小时保留边界。
        RecentLogBuffer restored = new RecentLogBuffer(clock);
        long now = clock.getAsLong(), previous = Long.MIN_VALUE;
        for (JsonElement element : array) {
            JsonObject item = element.getAsJsonObject();
            long time = item.get("time").getAsLong();
            if (time < previous) throw new IllegalArgumentException("Unordered diagnostic snapshot");
            previous = time;
            if (time < now - RETENTION_MS || time > now) continue;
            String stored = item.get("text").getAsString();
            int separator = stored.indexOf(": ");
            String tag = separator < 0 ? "app" : stored.substring(0, separator);
            String message = separator < 0 ? stored : stored.substring(separator + 2);
            // 识别旧日志中的异常标签与堆栈。
            boolean error = item.has("error") ? item.get("error").getAsBoolean() : isError(tag, message);
            // 仅保留异常并排除正常取消与业务数据。
            if (!useful(tag, message, error)) continue;
            error = true;
            boolean visible = item.has("visible") && item.get("visible").getAsBoolean();
            int count = item.has("count") ? item.get("count").getAsInt() : 1;
            long last = item.has("last") ? item.get("last").getAsLong() : time;
            if (count < 1 || last < time || last > now) throw new IllegalArgumentException("Invalid diagnostic count or time");
            // 统一整理摘要与异常并遮蔽敏感信息。
            // 合并一分钟内内容相同的异常。
            // 保存异常显示状态及重复计数时间范围。
            restored.store(new Entry(time, prepare(tag, message, visible), error, visible, count, last));
        }
        entries.clear();
        entries.addAll(restored.entries);
        repeated.clear();
        repeated.putAll(restored.repeated);
        chars = restored.chars;
        // 淘汰过期条目与超出容量的最早日志。
        trim();
    }

    // 遮蔽链接凭据与常见认证字段。
    private static String redact(String message) {
        Matcher matcher = URL.matcher(message);
        StringBuffer safe = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group();
            int query = url.indexOf('?'), fragment = url.indexOf('#');
            int end = Math.min(query < 0 ? url.length() : query, fragment < 0 ? url.length() : fragment);
            String base = url.substring(0, end);
            int authority = base.indexOf("://") + 3, slash = base.indexOf('/', authority), at = base.indexOf('@', authority);
            if (at >= 0 && (slash < 0 || at < slash)) base = base.substring(0, authority) + "***@" + base.substring(at + 1);
            matcher.appendReplacement(safe, Matcher.quoteReplacement(base + (end < url.length() ? "?[redacted]" : "")));
        }
        matcher.appendTail(safe);
        return SECRET.matcher(safe).replaceAll("$1$2[redacted]");
    }
}
