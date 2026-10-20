package com.fongmi.android.tv.model;

import com.fongmi.android.tv.api.source.HomeLoader;
import com.fongmi.android.tv.bean.Result;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

final class HomeRequest implements HomeLoader.Request<Result> {
    private final Loader loader;
    private boolean canceled;
    private Call active;

    interface Loader {
        // 使用可取消连接读取首页全部内容。
        Result load(HomeRequest request) throws Exception;
    }

    // 绑定单个首页来源的内容加载任务。
    HomeRequest(Loader loader) {
        this.loader = loader;
    }

    // 使用可取消连接读取首页全部内容。
    @Override public Result call() throws Exception {
        // 使用可取消连接读取首页全部内容。
        return loader.load(this);
    }

    // 登记连接使超时和换源能立即取消网络请求。
    Response execute(Call call) throws IOException {
        synchronized (this) {
            if (canceled) {
                call.cancel();
                throw new IOException("Home request canceled");
            }
            active = call;
        }
        return call.execute();
    }

    // 取消当前来源的网络请求。
    @Override public synchronized void cancel() {
        canceled = true;
        if (active != null) active.cancel();
    }
}
