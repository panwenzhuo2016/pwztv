package com.fongmi.android.tv.api.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.regex.Pattern;

public final class SourceValidator {
    public enum Kind { LIVE, VOD }
    private static final Pattern STREAM = Pattern.compile("(?m)^https?://[^\\s]+\\s*$");
    private static final Pattern TEXT_STREAM = Pattern.compile("(?m)^[^#\\n,]+,https?://[^\\s]+");

    // 校验响应结构避免把网页或网址当配置。
    public static boolean valid(String text, Kind kind) {
        if (text == null) return false;
        text = text.replace("\uFEFF", "").trim();
        if (kind == Kind.LIVE) {
            return text.startsWith("#EXTM3U") && text.contains("#EXTINF:") && STREAM.matcher(text).find()
                    || !text.startsWith("<") && TEXT_STREAM.matcher(text).find();
        }
        try {
            JsonElement parsed = JsonParser.parseString(text);
            if (!parsed.isJsonObject()) return false;
            JsonObject object = parsed.getAsJsonObject();
            if (!object.has("sites") || !object.get("sites").isJsonArray() || object.getAsJsonArray("sites").isEmpty()) return false;
            for (JsonElement element : object.getAsJsonArray("sites")) {
                if (!element.isJsonObject()) continue;
                JsonObject site = element.getAsJsonObject();
                if (site.has("key") && site.get("key").isJsonPrimitive() && site.has("api") && site.get("api").isJsonPrimitive()
                        && !site.get("key").getAsString().isBlank() && !site.get("api").getAsString().isBlank()) return true;
            }
            return false;
        } catch (RuntimeException error) {
            return false;
        }
    }
}
