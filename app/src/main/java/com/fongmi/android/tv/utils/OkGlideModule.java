package com.fongmi.android.tv.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.App;
import com.github.catvod.net.OkHttp;

import java.io.InputStream;

@GlideModule
public class OkGlideModule extends AppGlideModule {

    // 在默认适配基础上限制低内存设备图片缓存。
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setLogLevel(BuildConfig.DEBUG ? Log.DEBUG : Log.ERROR);
        // 获取当前设备的搜索和图片资源预算。
        ResourceBudget budget = App.resourceBudget();
        if (!budget.constrained) return;
        MemorySizeCalculator defaults = new MemorySizeCalculator.Builder(context).build();
        builder.setMemoryCache(new LruResourceCache(Math.min(defaults.getMemoryCacheSize(), budget.imageCacheBytes)));
        builder.setBitmapPool(new LruBitmapPool(Math.min(defaults.getBitmapPoolSize(), budget.bitmapPoolBytes)));
        builder.setArrayPool(new LruArrayPool(Math.min(defaults.getArrayPoolSizeInBytes(), budget.arrayPoolBytes)));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(OkHttp.client()));
    }
}
