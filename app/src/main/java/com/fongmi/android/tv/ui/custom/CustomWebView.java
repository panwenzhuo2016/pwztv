package com.fongmi.android.tv.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.http.SslError;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.impl.ParseCallback;
import com.fongmi.android.tv.ui.dialog.WebDialog;
import com.fongmi.android.tv.utils.Sniffer;
import com.fongmi.android.tv.utils.FailureGuard;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.Util;
import com.google.common.net.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomWebView extends WebView implements DialogInterface.OnDismissListener {

    private static final Pattern PLAYER = Pattern.compile("player.*https?://");
    private static final String BLANK = "about:blank";
    private static final int MAX_URLS = 5;

    private LinkedHashSet<String> urls;
    private WebResourceResponse empty;
    private ParseCallback callback;
    private WebDialog dialog;
    private Runnable timer;
    private boolean detect;
    private boolean stop;
    private String click;
    private String from;
    private String key;
    private String url;

    public static CustomWebView create(@NonNull Context context) {
        return new CustomWebView(context);
    }

    private CustomWebView(@NonNull Context context) {
        super(context);
        // 初始化解析设置并提供非空视频封面。
        initSettings();
    }

    @SuppressLint("SetJavaScriptEnabled")
    // 初始化解析设置并提供非空视频封面。
    public void initSettings() {
        timer = () -> stop(true);
        urls = new LinkedHashSet<>();
        empty = new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
        WebSettings setting = getSettings();
        setting.setSupportZoom(true);
        setting.setUseWideViewPort(true);
        setting.setDatabaseEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setJavaScriptEnabled(true);
        setting.setBuiltInZoomControls(true);
        setting.setDisplayZoomControls(false);
        setting.setLoadWithOverviewMode(true);
        setting.setUserAgentString(Setting.getUa());
        setting.setMediaPlaybackRequiresUserGesture(false);
        setting.setJavaScriptCanOpenWindowsAutomatically(false);
        setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        // 隔离网页请求及页面回调中的插件异常。
        setWebViewClient(webViewClient());
        // 为网页视频提供可复用的非空封面。
        setWebChromeClient(new VideoWebChromeClient());
    }

    // 加载网页失败时结束嗅探并通知解析回退。
    public CustomWebView start(String key, String from, Map<String, String> headers, String url, String click, ParseCallback callback, boolean detect) {
        this.callback = callback;
        this.detect = detect;
        this.click = click;
        this.from = from;
        this.key = key;
        this.url = url;
        // 隔离可恢复异常并返回是否完成当前操作。
        if (!FailureGuard.run("web.start:" + key, () -> {
            App.post(timer, Constant.TIMEOUT_PARSE_WEB);
            start(headers);
        })) stop(true);
        return this;
    }

    private void start(Map<String, String> headers) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        checkHeader(url, headers);
        loadUrl(url, headers);
    }

    private void checkHeader(String url, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            if (HttpHeaders.USER_AGENT.equalsIgnoreCase(key)) getSettings().setUserAgentString(headers.get(key));
            if (HttpHeaders.COOKIE.equalsIgnoreCase(key)) CookieManager.getInstance().setCookie(url, headers.get(key));
        }
    }

    // 隔离网页请求及页面回调中的插件异常。
    private WebViewClient webViewClient() {
        return new WebViewClient() {
            // 异常请求结束本轮解析并返回空响应。
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                try {
                    String url = request.getUrl().toString();
                    String host = request.getUrl().getHost();
                    if (TextUtils.isEmpty(host) || isAd(host)) return empty;
                    Map<String, String> headers = request.getRequestHeaders();
                    if (url.contains("/cdn-cgi/challenge-platform/")) post(() -> showDialog());
                    if (detect && PLAYER.matcher(url).find() && addUrl(url)) onParseAdd(headers, url);
                    else if (isVideoFormat(url)) onParseSuccess(headers, url);
                    return super.shouldInterceptRequest(view, request);
                } catch (RuntimeException | LinkageError error) {
                    // 使用系统日志避免再次进入诊断适配器。
                    FailureGuard.report("web.intercept:" + key, error);
                    post(() -> stop(true));
                    return empty;
                }
            }

            // 页面脚本异常时结束解析并允许切换线路。
            @Override
            public void onPageFinished(WebView view, String url) {
                if (BLANK.equals(url) || stop) return;
                // 隔离可恢复异常并返回是否完成当前操作。
                if (!FailureGuard.run("web.pageFinished:" + key, () -> evaluate(getScript(url), 0))) stop(true);
            }

            @Override
            @SuppressLint("WebViewClientOnReceivedSslError")
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        };
    }

    private boolean addUrl(String url) {
        if (urls.size() > MAX_URLS) urls.clear();
        return urls.add(url);
    }

    // 窗口显示失败时结束本轮网页解析。
    private void showDialog() {
        if (dialog != null || App.activity() == null) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        if (!FailureGuard.run("web.dialog:" + key, () -> {
            if (getParent() != null) ((ViewGroup) getParent()).removeView(this);
            dialog = new WebDialog(this).show();
            App.removeCallbacks(timer);
        })) stop(true);
    }

    private void hideDialog() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        stop(true);
    }

    private List<String> getScript(String url) {
        List<String> script = new ArrayList<>(Sniffer.getScript(Uri.parse(url)));
        if (TextUtils.isEmpty(click) || script.contains(click)) return script;
        script.add(0, click);
        return script;
    }

    // 异步脚本异常时通知解析回退而不逃逸回调。
    private void evaluate(List<String> script, int index) {
        if (stop || index >= script.size()) return;
        // 隔离可恢复异常并返回是否完成当前操作。
        if (!FailureGuard.run("web.script:" + key, () -> {
            String js = script.get(index);
            if (TextUtils.isEmpty(js)) {
                evaluate(script, index + 1);
            } else {
                evaluateJavascript(js, value -> evaluate(script, index + 1));
            }
        })) stop(true);
    }

    private boolean isAd(String host) {
        for (String ad : VodConfig.get().getAds()) if (Util.containOrMatch(host, ad)) return true;
        for (String ad : LiveConfig.get().getAds()) if (Util.containOrMatch(host, ad)) return true;
        return false;
    }

    // 插件格式识别失败时使用内置格式判断。
    private boolean isVideoFormat(String url) {
        try {
            if (!detect && url.equals(this.url)) return false;
            Spider spider = VodConfig.get().getSite(key).spider();
            if (spider.manualVideoCheck()) return spider.isVideoFormat(url);
            return Sniffer.isVideoFormat(url);
        } catch (Exception | LinkageError error) {
            // 使用系统日志避免再次进入诊断适配器。
            FailureGuard.report("web.videoCheck:" + key, error);
            return Sniffer.isVideoFormat(url);
        }
    }

    // 子页面启动失败时回退当前解析任务。
    private void onParseAdd(Map<String, String> headers, String url) {
        post(() -> {
            if (stop || callback == null) return;
            // 隔离可恢复异常并返回是否完成当前操作。
            if (!FailureGuard.run("web.child:" + key, () -> CustomWebView.create(App.get()).start(key, from, headers, url, click, callback, false))) stop(true);
        });
    }

    // 成功回调异常时通知失败并停止网页嗅探。
    private void onParseSuccess(Map<String, String> headers, String url) {
        ParseCallback target = callback;
        callback = null;
        // 隔离可恢复异常并返回是否完成当前操作。
        if (target != null && !FailureGuard.run("web.success:" + key, () -> target.onParseSuccess(headers, url, from))) {
            // 隔离可恢复异常并返回是否完成当前操作。
            FailureGuard.run("web.successFallback:" + key, target::onParseError);
        }
        post(() -> stop(false));
    }

    // 失败回调只交付一次并隔离回调异常。
    private void onParseError() {
        ParseCallback target = callback;
        callback = null;
        // 隔离可恢复异常并返回是否完成当前操作。
        if (target != null) FailureGuard.run("web.error:" + key, target::onParseError);
    }

    // 分步清理网页且清理失败仍通知解析回退。
    public void stop(boolean error) {
        if (stop) return;
        stop = true;
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("web.hideDialog:" + key, this::hideDialog);
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("web.stopLoading:" + key, this::stopLoading);
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("web.clearPage:" + key, () -> loadUrl(BLANK));
        // 隔离可恢复异常并返回是否完成当前操作。
        FailureGuard.run("web.cancelTimer:" + key, () -> App.removeCallbacks(timer));
        if (error) onParseError();
        else callback = null;
    }
}
