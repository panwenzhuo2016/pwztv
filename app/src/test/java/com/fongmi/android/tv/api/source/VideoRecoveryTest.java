package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Vod;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VideoRecoveryTest {
    // 反查重开轮次保留同片同集及进度校验。
    @Test public void retryRoundRetainsIdentityEpisodeAndPosition() {
        // 为一次点播设置有限的换站次数。
        VideoFallback fallback = new VideoFallback(1);
        // 记录原始影片身份防止切到同名异片。
        fallback.target("Example", "2024");
        // 保存换站前集数与内存播放进度。
        fallback.remember("第12集", 135_000, false);
        // 使用站点与影片组合记录已试来源。
        fallback.fail("a", "1");
        // 禁止重复尝试并限制本轮换站次数。
        assertFalse(fallback.allows("a", "1"));
        // 新一轮允许重试且保留影片身份集数及进度。
        fallback.retryRound();
        // 禁止重复尝试并限制本轮换站次数。
        assertTrue(fallback.allows("a", "1"));
        // 每轮失败最多启动一次跨站搜索。
        assertTrue(fallback.beginSearch());
        // 每轮失败最多启动一次跨站搜索。
        assertFalse(fallback.beginSearch());
        // 按片名与已知年份校验候选影片。
        assertTrue(fallback.matches("Example", "2024"));
        // 按片名与已知年份校验候选影片。
        assertFalse(fallback.matches("Example", "2023"));
        // 按集名或相同数字匹配续播集数。
        assertTrue(fallback.matchesEpisode("12", false));
        // 按集名或相同数字匹配续播集数。
        assertFalse(fallback.matchesEpisode("1", false));
        // 获取换站前的内存播放进度。
        assertEquals(135_000, fallback.position());
        // 轮询重试时允许重新探测上一轮冷却中的站点。
        assertTrue(fallback.isRetrying());
        // 播放恢复后清空本轮失败与续播状态。
        fallback.recovered();
        // 轮询重试时允许重新探测上一轮冷却中的站点。
        assertFalse(fallback.isRetrying());
        // 判断是否存在待续播的集数。
        assertFalse(fallback.hasResume());
    }

    // 反查重复换站保留最新进度且不恢复失败候选。
    @Test public void repeatedFallbackResumesLatestPositionAndRetainsAttemptLimit() {
        // 为一次点播设置有限的换站次数。
        VideoFallback fallback = new VideoFallback(2);
        // 记录原始影片身份防止切到同名异片。
        fallback.target("Example", "2024");
        // 保存换站前集数与内存播放进度。
        fallback.remember("第12集", 120_000, false);
        // 使用站点与影片组合记录已试来源。
        fallback.fail("a", "1");
        // 保存换站前集数与内存播放进度。
        fallback.remember("12", 135_000, false);
        Vod vod = mock(Vod.class);
        Flag flag = mock(Flag.class);
        Episode episode = mock(Episode.class);
        when(vod.getName()).thenReturn("Example");
        when(vod.getYear()).thenReturn("2024");
        when(vod.getFlags()).thenReturn(List.of(flag));
        when(flag.getFlag()).thenReturn("backup");
        when(flag.getEpisodes()).thenReturn(List.of(episode));
        when(episode.getName()).thenReturn("第12集");
        when(episode.getUrl()).thenReturn("https://example.org/new");
        History history = new History();
        // 查找换站后相同集数并保留原播放进度。
        assertTrue(VideoRecovery.resume(fallback, vod, history));
        assertEquals(135_000, history.getPosition());
        assertEquals("backup", history.getVodFlag());
        assertEquals("第12集", history.getVodRemarks());
        assertEquals("https://example.org/new", history.getEpisodeUrl());
        // 保存有效播放进度且忽略新线路未加载时的零值。
        VideoRecovery.remember(fallback, history, 0, -1, false);
        assertEquals(135_000, history.getPosition());
        // 获取换站前的内存播放进度。
        assertEquals(135_000, fallback.position());
        // 保存有效播放进度且忽略新线路未加载时的零值。
        VideoRecovery.remember(fallback, history, 90_000, 600_000, false);
        assertEquals(90_000, history.getPosition());
        // 获取换站前的内存播放进度。
        assertEquals(90_000, fallback.position());
        // 禁止重复尝试并限制本轮换站次数。
        assertFalse(fallback.allows("a", "1"));
        // 使用站点与影片组合记录已试来源。
        fallback.fail("b", "1");
        // 禁止重复尝试并限制本轮换站次数。
        assertFalse(fallback.allows("c", "1"));
        // 播放恢复后清空本轮失败与续播状态。
        fallback.recovered();
        // 禁止重复尝试并限制本轮换站次数。
        assertTrue(fallback.allows("a", "1"));
        // 判断是否存在待续播的集数。
        assertFalse(fallback.hasResume());
    }
}
