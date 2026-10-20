package com.fongmi.android.tv.api.source;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChannelIdentity {
    public enum Category { CCTV, SATELLITE, LOCAL, HMT, NEWS, MOVIES, SPORTS, DOCUMENTARY, WORLD }
    private static final Pattern CCTV = Pattern.compile("^CCTV[- ]?(\\d+)(\\+|PLUS)?(?!(?:K|\\d))", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALITY = Pattern.compile("\\s*[（(][^）)]*(?:\\d+[PpIi]|高清|标清)[^）)]*[）)]|\\s*\\[[^]]*]|\\b(?:HD|SD|FHD|UHD)\\b", Pattern.CASE_INSENSITIVE);

    // 使用稳定频道标识合并同台备用线路。
    public static String key(String id, String name, String group, boolean domestic) {
        // 去掉清晰度标记保留真实频道区别。
        String cleaned = clean(name);
        String stable = id == null ? "" : id.split("@", 2)[0].trim().toUpperCase(Locale.ROOT);
        if (!stable.isEmpty() && stable.contains(".") && !stable.matches("CCTV\\d+(?:PLUS)?\\.CN")) return stable;
        if (cleaned.matches("(?i).*CCTV.?4.*(?:亚洲|欧洲|美洲|ASIA|EUROPE|AMERICA).*")) return "CN:" + cleaned.toUpperCase(Locale.ROOT);
        Matcher cctv = CCTV.matcher(cleaned);
        if (cctv.find()) return "CCTV" + cctv.group(1) + (cctv.group(2) == null ? "" : "+") + ".CN";
        if (!stable.isEmpty() && stable.contains(".")) return stable;
        return (domestic ? "CN" : (group == null ? "" : group.trim().toUpperCase(Locale.ROOT))) + ":" + cleaned.toUpperCase(Locale.ROOT);
    }

    // 优先频道地区标识再按明确分组判断归属。
    public static boolean domestic(String id, boolean sourceDomestic, String group) {
        String stable = id == null ? "" : id.split("@", 2)[0].toLowerCase(Locale.ROOT);
        if (stable.contains(".")) return stable.matches(".*\\.(cn|hk|mo|tw)$");
        String category = group == null ? "" : group.toLowerCase(Locale.ROOT);
        if (category.matches(".*(?:国际频道|國際頻道|海外频道|海外頻道|international|world).*")) return false;
        if (category.matches(".*(?:港台|港臺|港澳|香港|台湾|台灣|臺灣|澳门|澳門|央视|央視|卫视|衛視|地方|国内|國內).*")) return true;
        return sourceDomestic;
    }

    // 将国内外频道归入有限的节目分类。
    public static Category category(String name, String id, String group, boolean domestic) {
        String title = (name == null ? "" : name).toUpperCase(Locale.ROOT);
        String stable = id == null ? "" : id.toLowerCase(Locale.ROOT);
        if (domestic) {
            if (stable.matches(".*\\.(hk|mo|tw)(@.*)?$")) return Category.HMT;
            if (!stable.contains(".") && group != null && group.matches(".*(?:港台|港臺|港澳|香港|台湾|台灣|臺灣|澳门|澳門).*")) return Category.HMT;
            if (title.startsWith("CCTV") || title.startsWith("CGTN") || title.contains("央视")) return Category.CCTV;
            if (title.contains("卫视") || title.contains("衛視")) return Category.SATELLITE;
            return Category.LOCAL;
        }
        String category = (group == null ? "" : group).toLowerCase(Locale.ROOT);
        if (category.matches(".*(?:news|新闻|新聞).*")) return Category.NEWS;
        if (category.matches(".*(?:sports|体育|體育).*")) return Category.SPORTS;
        if (category.matches(".*(?:documentary|纪录|紀錄).*")) return Category.DOCUMENTARY;
        if (category.matches(".*(?:movies|series|电影|影視|影视).*")) return Category.MOVIES;
        return Category.WORLD;
    }

    // 按央视主台序号排序避免默认进入特种台。
    public static int order(String name) {
        Matcher matcher = CCTV.matcher(name == null ? "" : name);
        if (!matcher.find() || matcher.group(1).length() > 2) return Integer.MAX_VALUE;
        return Integer.parseInt(matcher.group(1)) * 10 + (matcher.group(2) == null ? 0 : 1);
    }

    // 去掉清晰度标记保留真实频道区别。
    public static String clean(String name) {
        return QUALITY.matcher(name == null ? "" : name).replaceAll("").trim().replaceAll("\\s+", " ");
    }
}
