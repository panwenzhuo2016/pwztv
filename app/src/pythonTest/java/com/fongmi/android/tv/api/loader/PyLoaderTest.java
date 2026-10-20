package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.BuildConfig;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

public class PyLoaderTest {

    // 验证完整构建保留运行时且初始化入口不启动它。
    @Test public void includesPythonWithoutEagerlyStartingIt() throws Exception {
        assertTrue(BuildConfig.PYTHON_ENABLED);
        assertNotNull(Class.forName("com.chaquo.python.Python", false, getClass().getClassLoader()));
        // 创建Python片源缓存且暂不启动运行时。
        PyLoader loader = new PyLoader();
        Field runtime = PyLoader.class.getDeclaredField("loader");
        runtime.setAccessible(true);
        assertNull(runtime.get(loader));
        // 记录最近使用的Python片源。
        loader.setRecent("example");
        // 清理已创建的Python片源实例。
        loader.clear();
        // 处理最近使用的Python片源代理请求。
        assertNull(loader.proxy(Map.of("do", "py")));
        assertNull(runtime.get(loader));
    }
}
