package com.fongmi.android.tv.ui.custom;

import android.graphics.Bitmap;

import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class VideoWebChromeClientTest {
    // 验证视频封面非空可复用且回收后自动重建。
    @Test public void suppliesReusablePosterAndRecreatesRecycledBitmap() {
        VideoWebChromeClient client = mock(VideoWebChromeClient.class, CALLS_REAL_METHODS);
        Bitmap first = mock(Bitmap.class);
        Bitmap replacement = mock(Bitmap.class);
        when(first.getWidth()).thenReturn(1);
        when(first.getHeight()).thenReturn(1);
        when(first.isRecycled()).thenReturn(false, true);
        try (MockedStatic<Bitmap> bitmaps = mockStatic(Bitmap.class)) {
            bitmaps.when(() -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)).thenReturn(first, replacement);
            // 避免旧WebView回退封面为空导致崩溃。
            Bitmap poster = client.getDefaultVideoPoster();
            assertNotNull(poster);
            assertEquals(1, poster.getWidth());
            assertEquals(1, poster.getHeight());
            // 避免旧WebView回退封面为空导致崩溃。
            assertSame(poster, client.getDefaultVideoPoster());
            // 避免旧WebView回退封面为空导致崩溃。
            assertSame(replacement, client.getDefaultVideoPoster());
            bitmaps.verify(() -> Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), times(2));
        }
    }
}
