package com.fongmi.android.tv.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiagnosticError {
    private static final Pattern PLAYER = Pattern.compile("^tag=[^\\r\\n]*?,key=([^\\r\\n]*?),code=(\\d+)\\s*:\\s*|^source=[^\\r\\n]*? \\| ");
    private static final Pattern EXCEPTION = Pattern.compile("(?m)^[\\t ]*(?:(?:Caused by:|Suppressed:)[\\t ]*)?((?:[\\w$]+\\.)+[\\w$]+|[\\w$]*(?:Exception|Error)|Throwable)(?=:|[\\t ]*$)[^\\r\\n]*");
    private static final Set<String> NETWORK = Set.of("SocketException", "SocketTimeoutException", "ConnectException",
            "NoRouteToHostException", "UnknownHostException", "UnknownServiceException", "ProtocolException",
            "StreamResetException", "ConnectionShutdownException", "TimeoutException", "SSLException",
            "SSLHandshakeException", "SSLPeerUnverifiedException", "SSLProtocolException", "SSLKeyException",
            "InvalidResponseCodeException");
    private static final Set<String> WRAPPERS = Set.of("java.util.concurrent.ExecutionException",
            "java.util.concurrent.CompletionException", "java.io.IOException", "java.io.InterruptedIOException",
            "androidx.media3.common.PlaybackException", "androidx.media3.exoplayer.ExoPlaybackException",
            "com.google.android.exoplayer2.PlaybackException", "com.google.android.exoplayer2.ExoPlaybackException",
            "androidx.media3.datasource.HttpDataSource$HttpDataSourceException",
            "androidx.media3.datasource.DataSourceException");
    private static final Pattern IO_NETWORK = Pattern.compile("(?i)^(?:java\\.io\\.)?(?:InterruptedIO|IO)Exception: (?:Canceled|interrupted|timeout|connection reset|broken pipe|failed to connect|unable to resolve host)(?:\\b|$)");

    // 识别真实异常并忽略人为附加的显示调用链。
    public static boolean isException(String message) {
        if (message == null) return false;
        Matcher matcher = EXCEPTION.matcher(message);
        while (matcher.find()) {
            if (!matcher.group().contains("Throwable: Screen display call site")) return true;
        }
        return message.contains("cannot be converted to");
    }

    // 仅过滤网络异常链，混有程序异常时仍保留。
    public static boolean isRoutineNetwork(String message) {
        if (message == null) return false;
        Matcher player = PLAYER.matcher(message);
        if (player.find()) message = message.substring(player.end());
        boolean network = false;
        Matcher matcher = EXCEPTION.matcher(message);
        while (matcher.find()) {
            String type = matcher.group(1);
            String line = matcher.group().trim();
            String reason = line.substring(line.indexOf(type));
            int name = Math.max(type.lastIndexOf('.'), type.lastIndexOf('$')) + 1;
            if (NETWORK.contains(type.substring(name)) || IO_NETWORK.matcher(reason).find()) network = true;
            else if (!WRAPPERS.contains(type) && !reason.equals(type + ": Source error")
                    && !reason.equals("java.lang.Throwable: Screen display call site")
                    && !reason.equals("Throwable: Screen display call site")) return false;
        }
        return network;
    }

    // 去除播放日志前缀后识别真实异常内容。
    public static boolean containsException(String message) {
        if (message == null) return false;
        Matcher player = PLAYER.matcher(message);
        if (player.find()) message = message.substring(player.end());
        // 识别真实异常并忽略人为附加的显示调用链。
        return isException(message);
    }
}
