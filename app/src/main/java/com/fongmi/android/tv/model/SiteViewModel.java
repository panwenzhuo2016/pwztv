package com.fongmi.android.tv.model;

import android.text.TextUtils;

import androidx.collection.ArrayMap;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.source.HomeLoader;
import com.fongmi.android.tv.api.source.SourceHealth;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.DiagnosticLog;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Sniffer;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Prefers;
import com.github.catvod.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Response;

public class SiteViewModel extends ViewModel {

    private final List<Future<?>> searchFuture;
    private final ExecutorService executor;
    private final AtomicInteger taskId;
    private Future<Result> future;
    private final AtomicInteger searchGeneration = new AtomicInteger();

    public final MutableLiveData<Result> result;
    public final MutableLiveData<Result> player;
    public final MutableLiveData<Result> search;
    public final MutableLiveData<Result> action;

    public SiteViewModel() {
        taskId = new AtomicInteger(0);
        result = new MutableLiveData<>();
        player = new MutableLiveData<>();
        search = new MutableLiveData<>();
        action = new MutableLiveData<>();
        searchFuture = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
    }

    public SiteViewModel init() {
        search.setValue(null);
        result.setValue(null);
        player.setValue(null);
        action.setValue(null);
        return this;
    }

    // 首页单源最多等待八秒并自动选择可用来源。
    public void homeContent() {
        int id = taskId.incrementAndGet();
        if (future != null) future.cancel(true);
        if (executor.isShutdown()) return;
        VodConfig vod = VodConfig.get();
        Config config = vod.getConfig();
        Site initial = vod.getHome();
        // 按当前来源起点轮转且同轮仅尝试一次。
        List<Site> sites = HomeLoader.ordered(new ArrayList<>(vod.getSites()), initial,
                site -> !site.isHide() && SourceHealth.get().available("home:" + site.getKey()));
        // 配置单源首页总耗时上限和内容校验。
        HomeLoader<Site, Result> loader = new HomeLoader<>(Constant.TIMEOUT_HOME, App::submit,
                value -> value.getList().stream().anyMatch(item -> !item.getId().isEmpty() && !item.getName().isEmpty()));
        FutureTask<Result> task = new FutureTask<>(() -> {
            HomeDiagnostics diagnostics = new HomeDiagnostics();
            try {
                // 超时或空内容即换源并取消上一来源请求。
                HomeLoader.Outcome<Site, Result> loaded = loader.load(sites, site -> {
                    // 绑定单个首页来源的内容加载任务。
                    return new HomeRequest(connection -> {
                        try {
                            // 使用捕获的来源和可取消连接加载首页。
                            return loadHome(site, connection);
                        } catch (Exception error) {
                            // 保存首个真实异常及片源域名。
                            diagnostics.capture(site.getKey(), site.getApi(), error);
                            throw error;
                        }
                    });
                }, () -> taskId.get() == id && !Thread.currentThread().isInterrupted(), (site, reason) -> {
                    SourceHealth.get().fail("home:" + site.getKey());
                    // 汇总本轮各类来源失败次数。
                    diagnostics.failed(reason);
                });
                // 每轮只导出一次结果汇总及代表异常。
                // 按异常标记写入脱敏诊断缓冲。
                DiagnosticLog.record("home", diagnostics.report(loaded == null ? null : loaded.source().getKey()), true);
                // 在主线程确认首页轮次并同步来源和内容。
                postHome(id, config, initial, loaded);
                return loaded == null ? Result.empty() : loaded.value();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SpiderDebug.log("home", "key=%s,interrupted=true", initial.getKey());
            } catch (CancellationException e) {
                SpiderDebug.log("home", "key=%s,replaced=true", initial.getKey());
            } catch (Exception e) {
                // 汇总本轮各类来源失败次数。
                diagnostics.failed(e.getClass().getSimpleName());
                // 保存首个真实异常及片源域名。
                diagnostics.capture(initial.getKey(), initial.getApi(), e);
                // 每轮只导出一次结果汇总及代表异常。
                // 按异常标记写入脱敏诊断缓冲。
                DiagnosticLog.record("home", diagnostics.report(null), true);
                // 在主线程确认首页轮次并同步来源和内容。
                postHome(id, config, initial, null);
            }
            return Result.empty();
        });
        future = task;
        executor.execute(task);
    }

    // 在主线程确认首页轮次并同步来源和内容。
    private void postHome(int id, Config config, Site initial, HomeLoader.Outcome<Site, Result> loaded) {
        App.post(() -> {
            VodConfig vod = VodConfig.get();
            if (taskId.get() != id || vod.getConfig() != config || !initial.equals(vod.getHome())) return;
            if (loaded == null) {
                result.setValue(Result.error(ResUtil.getString(R.string.error_home_unavailable)));
                return;
            }
            SourceHealth.get().success("home:" + loaded.source().getKey());
            if (!initial.equals(loaded.source())) {
                vod.setHome(loaded.source());
                Notify.show(ResUtil.getString(R.string.home_source_switched, loaded.source().getName()));
            }
            loaded.value().setKey(loaded.source().getKey());
            result.setValue(loaded.value());
        });
    }

    // 使用捕获的来源和可取消连接加载首页。
    private Result loadHome(Site site, HomeRequest connection) throws Exception {
        if (site.getType() == 3) {
            Spider spider = site.recent().spider();
            boolean crash = Prefers.getBoolean("crash");
            String homeContent = crash ? "" : spider.homeContent(true);
            String homeVideoContent = crash ? "" : spider.homeVideoContent();
            Prefers.put("crash", false);
            SpiderDebug.log("home", homeContent);
            SpiderDebug.log("homeVideo", homeVideoContent);
            Result result = Result.fromJson(homeContent);
            List<Vod> list = Result.fromJson(homeVideoContent).getList();
            if (!list.isEmpty()) result.setList(list);
            // 按来源配置整理首页分类和筛选项。
            setTypes(site, result);
            return result;
        } else if (site.getType() == 4) {
            ArrayMap<String, String> params = new ArrayMap<>();
            params.put("filter", "true");
            if (site.getExt().startsWith("http")) {
                // 登记连接使超时和换源能立即取消网络请求。
                try (Response response = connection.execute(OkHttp.newCall(site.getExt()))) {
                    String extend = response.body().string();
                    if (!extend.isEmpty()) site.setExt(extend);
                }
            }
            // 复用采集请求并支持首页连接取消。
            String homeContent = call(site, params, connection);
            SpiderDebug.log("home", homeContent);
            Result result = Result.fromJson(homeContent);
            // 按来源配置整理首页分类和筛选项。
            setTypes(site, result);
            return result;
        } else {
            // 登记连接使超时和换源能立即取消网络请求。
            try (Response response = connection.execute(OkHttp.newCall(site.getApi(), site.getHeader()))) {
                String homeContent = response.body().string();
                SpiderDebug.log("home", homeContent);
                Result result = Result.fromType(site.getType(), homeContent);
                // 按来源配置整理首页分类和筛选项。
                setTypes(site, result);
                // 按需补全详情并纳入首页连接取消范围。
                fetchPic(site, result, connection);
                return result;
            }
        }
    }

    public void categoryContent(String key, String tid, String page, boolean filter, HashMap<String, String> extend) {
        // 捕获本次任务避免新请求替换等待中的结果。
        execute(result, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("category", "key=%s,tid=%s,page=%s,filter=%s,extend=%s", key, tid, page, filter, extend);
            if (site.getType() == 3) {
                Spider spider = site.recent().spider();
                String categoryContent = spider.categoryContent(tid, page, filter, extend);
                SpiderDebug.log("category", categoryContent);
                return Result.fromJson(categoryContent);
            } else {
                ArrayMap<String, String> params = new ArrayMap<>();
                if (site.getType() == 1 && !extend.isEmpty()) params.put("f", App.gson().toJson(extend));
                if (site.getType() == 4) params.put("ext", Util.base64(App.gson().toJson(extend), Util.URL_SAFE));
                params.put("ac", site.getType() == 0 ? "videolist" : "detail");
                params.put("t", tid);
                params.put("pg", page);
                // 复用采集请求并保持非首页请求行为。
                String categoryContent = call(site, params);
                SpiderDebug.log("category", categoryContent);
                return Result.fromType(site.getType(), categoryContent);
            }
        });
    }

    public void detailContent(String key, String id) {
        // 捕获本次任务避免新请求替换等待中的结果。
        execute(result, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("detail", "key=%s,id=%s", key, id);
            if (site.isEmpty() && "push_agent".equals(key)) {
                Vod vod = new Vod();
                vod.setId(id);
                vod.setName(id);
                vod.setPlayUrl(id);
                vod.setPlayFrom(ResUtil.getString(R.string.push));
                vod.setPic(ResUtil.getString(R.string.push_image));
                Source.get().parse(vod.setFlags());
                return Result.vod(vod);
            } else if (site.getType() == 3) {
                Spider spider = site.recent().spider();
                String detailContent = spider.detailContent(Arrays.asList(id));
                SpiderDebug.log("detail", detailContent);
                Result result = Result.fromJson(detailContent);
                Source.get().parse(result.getVod().setFlags());
                return result;
            } else {
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("ac", site.getType() == 0 ? "videolist" : "detail");
                params.put("ids", id);
                // 复用采集请求并保持非首页请求行为。
                String detailContent = call(site, params);
                SpiderDebug.log("detail", detailContent);
                Result result = Result.fromType(site.getType(), detailContent);
                Source.get().parse(result.getVod().setFlags());
                return result;
            }
        });
    }

    public void playerContent(String key, String flag, String id) {
        // 捕获本次任务避免新请求替换等待中的结果。
        execute(player, () -> {
            Source.get().stop();
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("player", "key=%s,flag=%s,id=%s", key, flag, id);
            if (site.getType() == 3) {
                Spider spider = site.recent().spider();
                String playerContent = spider.playerContent(flag, id, VodConfig.get().getFlags());
                SpiderDebug.log("player", playerContent);
                Result result = Result.fromJson(playerContent);
                if (result.getFlag().isEmpty()) result.setFlag(flag);
                // 解析播放地址并明确拒绝已移除的协议。
                result.setUrl(Source.get().fetch(result));
                result.setHeader(site.getHeader());
                result.setKey(key);
                return result;
            } else if (site.getType() == 4) {
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("play", id);
                params.put("flag", flag);
                // 复用采集请求并保持非首页请求行为。
                String playerContent = call(site, params);
                SpiderDebug.log("player", playerContent);
                Result result = Result.fromJson(playerContent);
                if (result.getFlag().isEmpty()) result.setFlag(flag);
                // 解析播放地址并明确拒绝已移除的协议。
                result.setUrl(Source.get().fetch(result));
                result.setHeader(site.getHeader());
                return result;
            } else if (site.isEmpty() && "push_agent".equals(key)) {
                Result result = new Result();
                result.setUrl(id);
                result.setParse(0);
                result.setFlag(flag);
                // 解析播放地址并明确拒绝已移除的协议。
                result.setUrl(Source.get().fetch(result));
                SpiderDebug.log("player", result.toString());
                return result;
            } else {
                Result result = new Result();
                result.setUrl(id);
                result.setFlag(flag);
                result.setHeader(site.getHeader());
                result.setPlayUrl(site.getPlayUrl());
                result.setParse(Sniffer.isVideoFormat(id) && result.getPlayUrl().isEmpty() ? 0 : 1);
                // 解析播放地址并明确拒绝已移除的协议。
                result.setUrl(Source.get().fetch(result));
                SpiderDebug.log("player", result.toString());
                return result;
            }
        });
    }

    // 启动新一轮搜索并隔离旧请求结果。
    public void searchContent(List<Site> sites, String keyword, boolean quick) {
        // 取消旧搜索并使其迟到结果失效。
        stopSearch();
        int generation = searchGeneration.get();
        // 后台搜索并仅发布本轮有效结果。
        sites.forEach(site -> searchFuture.add(App.submitSearch(SearchTask.create(this, site, keyword, quick).run(generation))));
    }

    // 仅在当前搜索轮次发布异步返回结果。
    public void publishSearch(int generation, Result result) {
        App.post(() -> {
            if (generation == searchGeneration.get()) search.setValue(result);
        });
    }

    public void searchContent(Site site, String keyword, boolean quick, String page) {
        // 捕获本次任务避免新请求替换等待中的结果。
        execute(result, SearchTask.create(this, site, keyword, quick, page));
    }

    public void action(String key, String action) {
        // 捕获本次任务避免新请求替换等待中的结果。
        execute(this.action, () -> {
            Site site = VodConfig.get().getSite(key);
            SpiderDebug.log("action", "key=%s,action=%s", key, action);
            if (site.getType() == 3) return Result.fromJson(site.recent().spider().action(action));
            if (site.getType() == 4) return Result.fromJson(OkHttp.string(action));
            return Result.empty();
        });
    }

    // 复用采集请求并保持非首页请求行为。
    public String call(Site site, ArrayMap<String, String> params) throws IOException {
        // 复用采集请求并支持首页连接取消。
        return call(site, params, null);
    }

    // 复用采集请求并支持首页连接取消。
    private String call(Site site, ArrayMap<String, String> params, HomeRequest connection) throws IOException {
        if (!site.getExt().isEmpty()) params.put("extend", site.getExt());
        Call get = OkHttp.newCall(site.getApi(), site.getHeader(), params);
        Call post = OkHttp.newCall(site.getApi(), site.getHeader(), OkHttp.toBody(params));
        Call selected = site.getExt().length() <= 1000 ? get : post;
        // 登记连接使超时和换源能立即取消网络请求。
        try (Response response = connection == null ? selected.execute() : connection.execute(selected)) {
            return response.body().string();
        }
    }

    // 为搜索等请求按需补全影片详情。
    public Result fetchPic(Site site, Result result) throws Exception {
        // 按需补全详情并纳入首页连接取消范围。
        return fetchPic(site, result, null);
    }

    // 按需补全详情并纳入首页连接取消范围。
    private Result fetchPic(Site site, Result result, HomeRequest connection) throws Exception {
        if (site.getType() > 2 || result.getList().isEmpty() || !result.getVod().getPic().isEmpty()) return result;
        ArrayList<String> ids = new ArrayList<>();
        boolean empty = site.getCategories().isEmpty();
        for (Vod item : result.getList()) if (empty || site.getCategories().contains(item.getTypeName())) ids.add(item.getId());
        if (ids.isEmpty()) return result.clear();
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("ac", site.getType() == 0 ? "videolist" : "detail");
        params.put("ids", TextUtils.join(",", ids));
        Call call = OkHttp.newCall(site.getApi(), site.getHeader(), params);
        // 登记连接使超时和换源能立即取消网络请求。
        try (Response response = connection == null ? call.execute() : connection.execute(call)) {
            result.setList(Result.fromType(site.getType(), response.body().string()).getList());
            return result;
        }
    }

    // 按来源配置整理首页分类和筛选项。
    private void setTypes(Site site, Result result) {
        result.getTypes().stream().filter(type -> result.getFilters().containsKey(type.getTypeId())).forEach(type -> type.setFilters(result.getFilters().get(type.getTypeId())));
        List<Class> types = site.getCategories().stream().flatMap(cate -> result.getTypes().stream().filter(type -> cate.equals(type.getTypeName()))).toList();
        if (!types.isEmpty()) result.setTypes(types);
    }

    // 捕获本次任务避免新请求替换等待中的结果。
    private void execute(MutableLiveData<Result> result, Callable<Result> callable) {
        int currentId = taskId.incrementAndGet();
        if (future != null && !future.isDone()) future.cancel(true);
        if (executor.isShutdown()) return;
        Future<Result> newFuture = App.submit(callable);
        future = newFuture;
        executor.execute(() -> {
            try {
                Result taskResult = newFuture.get(Constant.TIMEOUT_VOD, TimeUnit.MILLISECONDS);
                if (taskId.get() != currentId) return;
                result.postValue(taskResult);
            } catch (CancellationException ignored) {
            } catch (Throwable e) {
                if (taskId.get() != currentId) return;
                if (e.getCause() instanceof ExtractException) result.postValue(Result.error(e.getCause().getMessage()));
                else result.postValue(Result.empty());
                // 将异常堆栈同时交给本机诊断适配器。
                SpiderDebug.log(e);
            }
        });
    }

    // 取消旧搜索并使其迟到结果失效。
    public void stopSearch() {
        searchGeneration.incrementAndGet();
        searchFuture.forEach(future -> future.cancel(true));
        searchFuture.clear();
    }

    // 销毁页面时取消首页轮次并终止请求。
    @Override
    protected void onCleared() {
        super.onCleared();
        taskId.incrementAndGet();
        if (future != null) future.cancel(true);
        if (executor != null) executor.shutdownNow();
    }
}
