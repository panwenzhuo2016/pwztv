package com.fongmi.android.tv.player;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.bean.Parse;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.impl.ParseCallback;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.custom.CustomWebView;
import com.fongmi.android.tv.utils.UrlUtil;
import com.fongmi.android.tv.utils.FailureGuard;
import com.fongmi.android.tv.utils.WebViewUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;

public class ParseJob implements ParseCallback {

    private final List<CustomWebView> webViews;
    private ExecutorService executor;
    private ExecutorService infinite;
    private ParseCallback callback;
    private Parse parse;

    public static ParseJob create(ParseCallback callback) {
        return new ParseJob(callback);
    }

    public ParseJob(ParseCallback callback) {
        this.executor = Executors.newFixedThreadPool(2);
        this.infinite = Executors.newCachedThreadPool();
        this.webViews = new ArrayList<>();
        this.callback = callback;
    }

    // 配置或提交解析失败时交付失败并释放资源。
    public ParseJob start(Result result, boolean useParse) {
        // 隔离可恢复异常并返回是否完成当前操作。
        if (!FailureGuard.run("parse.start", () -> {
            setParse(result, useParse);
            // 执行解析并记录超时与取消异常。
            execute(result);
        })) onParseError();
        return this;
    }

    private void setParse(Result result, boolean useParse) {
        if (useParse) parse = VodConfig.get().getParse();
        if (result.getPlayUrl().startsWith("json:")) parse = Parse.get(1, result.getPlayUrl().substring(5));
        if (result.getPlayUrl().startsWith("parse:")) parse = VodConfig.get().getParse(result.getPlayUrl().substring(6));
        if (parse == null || parse.isEmpty()) parse = Parse.get(0, result.getPlayUrl());
        parse.setHeader(result.getHeader());
        parse.setClick(getClick(result));
    }

    private String getClick(Result result) {
        String click = VodConfig.get().getSite(result.getKey()).getClick();
        if (!TextUtils.isEmpty(click)) return click;
        return result.getClick();
    }

    // 执行解析并记录超时与取消异常。
    private void execute(Result result) {
        executor.execute(() -> {
            try {
                // 解析失败时保留来源与原始异常。
                executor.submit(getTask(result)).get(Constant.TIMEOUT_PARSE_DEF, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                SpiderDebug.log("parse", "key=%s,stage=wait", result.getKey());
                // 将异常堆栈同时交给本机诊断适配器。
                SpiderDebug.log(e);
                onParseError();
            }
        });
    }

    // 解析失败时保留来源与原始异常。
    private Runnable getTask(Result result) {
        return () -> {
            try {
                doInBackground(result.getKey(), result.getUrl().v(), result.getFlag());
            } catch (Throwable e) {
                SpiderDebug.log("parse", "key=%s,stage=resolve", result.getKey());
                // 将异常堆栈同时交给本机诊断适配器。
                SpiderDebug.log(e);
                onParseError();
            }
        };
    }

    private void doInBackground(String key, String webUrl, String flag) throws Throwable {
        switch (parse.getType()) {
            case 0: //嗅探
                startWeb(key, parse, webUrl);
                break;
            case 1: //Json
                jsonParse(parse, webUrl, true);
                break;
            case 2: //Json擴展
                jsonExtend(webUrl);
                break;
            case 3: //Json聚合
                jsonMix(webUrl, flag);
                break;
            case 4: //超級解析
                godParse(webUrl, flag);
                break;
        }
    }

    private void jsonParse(Parse item, String webUrl, boolean error) throws Exception {
        try (Response res = OkHttp.newCall(item.getUrl() + webUrl, item.getHeader()).execute()) {
            JsonObject object = Json.parse(res.body().string()).getAsJsonObject();
            String url = Json.safeString(object, "url");
            JsonObject data = object.getAsJsonObject("data");
            if (url.isEmpty()) url = Json.safeString(data, "url");
            checkResult(getHeader(object), url, item.getName(), error);
        }
    }

    private void jsonExtend(String webUrl) throws Throwable {
        LinkedHashMap<String, String> jxs = new LinkedHashMap<>();
        for (Parse item : VodConfig.get().getParses()) if (item.getType() == 1) jxs.put(item.getName(), item.extUrl());
        // 使用插件解析前等待或重试对应插件加载。
        checkResult(Result.fromObject(BaseLoader.get().jsonExt(parse.getUrl(), jxs, webUrl)));
    }

    private void jsonMix(String webUrl, String flag) throws Throwable {
        LinkedHashMap<String, HashMap<String, String>> jxs = new LinkedHashMap<>();
        for (Parse item : VodConfig.get().getParses()) jxs.put(item.getName(), item.mixMap());
        // 使用插件混合解析前等待或重试插件加载。
        checkResult(Result.fromObject(BaseLoader.get().jsonExtMix(flag, parse.getUrl(), parse.getName(), jxs, webUrl)));
    }

    private void godParse(String webUrl, String flag) throws Exception {
        List<Parse> json = VodConfig.get().getParses(1, flag);
        List<Parse> webs = VodConfig.get().getParses(0, flag);
        int count = json.size() + (webs.isEmpty() ? 0 : 1);
        CountDownLatch latch = new CountDownLatch(count);
        for (Parse item : json) infinite.execute(() -> jsonParse(latch, item, webUrl));
        if (!webs.isEmpty()) startWeb(webs, webUrl);
        latch.await();
        onParseError();
    }

    // 记录并行解析异常并释放等待计数。
    private void jsonParse(CountDownLatch latch, Parse item, String webUrl) {
        try {
            jsonParse(item, webUrl, false);
        } catch (Exception | LinkageError e) {
            // 使用系统日志避免再次进入诊断适配器。
            FailureGuard.report("parse.parallel:" + item.getName(), e);
        } finally {
            latch.countDown();
        }
    }

    private void checkResult(Map<String, String> headers, String url, String from, boolean error) {
        if (url.length() > 40) {
            onParseSuccess(headers, url, from);
        } else if (error) {
            onParseError();
        }
    }

    private void checkResult(Result result) {
        result.setHeader(parse.getHeader());
        if (result.getUrl().isEmpty()) onParseError();
        else if (result.getParse() == 1) startWeb(result.getHeader(), UrlUtil.convert(result.getUrl().v()));
        else onParseSuccess(result.getHeader(), result.getUrl().v(), result.getJxFrom());
    }

    private void startWeb(List<Parse> items, String webUrl) {
        StringBuilder sb = new StringBuilder();
        for (Parse item : items) sb.append(item.getUrl()).append(";");
        startWeb(new HashMap<>(), Server.get().getAddress("/parse?jxs=" + Util.substring(sb.toString()) + "&url=" + webUrl));
    }

    private void startWeb(String key, Parse item, String webUrl) {
        startWeb(key, item.getName(), item.getHeader(), item.getUrl() + webUrl, item.getClick());
    }

    private void startWeb(Map<String, String> headers, String url) {
        startWeb("", "", headers, url, "");
    }

    // 网页解析启动失败时回退到解析失败流程。
    private void startWeb(String key, String from, Map<String, String> headers, String url, String click) {
        if (WebViewUtil.support()) {
            App.post(() -> {
                if (callback == null) return;
                // 隔离可恢复异常并返回是否完成当前操作。
                if (!FailureGuard.run("parse.webStart:" + key, () -> {
                    CustomWebView view = CustomWebView.create(App.get());
                    webViews.add(view);
                    view.start(key, from, headers, url, click, this, !url.contains("player/?url="));
                })) onParseError();
            });
        } else {
            onParseError();
        }
    }

    private Map<String, String> getHeader(JsonObject object) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) if (!entry.getValue().isJsonNull() && (entry.getKey().equalsIgnoreCase(HttpHeaders.USER_AGENT) || entry.getKey().equalsIgnoreCase(HttpHeaders.REFERER) || entry.getKey().equalsIgnoreCase(HttpHeaders.COOKIE) || entry.getKey().equalsIgnoreCase("ua"))) headers.put(UrlUtil.fixHeader(entry.getKey()), entry.getValue().getAsString());
        if (headers.isEmpty()) return parse.getHeader();
        return headers;
    }

    @Override
    // 成功回调异常时仍通知失败并释放解析资源。
    public void onParseSuccess(Map<String, String> headers, String url, String from) {
        App.post(() -> {
            ParseCallback target = callback;
            callback = null;
            try {
                // 隔离可恢复异常并返回是否完成当前操作。
                if (target != null && !FailureGuard.run("parse.success", () -> target.onParseSuccess(headers, url, from))) {
                    // 隔离可恢复异常并返回是否完成当前操作。
                    FailureGuard.run("parse.successFallback", target::onParseError);
                }
            } finally {
                // 终止解析任务并逐个释放网页资源。
                stop();
            }
        });
    }

    @Override
    // 失败回调异常时仍释放本轮解析资源。
    public void onParseError() {
        App.post(() -> {
            ParseCallback target = callback;
            callback = null;
            try {
                // 隔离可恢复异常并返回是否完成当前操作。
                if (target != null) FailureGuard.run("parse.error", target::onParseError);
            } finally {
                // 终止解析任务并逐个释放网页资源。
                stop();
            }
        });
    }

    // 单个网页释放失败不阻断其余网页清理。
    private void stopWeb() {
        List<CustomWebView> pending = new ArrayList<>(webViews);
        webViews.clear();
        for (CustomWebView webView : pending) {
            // 隔离可恢复异常并返回是否完成当前操作。
            FailureGuard.run("parse.webStop", () -> webView.stop(false));
            // 隔离可恢复异常并返回是否完成当前操作。
            FailureGuard.run("parse.webDestroy", webView::destroy);
        }
    }

    // 终止解析任务并逐个释放网页资源。
    public void stop() {
        // 隔离可恢复异常并返回是否完成当前操作。
        if (executor != null) FailureGuard.run("parse.executorStop", executor::shutdownNow);
        // 隔离可恢复异常并返回是否完成当前操作。
        if (infinite != null) FailureGuard.run("parse.infiniteStop", infinite::shutdownNow);
        infinite = null;
        executor = null;
        callback = null;
        // 单个网页释放失败不阻断其余网页清理。
        stopWeb();
    }
}
