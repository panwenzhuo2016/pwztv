package com.fongmi.android.tv.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class PlaybackSupportTest {
    // 验证下载协议被拒绝且常规播放协议保留。
    @Test public void rejectsRemovedProtocolsWithoutRejectingNormalPlayback() {
        for (String url : List.of("magnet:?xt=urn:btih:123", "ED2K://file", "thunder://abc", "jianpian://abc",
                "tvbox-xg://abc", "xg://abc", "xgplay://abc", "ftp://example.com/a.mp4",
                "https://example.com/a.torrent", "https://example.com/A.TORRENT?token=1", "file:///a.torrent;1")) {
            // 识别已移除的下载与P2P播放地址。
            assertTrue(url, PlaybackSupport.removedUrl(url));
        }
        for (String url : List.of("", "https://example.com/a.m3u8", "https://example.com/a.mp4?url=magnet:abc",
                "rtsp://example.com/live", "rtmp://example.com/live", "mitv://example.com/live",
                "https://example.com/a.torrent.mp4", "https://example.com/a.m3u8?name=a.torrent")) {
            // 识别已移除的下载与P2P播放地址。
            assertFalse(url, PlaybackSupport.removedUrl(url));
        }
        // 识别已移除的下载与P2P播放地址。
        assertFalse(PlaybackSupport.removedUrl(null));
    }

    // 反查更新后的配置不会恢复被移除片源。
    @Test public void filtersReintroducedAndRenamedPluginSources() {
        JsonArray refreshed = JsonParser.parseString("["
                + "{\"key\":\"贱贱\",\"api\":\"csp_JPJGuard\"},"
                + "{\"key\":\"新6V\",\"api\":\"csp_SixV\"},"
                + "{\"key\":\"renamed\",\"api\":\"csp_SixVGuard\"},"
                + "{\"key\":\"other\",\"api\":\"csp_JPJ\"},"
                + "{\"key\":\"normal\",\"api\":\"https://example.com/api\"}]").getAsJsonArray();
        // 过滤依赖已移除引擎的已知片源。
        List<String> retained = refreshed.asList().stream().map(JsonElement::getAsJsonObject)
                .filter(site -> !PlaybackSupport.removedSite(site.get("key").getAsString(), site.get("api").getAsString()))
                .map(site -> site.get("key").getAsString()).toList();
        assertEquals(List.of("normal"), retained);
        // 过滤依赖已移除引擎的已知片源。
        assertFalse(PlaybackSupport.removedSite(null, null));
    }

    // 定位项目文件以反查真实配置与依赖。
    private Path root() {
        return Files.isDirectory(Path.of("src/main/assets")) ? Path.of("..") : Path.of("");
    }

    // 反查两份配置删除生效且保留119个片源。
    @Test public void bundledSourcesKeepNormalSitesAndLiveEntries() throws Exception {
        Set<String> keys = new HashSet<>();
        // 定位项目文件以反查真实配置与依赖。
        Path assets = root().resolve("app/src/main/assets");
        for (String file : List.of("sources/vod.json", "config/0821.json")) {
            var config = JsonParser.parseString(Files.readString(assets.resolve(file))).getAsJsonObject();
            assertFalse(config.getAsJsonArray("lives").isEmpty());
            for (JsonElement entry : config.getAsJsonArray("sites")) {
                var site = entry.getAsJsonObject();
                // 过滤依赖已移除引擎的已知片源。
                assertFalse(PlaybackSupport.removedSite(site.get("key").getAsString(), site.get("api").getAsString()));
                if (file.equals("sources/vod.json")) keys.add(site.get("key").getAsString());
            }
        }
        for (JsonElement site : JsonParser.parseString(Files.readString(assets.resolve("sources/direct.json"))).getAsJsonArray()) {
            keys.add(site.getAsJsonObject().get("key").getAsString());
        }
        assertEquals(119, keys.size());
        assertFalse(keys.contains("贱贱"));
        assertFalse(keys.contains("新6V"));
    }

    // 反查两个大型引擎依赖确实从工程删除。
    @Test public void removedEnginesAreAbsentFromDependencies() {
        // 定位项目文件以反查真实配置与依赖。
        Path libs = root().resolve("app/libs");
        assertFalse(Files.exists(libs.resolve("jianpian-release.aar")));
        assertFalse(Files.exists(libs.resolve("thunder-release.aar")));
        assertTrue(Files.exists(libs.resolve("forcetech-release.aar")));
        assertTrue(Files.exists(libs.resolve("tvbus-release.aar")));
    }
}
