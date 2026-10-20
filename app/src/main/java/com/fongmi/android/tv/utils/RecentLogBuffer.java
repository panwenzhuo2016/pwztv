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
    private static final int MAX_ENTRIES = 2048;
    private static final String TRUNCATED = " [truncated]";
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
        // 仅记录有效异常并保留脱敏后的完整堆栈。
        append(tag, message, error, false);
    }

    // 仅记录有效异常并保留脱敏后的完整堆栈。
    public synchronized void append(String tag, String message, boolean error, boolean visible) {
        // 识别屏幕错误并过滤后台日志与普通提示。
        if (!visible || !useful(tag, message, error)) return;
        String source = tag == null ? "app" : tag;
        // 统一整理摘要与异常并遮蔽敏感信息。
        String text = prepare(source, message);
        // 淘汰过期条目与超出容量的最早日志。
        trim();
        long now = clock.getAsLong();
        // 合并当前保留窗口内内容相同的异常。
        // 保存异常显示状态及重复计数时间范围。
        store(new Entry(now, text, true, visible, 1, now));
        // 淘汰过期条目与超出容量的最早日志。
        trim();
    }

    // 识别屏幕错误并过滤后台日志与普通提示。
    public static boolean useful(String tag, String message, boolean error) {
        if (message == null || message.isBlank()) return false;
        // 识别屏幕错误提示并排除普通成功通知。
        return error || DiagnosticError.isPopupError(message);
    }

    // 合并当前保留窗口内内容相同的异常。
    private void store(Entry entry) {
        String key = entry.visible + "\0" + entry.text;
        Entry previous = repeated.get(key);
        if (previous != null) {
            previous.count = (int) Math.min(Integer.MAX_VALUE, (long) previous.count + entry.count);
            previous.last = Math.max(previous.last, entry.last);
            return;
        }
        entries.addLast(entry);
        repeated.put(key, entry);
        chars += entry.text.length();
    }

    // 统一整理摘要与异常并遮蔽敏感信息。
    private static String prepare(String tag, String message) {
        String text = tag.equals("aesX") || tag.equals("rsaX") ? "[crypto details omitted]" : message;
        // 遮蔽链接凭据与常见认证字段。
        text = redact(text);
        if ("home".equals(tag) && text.startsWith("result=")) {
            int end = text.indexOf('\n');
            text = end < 0 ? "" : text.substring(end + 1);
        }
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
        var iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry removed = iterator.next();
            if (removed.last >= cutoff) continue;
            iterator.remove();
            chars -= removed.text.length();
            repeated.remove(removed.visible + "\0" + removed.text, removed);
        }
        while (!entries.isEmpty() && (chars > MAX_CHARS || entries.size() > MAX_ENTRIES)) {
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
            if (time > now) continue;
            String stored = item.get("text").getAsString();
            int separator = stored.indexOf(": ");
            String tag = separator < 0 ? "app" : stored.substring(0, separator);
            String message = separator < 0 ? stored : stored.substring(separator + 2);
            boolean visible = item.has("visible") && item.get("visible").getAsBoolean();
            // 识别屏幕错误并过滤后台日志与普通提示。
            if (!visible || !useful(tag, message, true)) continue;
            int count = item.has("count") ? item.get("count").getAsInt() : 1;
            long last = item.has("last") ? item.get("last").getAsLong() : time;
            if (count < 1 || last < time || last > now) throw new IllegalArgumentException("Invalid diagnostic count or time");
            if (last < now - RETENTION_MS) continue;
            // 统一整理摘要与异常并遮蔽敏感信息。
            // 合并当前保留窗口内内容相同的异常。
            // 保存异常显示状态及重复计数时间范围。
            restored.store(new Entry(time, prepare(tag, message), true, visible, count, last));
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
