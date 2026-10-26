package com.fongmi.android.tv.api.source;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VideoFallback {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private final Set<String> visited = new HashSet<>();
    private final int limit;
    private boolean searching;
    private String title = "", year = "", episode = "";
    private long position;
    private boolean single;

    // 为一次点播设置有限的换站次数。
    public VideoFallback(int limit) {
        this.limit = Math.max(1, limit);
    }

    // 记录原始影片身份防止切到同名异片。
    public void target(String title, String year) {
        if (!this.title.isEmpty()) return;
        // 统一影片名称但保留季数与年份。
        this.title = normalize(title);
        // 统一影片名称但保留季数与年份。
        this.year = normalize(year);
    }

    // 使用站点与影片组合记录已试来源。
    public void fail(String site, String id) {
        visited.add(site + "\u0000" + id);
    }

    // 禁止重复尝试并限制本轮换站次数。
    public boolean allows(String site, String id) {
        return visited.size() < limit && !visited.contains(site + "\u0000" + id);
    }

    // 每轮失败最多启动一次跨站搜索。
    public boolean beginSearch() {
        if (searching || visited.size() >= limit) return false;
        searching = true;
        return true;
    }

    // 按片名与已知年份校验候选影片。
    public boolean matches(String name, String candidateYear) {
        // 统一影片名称但保留季数与年份。
        if (!title.equals(normalize(name))) return false;
        // 统一影片名称但保留季数与年份。
        String otherYear = normalize(candidateYear);
        return year.isEmpty() || otherYear.isEmpty() || year.equals(otherYear);
    }

    // 保存换站前集数与内存播放进度。
    public void remember(String episode, long position, boolean single) {
        if (!this.episode.isEmpty()) {
            // 按集名或相同数字匹配续播集数。
            if (matchesEpisode(episode, single)) this.position = Math.max(0, position);
            return;
        }
        this.episode = episode == null ? "" : episode;
        this.position = Math.max(0, position);
        this.single = single;
    }

    // 按集名或相同数字匹配续播集数。
    public boolean matchesEpisode(String candidate, boolean candidateSingle) {
        // 统一影片名称但保留季数与年份。
        if (normalize(episode).equals(normalize(candidate))) return true;
        // 提取全部集数数字避免混淆季数与集数。
        List<String> expected = numbers(episode);
        // 提取全部集数数字避免混淆季数与集数。
        List<String> actual = numbers(candidate);
        if (expected.isEmpty() && actual.isEmpty()) return single && candidateSingle;
        return !expected.isEmpty() && expected.equals(actual);
    }

    // 提取全部集数数字避免混淆季数与集数。
    private static List<String> numbers(String value) {
        List<String> numbers = new ArrayList<>();
        Matcher matcher = NUMBER.matcher(value == null ? "" : value);
        while (matcher.find()) numbers.add(matcher.group().replaceFirst("^0+(?!$)", ""));
        return numbers;
    }

    // 判断是否存在待续播的集数。
    public boolean hasResume() {
        return !episode.isEmpty();
    }

    // 获取换站前的内存播放进度。
    public long position() {
        return position;
    }

    // 播放成功或主动选片后开始新的恢复轮次。
    public void reset() {
        // 播放恢复后清空本轮失败与续播状态。
        recovered();
        title = "";
        year = "";
    }

    // 播放恢复后清空本轮失败与续播状态。
    public void recovered() {
        visited.clear();
        searching = false;
        episode = "";
        position = 0;
    }

    // 统一影片名称但保留季数与年份。
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Punct}，。·：！？（）【】]", "");
    }
}
