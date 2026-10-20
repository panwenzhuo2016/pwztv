package com.fongmi.android.tv.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StringMapTest {
    // 反查旧序列化参数可恢复且副本修改不污染原值。
    @Test public void restoresLegacySerializedFiltersAndKeepsCopiesIndependent() throws Exception {
        HashMap<String, String> original = new HashMap<>(Map.of("year", "2026", "area", "中国"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(original);
        }
        Object restored;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = input.readObject();
        }
        // 校验并复制字符串映射，保留空键值兼容性。
        HashMap<String, String> filters = StringMap.copy(restored);
        assertEquals(original, filters);
        filters.put("year", "2025");
        filters.remove("area");
        assertEquals("2025", filters.get("year"));
        assertFalse(filters.containsKey("area"));
        assertEquals("2026", ((Map<?, ?>) restored).get("year"));
        assertEquals("中国", ((Map<?, ?>) restored).get("area"));
    }

    // 反查代理响应头完整保留且空参数可写。
    @Test public void preservesResponseHeadersAndAllowsMissingFilters() {
        Map<String, String> source = Map.of("Content-Type", "video/mp2t", "X-Token", "abc");
        // 校验并复制字符串映射，保留空键值兼容性。
        HashMap<String, String> headers = StringMap.copy(source);
        assertEquals(source, headers);
        // 校验并复制字符串映射，保留空键值兼容性。
        HashMap<String, String> empty = StringMap.copy(null);
        assertTrue(empty.isEmpty());
        empty.put("year", "2026");
        assertEquals("2026", empty.get("year"));
    }

    // 校验混合键值及非映射对象被明确拒绝。
    @Test public void rejectsInvalidTypesBeforeTheyReachCallers() {
        for (Object value : new Object[]{Map.of(1, "value"), Map.of("key", 1), "invalid"}) {
            // 校验并复制字符串映射，保留空键值兼容性。
            assertThrows(IllegalArgumentException.class, () -> StringMap.copy(value));
        }
    }

    // 反查历史允许的空键和空值仍被保留。
    @Test public void preservesNullableMapEntries() {
        HashMap<String, String> source = new HashMap<>();
        source.put(null, "value");
        source.put("key", null);
        // 校验并复制字符串映射，保留空键值兼容性。
        HashMap<String, String> copy = StringMap.copy(source);
        assertEquals(source, copy);
        assertTrue(copy.containsKey("key"));
        assertNull(copy.get("key"));
        assertEquals("value", copy.get(null));
    }
}
