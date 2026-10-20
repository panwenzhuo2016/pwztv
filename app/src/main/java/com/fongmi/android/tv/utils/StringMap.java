package com.fongmi.android.tv.utils;

import java.util.HashMap;
import java.util.Map;

public final class StringMap {
    // 校验并复制字符串映射，保留空键值兼容性。
    public static HashMap<String, String> copy(Object value) {
        HashMap<String, String> result = new HashMap<>();
        if (value == null) return result;
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("Expected a string map");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object item = entry.getValue();
            if (key != null && !(key instanceof String) || item != null && !(item instanceof String)) {
                throw new IllegalArgumentException("Expected string map keys and values");
            }
            result.put((String) key, (String) item);
        }
        return result;
    }
}
