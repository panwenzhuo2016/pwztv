package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.List;
import java.util.Map;

public class PyLoader {

    // 创建Python片源缓存且暂不启动运行时。
    public PyLoader() {
        // 默认构建没有Python运行时和片源实例。
    }

    // 清理已创建的Python片源实例。
    public void clear() {
        // 默认构建没有Python片源实例可清理。
    }

    // 记录最近使用的Python片源。
    public void setRecent(String recent) {
        // 默认构建不持有Python代理状态。
    }

    // 返回当前版本不支持此片源的提示。
    private String unavailable() {
        return App.gson().toJson(Result.error(ResUtil.getString(R.string.error_python_disabled)));
    }

    // 获取Python片源或返回不可用结果。
    public Spider getSpider(String key, String api, String ext) {
        Spider spider = new SpiderNull() {
            // 返回当前版本不支持此片源的首页结果。
            @Override public String homeContent(boolean filter) {
                // 返回当前版本不支持此片源的提示。
                return unavailable();
            }

            // 返回当前版本不支持此片源的播放结果。
            @Override public String playerContent(String flag, String id, List<String> vipFlags) {
                // 返回当前版本不支持此片源的提示。
                return unavailable();
            }
        };
        spider.siteKey = key;
        return spider;
    }

    // 处理最近使用的Python片源代理请求。
    public Object[] proxy(Map<String, String> params) {
        return null;
    }
}
