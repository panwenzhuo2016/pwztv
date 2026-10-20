package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.BuildConfig;
import com.github.catvod.crawler.Spider;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class PyLoaderTest {

    // 反查默认构建的类路径不包含Python运行时。
    @Test public void excludesPythonRuntimeFromDefaultBuild() {
        assertFalse(BuildConfig.PYTHON_ENABLED);
        for (String name : new String[]{"com.chaquo.python.Python", "com.fongmi.chaquo.Loader"}) {
            try {
                Class.forName(name, false, getClass().getClassLoader());
                fail("Default build includes Python class: " + name);
            } catch (ClassNotFoundException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }

    // 验证误选Python源后可清理且不生成假结果。
    @Test public void unsupportedSourceDoesNotInitializeARuntimeOrProxy() throws Exception {
        // 创建Python片源缓存且暂不启动运行时。
        PyLoader loader = new PyLoader();
        // 记录最近使用的Python片源。
        loader.setRecent("example");
        // 获取Python片源或返回不可用结果。
        Spider spider = loader.getSpider("example", "https://example.org/source.py", "");
        assertEquals("example", spider.siteKey);
        assertEquals("", spider.searchContent("test", false));
        // 处理最近使用的Python片源代理请求。
        assertNull(loader.proxy(Map.of("do", "py")));
        // 清理已创建的Python片源实例。
        loader.clear();
        // 处理最近使用的Python片源代理请求。
        assertNull(loader.proxy(Map.of("do", "py")));
    }
}
