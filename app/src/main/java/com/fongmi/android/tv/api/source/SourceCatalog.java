package com.fongmi.android.tv.api.source;

import com.fongmi.android.tv.api.Decoder;
import com.fongmi.android.tv.api.config.BuiltinConfig;
import com.github.catvod.utils.Asset;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;

public final class SourceCatalog {
    public static final String AGGREGATE = "builtin://aggregate";

    // 使用有效点播快照并后台更新订阅。
    public static String vod() {
        // 获取内置订阅与失败切换策略。
        SourceManifest.Entry entry = SourceManifest.get().vod;
        // 获取有效缓存或随包提供的订阅快照。
        String text = SourceSubscriptions.cached(entry, SourceValidator.Kind.VOD);
        // 去重安排订阅更新且不阻塞缓存读取。
        SourceSubscriptions.refresh(entry, SourceValidator.Kind.VOD);
        JsonObject config = JsonParser.parseString(Decoder.fixJson(BuiltinConfig.BASE + "0821.json", text)).getAsJsonObject();
        JsonArray sites = new JsonArray();
        Set<String> keys = new HashSet<>();
        JsonArray direct = JsonParser.parseString(Asset.read("sources/direct.json")).getAsJsonArray();
        for (JsonElement item : direct) {
            sites.add(item);
            keys.add(item.getAsJsonObject().get("key").getAsString());
        }
        for (JsonElement item : config.getAsJsonArray("sites")) {
            if (!item.isJsonObject() || !item.getAsJsonObject().has("key") || !item.getAsJsonObject().get("key").isJsonPrimitive()) continue;
            if (keys.add(item.getAsJsonObject().get("key").getAsString())) sites.add(item);
        }
        config.add("sites", sites);
        JsonArray lives = new JsonArray();
        JsonObject aggregate = new JsonObject();
        aggregate.addProperty("name", "电视直播");
        aggregate.addProperty("url", AGGREGATE);
        lives.add(aggregate);
        // 获取内置订阅与失败切换策略。
        for (SourceManifest.Entry source : SourceManifest.get().live) {
            JsonObject live = new JsonObject();
            live.addProperty("name", source.name);
            live.addProperty("url", "builtin://source/" + source.id);
            lives.add(live);
        }
        Set<String> liveNames = new HashSet<>();
        for (JsonElement item : lives) liveNames.add(item.getAsJsonObject().get("name").getAsString());
        if (config.has("lives") && config.get("lives").isJsonArray()) {
            for (JsonElement item : config.getAsJsonArray("lives")) {
                if (!item.isJsonObject() || !item.getAsJsonObject().has("name") || !item.getAsJsonObject().get("name").isJsonPrimitive()) continue;
                if (liveNames.add(item.getAsJsonObject().get("name").getAsString())) lives.add(item);
            }
        }
        config.add("lives", lives);
        return config.toString();
    }
}
