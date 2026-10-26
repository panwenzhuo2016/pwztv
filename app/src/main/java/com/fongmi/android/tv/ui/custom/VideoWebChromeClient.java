package com.fongmi.android.tv.ui.custom;

import android.graphics.Bitmap;
import android.webkit.WebChromeClient;

import androidx.annotation.NonNull;

final class VideoWebChromeClient extends WebChromeClient {
    private Bitmap poster;

    // 为网页视频提供可复用的非空封面。
    VideoWebChromeClient() {
    }

    // 避免旧WebView回退封面为空导致崩溃。
    @NonNull
    @Override
    public Bitmap getDefaultVideoPoster() {
        if (poster == null || poster.isRecycled()) {
            poster = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        return poster;
    }
}
