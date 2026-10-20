package com.fongmi.android.tv.api.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class BuiltinConfigTest {
    // 读取真实随包配置作为离线回归样本。
    private String snapshot() throws IOException {
        Path path = Path.of("src/main/assets/sources/vod.json");
        if (!Files.exists(path)) path = Path.of("app").resolve(path);
        return Files.readString(path);
    }

    // 反查内置配置重载后仍有站点和直播入口。
    @Test public void loadsBuiltinRepeatedlyWithoutAnHttpRequest() throws Exception {
        AtomicInteger localReads = new AtomicInteger();
        AtomicInteger remoteReads = new AtomicInteger();
        // 读取真实随包配置作为离线回归样本。
        String snapshot = snapshot();
        for (int reload = 0; reload < 2; reload++) {
            // 统一读取内置资源与远程配置。
            String loaded = BuiltinConfig.load("builtin", () -> {
                localReads.incrementAndGet();
                return snapshot;
            }, url -> { throw new AssertionError("builtin reached asset reader"); }, url -> {
                remoteReads.incrementAndGet();
                throw new IllegalArgumentException("Expected URL scheme 'http' or 'https'");
            });
            JsonObject config = JsonParser.parseString(loaded).getAsJsonObject();
            assertFalse(config.getAsJsonArray("sites").isEmpty());
            assertFalse(config.getAsJsonArray("lives").isEmpty());
            assertTrue(config.getAsJsonArray("sites").asList().stream()
                    .anyMatch(site -> "hd1080".equals(site.getAsJsonObject().get("key").getAsString())));
        }
        assertEquals(2, localReads.get());
        assertEquals(0, remoteReads.get());
    }

    // 外部配置失败后可立即恢复内置离线入口。
    @Test public void loadsBuiltinOfflineAfterRemoteFailure() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        BuiltinConfig.Reader offline = url -> {
            requests.incrementAndGet();
            throw new IOException("network unavailable");
        };
        // 读取真实随包配置作为离线回归样本。
        String snapshot = snapshot();
        try {
            // 统一读取内置资源与远程配置。
            BuiltinConfig.load("https://example.com/config.json", () -> snapshot, offline, offline);
            fail("remote failure must remain visible");
        } catch (IOException expected) {
            assertEquals("network unavailable", expected.getMessage());
        }
        // 统一读取内置资源与远程配置。
        String loaded = BuiltinConfig.load(" builtin ", () -> snapshot, offline, offline);
        assertEquals(snapshot, loaded);
        assertEquals(1, requests.get());
    }

    // 资源与远程地址保持各自的读取方式。
    @Test public void preservesAssetAndRemoteConfigurationRoutes() throws Exception {
        String asset = "assets://sources/vod.json";
        String remote = "https://example.com/config.json?token=value";
        AtomicInteger assets = new AtomicInteger();
        AtomicInteger requests = new AtomicInteger();
        BuiltinConfig.Reader assetReader = url -> {
            assertEquals(asset, url);
            assets.incrementAndGet();
            return "asset content";
        };
        BuiltinConfig.Reader remoteReader = url -> {
            assertEquals(remote, url);
            requests.incrementAndGet();
            return "remote content";
        };
        // 统一读取内置资源与远程配置。
        assertEquals("asset content", BuiltinConfig.load(asset, () -> "builtin", assetReader, remoteReader));
        // 统一读取内置资源与远程配置。
        assertEquals("remote content", BuiltinConfig.load(remote, () -> "builtin", assetReader, remoteReader));
        assertEquals(1, assets.get());
        assertEquals(1, requests.get());
    }

    // 空地址在发起任何读取前被明确拒绝。
    @Test public void rejectsEmptyConfigurationBeforeAnyRead() throws Exception {
        for (String url : new String[]{null, "", "  "}) {
            try {
                // 统一读取内置资源与远程配置。
                BuiltinConfig.load(url, () -> { throw new AssertionError("unexpected builtin read"); },
                        value -> { throw new AssertionError("unexpected asset read"); },
                        value -> { throw new AssertionError("unexpected HTTP request"); });
                fail("empty configuration accepted");
            } catch (IllegalArgumentException expected) {
                assertEquals("Configuration URL is empty", expected.getMessage());
            }
        }
    }
}
