package com.fongmi.android.tv.api.source;

import org.junit.Test;

import static org.junit.Assert.*;

public class SourceRulesTest {
    // 验证混合清单按分组归类且地区标识优先。
    @Test public void classifiesMixedPlaylistsWithoutOverridingStableRegion() {
        // 优先频道地区标识再按明确分组判断归属。
        assertTrue(ChannelIdentity.domestic("", false, "港台频道"));
        // 优先频道地区标识再按明确分组判断归属。
        assertTrue(ChannelIdentity.domestic("", false, "港澳代理"));
        // 优先频道地区标识再按明确分组判断归属。
        assertTrue(ChannelIdentity.domestic("", false, "地方频道"));
        // 优先频道地区标识再按明确分组判断归属。
        assertFalse(ChannelIdentity.domestic("", true, "国际频道"));
        // 优先频道地区标识再按明确分组判断归属。
        assertFalse(ChannelIdentity.domestic("ABC.us@HD", true, "港台频道"));
        // 优先频道地区标识再按明确分组判断归属。
        assertTrue(ChannelIdentity.domestic("CCTV1.cn", false, "国际频道"));
        // 优先频道地区标识再按明确分组判断归属。
        assertTrue(ChannelIdentity.domestic("", true, null));
        // 优先频道地区标识再按明确分组判断归属。
        assertFalse(ChannelIdentity.domestic("", false, "General"));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.HMT, ChannelIdentity.category("香港卫视", "", "港台频道", true));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.HMT, ChannelIdentity.category("中視新聞", "123", "港澳台", true));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.HMT, ChannelIdentity.category("中視新聞", "123", "臺灣", true));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.LOCAL, ChannelIdentity.category("地方台", "", "地方频道", true));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.WORLD, ChannelIdentity.category("ABC", "ABC.us", "港台频道", false));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.CCTV, ChannelIdentity.category("CCTV1", "CCTV1.cn", "港台频道", true));
    }

    // 验证有限分类与公网地址过滤边界。
    @Test public void classifiesChannelsAndRejectsPrivateRoutes() {
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.CCTV, ChannelIdentity.category("CCTV1", "CCTV1.cn", "General", true));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.NEWS, ChannelIdentity.category("News", "ABC.us", "News;General", false));
        // 将国内外频道归入有限的节目分类。
        assertEquals(ChannelIdentity.Category.HMT, ChannelIdentity.category("Test", "Test.hk@HD", "", true));
        // 按央视主台序号排序避免默认进入特种台。
        assertTrue(ChannelIdentity.order("CCTV-1") < ChannelIdentity.order("CCTV-8K"));
        for (String url : new String[]{"rtp://239.1.1.1:1234", "http://127.0.0.1/a", "http://192.168.1.1/a", "http://172.16.1.1/a", "http://224.0.0.1/a", "https://[::1]/a"}) {
            // 过滤无法从公网直接访问的地址。
            assertFalse(LiveMerger.publicUrl(url));
        }
        // 过滤无法从公网直接访问的地址。
        assertTrue(LiveMerger.publicUrl("https://example.com/live.m3u8"));
        // 过滤无法从公网直接访问的地址。
        assertTrue(LiveMerger.publicUrl("https://[2001:db8::1]/live.m3u8"));
    }
    // 拒绝网页与普通网址冒充订阅内容。
    @Test public void rejectsInvalidSubscriptions() {
        for (String text : new String[]{"<html>error</html>", "https://example.com", "{}", "[]"}) {
            // 校验响应结构避免把网页或网址当配置。
            assertFalse(SourceValidator.valid(text, SourceValidator.Kind.LIVE));
            // 校验响应结构避免把网页或网址当配置。
            assertFalse(SourceValidator.valid(text, SourceValidator.Kind.VOD));
        }
        // 校验响应结构避免把网页或网址当配置。
        assertTrue(SourceValidator.valid("#EXTM3U\n#EXTINF:-1,Test\nhttps://example.com/a.m3u8", SourceValidator.Kind.LIVE));
        // 校验响应结构避免把网页或网址当配置。
        assertTrue(SourceValidator.valid("Test,https://example.com/a.m3u8", SourceValidator.Kind.LIVE));
        // 校验响应结构避免把网页或网址当配置。
        assertTrue(SourceValidator.valid("{\"sites\":[{\"key\":\"a\",\"api\":\"https://example.com\"}]}", SourceValidator.Kind.VOD));
    }

    // 同名异国与央视地区频道保持独立。
    @Test public void preservesRegionalChannelIdentity() {
        // 使用稳定频道标识合并同台备用线路。
        String cctv = ChannelIdentity.key("CCTV1.cn@HD", "CCTV-1 高清", "", true);
        // 使用稳定频道标识合并同台备用线路。
        assertEquals(cctv, ChannelIdentity.key("", "CCTV1", "", true));
        // 使用稳定频道标识合并同台备用线路。
        assertNotEquals(ChannelIdentity.key("CCTV4Asia.cn", "CCTV4", "", true), ChannelIdentity.key("CCTV4Europe.cn", "CCTV4", "", true));
        // 使用稳定频道标识合并同台备用线路。
        assertNotEquals(ChannelIdentity.key("ABC.au", "ABC", "", false), ChannelIdentity.key("ABC.us", "ABC", "", false));
        // 使用稳定频道标识合并同台备用线路。
        assertNotEquals(ChannelIdentity.key("", "ABC", "AU", false), ChannelIdentity.key("", "ABC", "US", false));
    }

    // 反查跨站同编号与搜索次数上限。
    @Test public void boundsVideoFallbackWithoutCrossSiteIdCollision() {
        // 为一次点播设置有限的换站次数。
        VideoFallback fallback = new VideoFallback(2);
        // 使用站点与影片组合记录已试来源。
        fallback.fail("a", "1");
        // 禁止重复尝试并限制本轮换站次数。
        assertFalse(fallback.allows("a", "1"));
        // 禁止重复尝试并限制本轮换站次数。
        assertTrue(fallback.allows("b", "1"));
        // 每轮失败最多启动一次跨站搜索。
        assertTrue(fallback.beginSearch());
        // 每轮失败最多启动一次跨站搜索。
        assertFalse(fallback.beginSearch());
        // 使用站点与影片组合记录已试来源。
        fallback.fail("b", "1");
        // 禁止重复尝试并限制本轮换站次数。
        assertFalse(fallback.allows("c", "1"));
    }

    // 反查片名年份集数与恢复后的新进度。
    @Test public void preservesEpisodeAndRejectsDifferentYearOrSeason() {
        // 为一次点播设置有限的换站次数。
        VideoFallback fallback = new VideoFallback(20);
        // 记录原始影片身份防止切到同名异片。
        fallback.target("示例第2季", "2024");
        // 按片名与已知年份校验候选影片。
        assertFalse(fallback.matches("示例第1季", "2024"));
        // 按片名与已知年份校验候选影片。
        assertFalse(fallback.matches("示例第2季", "2023"));
        // 保存换站前集数与内存播放进度。
        fallback.remember("第12集", 12345, false);
        // 按集名或相同数字匹配续播集数。
        assertTrue(fallback.matchesEpisode("12", false));
        // 按集名或相同数字匹配续播集数。
        assertFalse(fallback.matchesEpisode("第1集", true));
        // 获取换站前的内存播放进度。
        assertEquals(12345, fallback.position());
        // 播放恢复后清空本轮失败与续播状态。
        fallback.recovered();
        // 判断是否存在待续播的集数。
        assertFalse(fallback.hasResume());
        // 保存换站前集数与内存播放进度。
        fallback.remember("第13集", 70000, false);
        // 获取换站前的内存播放进度。
        assertEquals(70000, fallback.position());
        // 播放恢复后清空本轮失败与续播状态。
        fallback.recovered();
        // 保存换站前集数与内存播放进度。
        fallback.remember("S01E12", 80000, false);
        // 按集名或相同数字匹配续播集数。
        assertFalse(fallback.matchesEpisode("S01E13", false));
        // 按集名或相同数字匹配续播集数。
        assertFalse(fallback.matchesEpisode("S02E12", false));
        // 按集名或相同数字匹配续播集数。
        assertTrue(fallback.matchesEpisode("第1季第12集", false));
    }
}
