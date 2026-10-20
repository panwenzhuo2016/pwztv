package com.fongmi.android.tv.api.loader;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Site;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.utils.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class BaseLoader {

    private final JarLoader jarLoader;
    private final PyLoader pyLoader;
    private final JsLoader jsLoader;
    private volatile String recentJar;

    private static class Loader {
        // 初始化片源加载入口且不提前启动Python。
        static volatile BaseLoader INSTANCE = new BaseLoader();
    }

    public static BaseLoader get() {
        return Loader.INSTANCE;
    }

    // 初始化片源加载入口且不提前启动Python。
    private BaseLoader() {
        jarLoader = new JarLoader();
        // 创建Python片源缓存且暂不启动运行时。
        pyLoader = new PyLoader();
        jsLoader = new JsLoader();
    }

    // 清理各类片源的加载缓存。
    public void clear() {
        App.execute(() -> {
            jarLoader.clear();
            // 清理已创建的Python片源实例。
            pyLoader.clear();
            jsLoader.clear();
        });
    }

    // 按脚本类型分派到对应片源加载器。
    public Spider getSpider(String key, String api, String ext, String jar) {
        boolean js = api.contains(".js");
        boolean py = api.contains(".py");
        boolean csp = api.startsWith("csp_");
        // 获取Python片源或返回不可用结果。
        if (py) return pyLoader.getSpider(key, api, ext);
        else if (js) return jsLoader.getSpider(key, api, ext, jar);
        else if (csp) return jarLoader.getSpider(key, api, ext, jar);
        else return new SpiderNull();
    }

    public Spider getSpider(String key) {
        Site site = VodConfig.get().getSite(key);
        Live live = LiveConfig.get().getLive(key);
        if (!site.isEmpty()) return site.spider();
        if (!live.isEmpty()) return live.spider();
        return new SpiderNull();
    }

    // 记录当前来源并保留按需解析的插件地址。
    public void setRecent(String key, String api, String jar) {
        boolean js = api.contains(".js");
        boolean py = api.contains(".py");
        boolean csp = api.startsWith("csp_");
        if (js) jsLoader.setRecent(key);
        // 记录最近使用的Python片源。
        else if (py) pyLoader.setRecent(key);
        else if (csp) {
            recentJar = jar;
            jarLoader.setRecent(Util.md5(jar));
        }
    }

    // 按来源分派片源代理请求。
    public Object[] proxy(Map<String, String> params) throws Exception {
        if (params.containsKey("siteKey")) return getSpider(params.get("siteKey")).proxy(params);
        if ("js".equals(params.get("do"))) return jsLoader.proxy(params);
        // 处理最近使用的Python片源代理请求。
        if ("py".equals(params.get("do"))) return pyLoader.proxy(params);
        return jarLoader.proxy(params);
    }

    // 按需加载插件并记录当前解析来源。
    public void parseJar(String jar, boolean recent) {
        if (TextUtils.isEmpty(jar)) return;
        jarLoader.parseJar(Util.md5(jar), jar);
        if (recent) {
            recentJar = jar;
            jarLoader.setRecent(Util.md5(jar));
        }
    }

    // 后台预加载可选插件且失败不阻断配置。
    public void prepareJar(String jar, boolean recent) {
        if (TextUtils.isEmpty(jar)) return;
        String key = Util.md5(jar);
        if (recent) {
            recentJar = jar;
            jarLoader.setRecent(key);
        }
        App.execute(() -> {
            try {
                jarLoader.parseJar(key, jar);
            } catch (RuntimeException error) {
                SpiderDebug.log("config", "source=%s optional plugin preload failed: %s", key, error.getClass().getSimpleName());
            }
        });
    }

    public DexClassLoader dex(String jar) {
        return jarLoader.dex(jar);
    }

    // 使用插件解析前等待或重试对应插件加载。
    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        // 按需加载插件并记录当前解析来源。
        parseJar(recentJar, false);
        return jarLoader.jsonExt(key, jxs, url);
    }

    // 使用插件混合解析前等待或重试插件加载。
    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        // 按需加载插件并记录当前解析来源。
        parseJar(recentJar, false);
        return jarLoader.jsonExtMix(flag, key, name, jxs, url);
    }
}
