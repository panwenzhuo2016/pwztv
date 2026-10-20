package com.fongmi.android.tv.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RecentLogBuffer {
    public static final long RETENTION_MS = TimeUnit.HOURS.toMillis(1);
    public static final int MAX_CHARS = 96 * 1024;
    private static final int MAX_ENTRY_CHARS = 4096;
    private static final int MAX_ENTRIES = 2048;
    private static final Pattern URL = Pattern.compile("https?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET = Pattern.compile("(?i)(authorization|(?:set-)?cookie|password|token|secret|api[_-]?key|access[_-]?token)([\"']?\\s*[:=]\\s*)[^\\r\\n,}]+");
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    private final LongSupplier clock;
    private int chars;

    private record Entry(long time, String text) { }

    // 注入时钟便于验证一小时保留边界。
    public RecentLogBuffer(LongSupplier clock) {
        this.clock = clock;
    }

    // 记录脱敏日志并限制单条与总容量。
    public synchronized void append(String tag, String message) {
        if (message == null || message.isBlank()) return;
        String source = tag == null ? "app" : tag;
        String text = source.equals("aesX") || source.equals("rsaX") ? "[crypto details omitted]" : message;
        if (text.length() > MAX_ENTRY_CHARS) text = text.substring(0, MAX_ENTRY_CHARS) + " [truncated]";
        // 遮蔽链接凭据与常见认证字段。
        text = source + ": " + redact(text);
        if (text.length() > MAX_ENTRY_CHARS) text = text.substring(0, MAX_ENTRY_CHARS) + " [truncated]";
        entries.addLast(new Entry(clock.getAsLong(), text));
        chars += text.length();
        // 淘汰过期条目与超出容量的最早日志。
        trim();
    }

    // 淘汰过期条目与超出容量的最早日志。
    private void trim() {
        long cutoff = clock.getAsLong() - RETENTION_MS;
        while (!entries.isEmpty() && (entries.peekFirst().time < cutoff || chars > MAX_CHARS || entries.size() > MAX_ENTRIES)) {
            chars -= entries.removeFirst().text.length();
        }
    }

    // 导出按时间排列的最近一小时诊断文本。
    public synchronized String export() {
        // 淘汰过期条目与超出容量的最早日志。
        trim();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        StringBuilder text = new StringBuilder();
        for (Entry entry : entries) text.append(format.format(new Date(entry.time))).append(' ').append(entry.text).append('\n');
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
            array.add(item);
        }
        return array.toString();
    }

    // 恢复仍在一小时范围内的日志快照。
    public synchronized void restore(String snapshot) {
        JsonArray array = JsonParser.parseString(snapshot).getAsJsonArray();
        ArrayDeque<Entry> restored = new ArrayDeque<>();
        long now = clock.getAsLong(), previous = Long.MIN_VALUE;
        for (JsonElement element : array) {
            JsonObject item = element.getAsJsonObject();
            long time = item.get("time").getAsLong();
            if (time < previous) throw new IllegalArgumentException("Unordered diagnostic snapshot");
            previous = time;
            if (time < now - RETENTION_MS || time > now) continue;
            // 遮蔽链接凭据与常见认证字段。
            String text = redact(item.get("text").getAsString());
            if (text.length() > MAX_ENTRY_CHARS + 32) text = text.substring(0, MAX_ENTRY_CHARS);
            restored.addLast(new Entry(time, text));
        }
        entries.clear();
        entries.addAll(restored);
        chars = entries.stream().mapToInt(entry -> entry.text.length()).sum();
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
