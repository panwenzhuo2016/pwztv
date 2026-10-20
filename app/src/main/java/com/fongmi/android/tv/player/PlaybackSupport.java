package com.fongmi.android.tv.player;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class PlaybackSupport {
    private static final Set<String> REMOVED_KEYS = Set.of("贱贱", "新6v");
    private static final Set<String> REMOVED_APIS = Set.of("csp_jpj", "csp_jpjguard", "csp_sixv", "csp_sixvguard");
    private static final Pattern P2P_SCHEME = Pattern.compile("^(magnet|thunder|ed2k|jianpian|tvbox-xg|xg|xgplay|ftp):", Pattern.CASE_INSENSITIVE);
    private static final Pattern TORRENT = Pattern.compile("^[^?#;|]+\\.torrent(?:[?#;|].*)?$", Pattern.CASE_INSENSITIVE);

    // 过滤依赖已移除引擎的已知片源。
    public static boolean removedSite(String key, String api) {
        return key != null && REMOVED_KEYS.contains(key.trim().toLowerCase(Locale.ROOT))
                || api != null && REMOVED_APIS.contains(api.trim().toLowerCase(Locale.ROOT));
    }

    // 识别已移除的下载与P2P播放地址。
    public static boolean removedUrl(String url) {
        if (url == null) return false;
        String value = url.trim();
        return P2P_SCHEME.matcher(value).find() || TORRENT.matcher(value).matches();
    }
}
