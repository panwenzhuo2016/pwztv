package com.fongmi.android.tv.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiagnosticError {
    private static final int MAX_SOURCE_CHARS = 24;
    private static final Pattern PLAYER = Pattern.compile("^tag=[^\\r\\n]*?,key=([^\\r\\n]*?),code=(\\d+)\\s*:\\s*");
    private static final Pattern EXCEPTION = Pattern.compile("(?m)^[\\t ]*(?:(?:Caused by:|Suppressed:)[\\t ]*)?((?:[\\w$]+\\.)+[\\w$]+|[\\w$]*(?:Exception|Error)|Throwable)(?=:|[\\t ]*$)[^\\r\\n]*");
    private static final Set<String> NETWORK = Set.of("java.net.SocketException", "java.net.SocketTimeoutException",
            "java.net.ConnectException", "java.net.NoRouteToHostException", "java.net.UnknownHostException");
    private static final Set<String> WRAPPERS = Set.of("java.util.concurrent.ExecutionException",
            "java.util.concurrent.CompletionException");

    // 优先保留播放源、错误码与主异常链根因。
    public static String backgroundSummary(String message) {
        Matcher player = PLAYER.matcher(message);
        if (player.find()) {
            String source = player.group(1).split("@@@", 2)[0];
            int end = Math.min(source.length(), MAX_SOURCE_CHARS);
            if (end > 0 && Character.isHighSurrogate(source.charAt(end - 1))) end--;
            String context = "source=" + source.substring(0, end) + ",code=" + player.group(2);
            // 提取主异常根因并排除附带异常分支。
            String reason = rootSummary(message.substring(player.end()));
            Matcher exception = EXCEPTION.matcher(reason);
            if (exception.lookingAt()) {
                String type = exception.group(1);
                if (type.endsWith("Exception") || type.endsWith("Error")) {
                    int name = Math.max(type.lastIndexOf('.'), type.lastIndexOf('$')) + 1;
                    reason = type.substring(name) + reason.substring(type.length());
                }
            }
            return context + " | " + reason;
        }
        // 仅将明确的网络异常链归为普通故障。
        if (isRoutineNetwork(message)) {
            // 摘出网络错误原因并附带原始业务上下文。
            return networkSummary(message);
        }
        // 提取主异常根因并排除附带异常分支。
        return rootSummary(message);
    }

    // 提取主异常根因并排除附带异常分支。
    private static String rootSummary(String message) {
        Matcher matcher = EXCEPTION.matcher(message);
        String reason = null;
        while (matcher.find()) {
            String line = matcher.group();
            if (line.stripLeading().startsWith("Suppressed:")) continue;
            if (reason == null || line.startsWith("Caused by:")) {
                reason = line.substring(line.indexOf(matcher.group(1))).trim();
            }
        }
        return reason == null ? message : reason;
    }

    // 识别提示中的异常文本而非普通操作提示。
    public static boolean isException(String message) {
        return message != null && (EXCEPTION.matcher(message).find() || message.contains("cannot be converted to"));
    }

    // 仅将明确的网络异常链归为普通故障。
    public static boolean isRoutineNetwork(String message) {
        if (message == null) return false;
        boolean network = false;
        Matcher matcher = EXCEPTION.matcher(message);
        while (matcher.find()) {
            String type = matcher.group(1);
            if (NETWORK.contains(type)) network = true;
            else if (!WRAPPERS.contains(type)) return false;
        }
        return network;
    }

    // 摘出网络错误原因并附带原始业务上下文。
    public static String networkSummary(String message) {
        Matcher matcher = EXCEPTION.matcher(message);
        while (matcher.find()) {
            if (!NETWORK.contains(matcher.group(1))) continue;
            String line = matcher.group().trim();
            int type = line.indexOf(matcher.group(1));
            String context = message.substring(0, matcher.start()).trim();
            // 丢弃包装异常堆栈，只保留前置业务信息。
            Matcher previous = EXCEPTION.matcher(context);
            if (previous.find()) context = context.substring(0, previous.start()).trim();
            return line.substring(type) + (context.isEmpty() ? "" : " | " + context.replace('\n', ' ').replace('\r', ' '));
        }
        return message;
    }
}
