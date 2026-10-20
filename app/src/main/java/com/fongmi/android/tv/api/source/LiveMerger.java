package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.utils.ResUtil;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LiveMerger {
    private final Map<String, Channel> channels = new LinkedHashMap<>();
    private final Map<String, Set<String>> urls = new HashMap<>();
    private final Map<ChannelIdentity.Category, Group> groups = new LinkedHashMap<>();
    private final Live target;
    private final int limit;

    // 创建单次频道聚合索引与容量限制。
    public LiveMerger(Live target, int limit) {
        this.target = target;
        this.limit = Math.max(1, limit);
    }

    // 合并同台线路并保留各源请求参数。
    public void add(Live source, SourceManifest.Entry entry) {
        for (Group group : source.getGroups()) for (Channel channel : group.getChannel()) {
            String name = channel.getName();
            if (name.isBlank() || name.contains("更新时间") || name.contains("更新日期")) continue;
            // 优先频道地区标识再按明确分组判断归属。
            boolean domestic = ChannelIdentity.domestic(channel.getTvgId(), entry.domestic, group.getName());
            // 使用稳定频道标识合并同台备用线路。
            String key = ChannelIdentity.key(channel.getTvgId(), name, entry.id + ":" + group.getName(), domestic);
            for (String route : channel.getUrls()) {
                String url = route.split("\\$", 2)[0];
                // 过滤无法从公网直接访问的地址。
                if (!publicUrl(url)) continue;
                Set<String> seen = urls.computeIfAbsent(key, ignored -> new HashSet<>());
                // 获取指定线路自己的播放请求参数。
                Channel metadata = channel.route(route);
                Map<String, String> headers = new HashMap<>(entry.headers == null ? Map.of() : entry.headers);
                headers.putAll(metadata.getHeaders());
                String signature = url + new java.util.TreeMap<>(headers) + (metadata.getDrm() == null ? "" : com.fongmi.android.tv.App.gson().toJson(metadata.getDrm()));
                if (seen.size() >= limit || !seen.add(signature)) continue;
                Channel merged = channels.get(key);
                if (merged == null) {
                    // 复制频道并保留线路独立请求参数。
                    merged = new Channel().copy(channel);
                    merged.setUrls(new java.util.ArrayList<>());
                    if (!domestic) {
                        String stable = channel.getTvgId().split("@", 2)[0];
                        String region = stable.matches(".*\\.[a-zA-Z]{2}$") ? stable.substring(stable.lastIndexOf('.') + 1).toUpperCase(java.util.Locale.ROOT) : entry.name;
                        merged.setName(name + " · " + region);
                        merged.setShow(merged.getName());
                    }
                    merged.setNumber(channels.size() + 1);
                    channels.put(key, merged);
                    // 将国内外频道归入有限的节目分类。
                    ChannelIdentity.Category category = ChannelIdentity.category(name, channel.getTvgId(), group.getName(), domestic);
                    Group output = groups.get(category);
                    if (output == null) {
                        // 获取分类对应的本地化名称。
                        output = new Group(categoryName(category));
                        groups.put(category, output);
                        target.getGroups().add(output);
                    }
                    merged.setGroup(output);
                    output.getChannel().add(merged);
                }
                // 复制频道并保留线路独立请求参数。
                Channel request = new Channel().copy(metadata);
                request.setUa("");
                request.setOrigin("");
                request.setReferer("");
                request.setHeader(headers);
                // 保存每条线路独立的播放请求参数。
                merged.addRoute(request, url, entry.name + " " + (merged.getUrls().size() + 1));
            }
        }
    }

    // 获取分类对应的本地化名称。
    private static String categoryName(ChannelIdentity.Category category) {
        return ResUtil.getString(switch (category) {
            case CCTV -> R.string.source_cctv;
            case SATELLITE -> R.string.source_satellite;
            case LOCAL -> R.string.source_local;
            case HMT -> R.string.source_hmt;
            case NEWS -> R.string.source_news;
            case MOVIES -> R.string.source_movies;
            case SPORTS -> R.string.source_sports;
            case DOCUMENTARY -> R.string.source_documentary;
            case WORLD -> R.string.source_world;
        });
    }

    // 固定分类顺序并优先展示国内频道。
    public void finish() {
        Group cctv = groups.get(ChannelIdentity.Category.CCTV);
        // 按央视主台序号排序避免默认进入特种台。
        if (cctv != null) cctv.getChannel().sort(java.util.Comparator.comparingInt(channel -> ChannelIdentity.order(channel.getName())));
        target.getGroups().clear();
        groups.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> target.getGroups().add(entry.getValue()));
    }

    // 过滤无法从公网直接访问的地址。
    public static boolean publicUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) return false;
            String host = uri.getHost();
            if (host == null || host.equalsIgnoreCase("localhost")) return false;
            return !host.matches("(?i)(?:127|10|0|169\\.254|192\\.168|172\\.(?:1[6-9]|2\\d|3[01]))\\..*|(?:22[4-9]|23\\d)\\..*|\\[?(?:::1|f[cd][0-9a-f].*|fe[89ab][0-9a-f].*)\\]?");
        } catch (IllegalArgumentException error) {
            return false;
        }
    }
}
