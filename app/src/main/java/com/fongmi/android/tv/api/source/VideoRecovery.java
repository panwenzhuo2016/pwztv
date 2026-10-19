package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Vod;

public final class VideoRecovery {
    // 查找换站后相同集数并保留原播放进度。
    public static boolean resume(VideoFallback fallback, Vod vod, History history) {
        // 判断是否存在待续播的集数。
        if (!fallback.hasResume()) return true;
        // 按片名与已知年份校验候选影片。
        if (!fallback.matches(vod.getName(), vod.getYear())) return false;
        for (Flag flag : vod.getFlags()) for (Episode episode : flag.getEpisodes()) {
            // 按集名或相同数字匹配续播集数。
            if (!fallback.matchesEpisode(episode.getName(), flag.getEpisodes().size() == 1)) continue;
            history.setVodFlag(flag.getFlag());
            history.setVodRemarks(episode.getName());
            history.setEpisodeUrl(episode.getUrl());
            // 获取换站前的内存播放进度。
            history.setPosition(fallback.position());
            return true;
        }
        return false;
    }
}
