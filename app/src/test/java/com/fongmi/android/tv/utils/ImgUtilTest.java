package com.fongmi.android.tv.utils;

import android.text.TextUtils;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.github.catvod.crawler.SpiderDebug;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.IOException;

import jahirfiquitiva.libs.textdrawable.TextDrawable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImgUtilTest {

    private MockedStatic<Glide> glide;
    private MockedStatic<TextUtils> text;
    private MockedStatic<UrlUtil> urls;
    private MockedStatic<ResUtil> resources;
    private MockedStatic<SpiderDebug> logs;
    private MockedConstruction<TextDrawable.Builder> drawables;
    private ImageView view;
    private RequestManager manager;
    private RequestBuilder<Drawable> primary;
    private RequestBuilder<Drawable> retry;
    private TextDrawable fallback;
    private final String url = "https://posters.example/cover.jpg";

    // 隔离安卓视图，执行真实图片绑定与失败监听代码。
    @Before public void setUp() {
        view = mock(ImageView.class);
        manager = mock(RequestManager.class);
        primary = mock(RequestBuilder.class, RETURNS_SELF);
        retry = mock(RequestBuilder.class, RETURNS_SELF);
        fallback = mock(TextDrawable.class);
        glide = mockStatic(Glide.class);
        text = mockStatic(TextUtils.class);
        urls = mockStatic(UrlUtil.class);
        resources = mockStatic(ResUtil.class);
        logs = mockStatic(SpiderDebug.class);
        drawables = mockConstruction(TextDrawable.Builder.class, (builder, context) -> {
            when(builder.buildRect(anyString(), anyInt())).thenReturn(fallback);
            when(builder.buildRoundRect(anyString(), anyInt(), anyInt())).thenReturn(fallback);
        });
        glide.when(() -> Glide.with(view)).thenReturn(manager);
        text.when(() -> TextUtils.isEmpty(any())).thenAnswer(call -> {
            CharSequence value = call.getArgument(0);
            return value == null || value.length() == 0;
        });
        urls.when(() -> UrlUtil.convert(url)).thenReturn(url);
        urls.when(() -> UrlUtil.host(url)).thenReturn("posters.example");
        doReturn(primary).when(manager).load(any(Object.class));
        doReturn(retry).when(primary).clone();
    }

    // 释放静态模拟，避免污染其他回归用例。
    @After public void tearDown() {
        drawables.close();
        logs.close();
        resources.close();
        urls.close();
        text.close();
        glide.close();
    }

    // 失败后重新绑定相同地址仍应发起加载。
    @Test public void failedPosterIsNotPermanentlyBlacklisted() {
        // 加载封面并允许失败后重新请求。
        ImgUtil.load("医学大联盟", url, view);
        ArgumentCaptor<RequestListener> listener = ArgumentCaptor.forClass(RequestListener.class);
        verify(primary).listener(listener.capture());
        assertFalse(listener.getValue().onLoadFailed(new GlideException("temporary", new IOException("offline")), null, null, true));
        // 加载封面并允许失败后重新请求。
        ImgUtil.load("医学大联盟", url, view);
        verify(manager, times(2)).load(any(Object.class));
        verify(primary, times(2)).into(view);
    }

    // 每次绑定仅建立一次重试，最终交由占位图处理。
    @Test public void retriesOnceWithoutRecursiveRequests() {
        // 加载封面并允许失败后重新请求。
        ImgUtil.load("封面", url, view);
        verify(primary).error(retry);
        verify(retry, never()).error(any(RequestBuilder.class));
        ArgumentCaptor<RequestListener> listener = ArgumentCaptor.forClass(RequestListener.class);
        verify(retry).listener(listener.capture());
        assertFalse(listener.getValue().onLoadFailed(new GlideException("unavailable"), null, null, false));
        verify(manager, times(1)).load(any(Object.class));
        verify(view, never()).setImageDrawable(any());
    }

    // 无图条目必须取消旧请求再显示文字占位。
    @Test public void emptyPosterClearsRecycledViewRequest() {
        // 加载封面并允许失败后重新请求。
        ImgUtil.load("无封面", "", view);
        verify(manager).clear(view);
        verify(view).setImageDrawable(fallback);
        verify(manager, never()).load(any(Object.class));
    }
}
