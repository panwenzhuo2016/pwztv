package com.fongmi.android.tv.player.exo;

import android.os.Process;

import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.player.PlaybackMode;
import com.fongmi.android.tv.utils.ResourceBudget;

import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExoPlaybackPolicyTest {
    // 反查三十二位设备实际加载小缓存与八秒恢复阈值。
    @Test public void appliesConstrainedBufferTo32BitDevices() {
        DefaultLoadControl control = mock(DefaultLoadControl.class);
        try (MockedStatic<App> app = mockStatic(App.class);
             MockedStatic<Setting> setting = mockStatic(Setting.class);
             MockedStatic<Process> process = mockStatic(Process.class);
             MockedConstruction<DefaultLoadControl.Builder> builders = mockConstruction(DefaultLoadControl.Builder.class,
                     withSettings().defaultAnswer(RETURNS_SELF), (builder, context) -> when(builder.build()).thenReturn(control))) {
            // 兼顾系统低内存标记和应用堆额度。
            app.when(App::resourceBudget).thenReturn(ResourceBudget.forDevice(false, 256));
            setting.when(Setting::getBuffer).thenReturn(1);
            process.when(Process::is64Bit).thenReturn(false);
            // 按直播或点播场景配置缓冲及设备缓存额度。
            assertSame(control, ExoUtil.buildLoadControl(PlaybackMode.VOD));
            DefaultLoadControl.Builder applied = builders.constructed().get(0);
            verify(applied).setBufferDurationsMs(30_000, 60_000, 2500, 8000);
            verify(applied).setTargetBufferBytes(12 * 1024 * 1024);
            verify(applied).setPrioritizeTimeOverSizeThresholds(false);
        }
    }

    // 反查直播点播启用自适应且投屏保留原码率策略。
    @Test public void allowsAdaptiveVideoSelectionForLiveAndVod() {
        for (PlaybackMode mode : PlaybackMode.values()) {
            DefaultTrackSelector.Parameters.Builder parameters = mock(DefaultTrackSelector.Parameters.Builder.class, RETURNS_SELF);
            DefaultTrackSelector.Parameters selected = mock(DefaultTrackSelector.Parameters.class);
            when(parameters.build()).thenReturn(selected);
            try (MockedStatic<App> app = mockStatic(App.class);
                 MockedStatic<Setting> setting = mockStatic(Setting.class);
                 MockedConstruction<DefaultTrackSelector> selectors = mockConstruction(DefaultTrackSelector.class,
                         (selector, context) -> when(selector.buildUponParameters()).thenReturn(parameters))) {
                // 允许自适应码率并保留用户手选轨道。
                DefaultTrackSelector selector = (DefaultTrackSelector) ExoUtil.buildTrackSelector(mode != PlaybackMode.DEFAULT);
                verify(parameters).setForceHighestSupportedBitrate(mode == PlaybackMode.DEFAULT);
                verify(selector).setParameters(selected);
            }
        }
    }

    // 反查各架构直播实际加载三秒恢复与对应缓存额度。
    @Test public void appliesLivePolicyForBothArchitecturesAndMemoryBudgets() {
        for (boolean is64Bit : new boolean[]{false, true}) {
            for (boolean lowRam : new boolean[]{false, true}) {
                DefaultLoadControl control = mock(DefaultLoadControl.class);
                try (MockedStatic<App> app = mockStatic(App.class);
                     MockedStatic<Setting> setting = mockStatic(Setting.class);
                     MockedStatic<Process> process = mockStatic(Process.class);
                     MockedConstruction<DefaultLoadControl.Builder> builders = mockConstruction(DefaultLoadControl.Builder.class,
                             withSettings().defaultAnswer(RETURNS_SELF), (builder, context) -> when(builder.build()).thenReturn(control))) {
                    // 兼顾系统低内存标记和应用堆额度。
                    app.when(App::resourceBudget).thenReturn(ResourceBudget.forDevice(lowRam, 256));
                    setting.when(Setting::getBuffer).thenReturn(1);
                    process.when(Process::is64Bit).thenReturn(is64Bit);
                    // 按直播或点播场景配置缓冲及设备缓存额度。
                    assertSame(control, ExoUtil.buildLoadControl(PlaybackMode.LIVE));
                    DefaultLoadControl.Builder applied = builders.constructed().get(0);
                    verify(applied).setBufferDurationsMs(10_000, 20_000, 1500, 3000);
                    verify(applied).setTargetBufferBytes((lowRam || !is64Bit ? 8 : 16) * 1024 * 1024);
                    verify(applied).setPrioritizeTimeOverSizeThresholds(false);
                }
            }
        }
    }

    // 反查未配置直播点播的投屏仍使用原缓冲策略。
    @Test public void leavesDefaultPlaybackBufferUnchanged() {
        try (MockedStatic<Setting> setting = mockStatic(Setting.class);
             MockedConstruction<DefaultLoadControl.Builder> builders = mockConstruction(DefaultLoadControl.Builder.class,
                     withSettings().defaultAnswer(RETURNS_SELF))) {
            setting.when(Setting::getBuffer).thenReturn(2);
            // 按直播或点播场景配置缓冲及设备缓存额度。
            ExoUtil.buildLoadControl(PlaybackMode.DEFAULT);
            DefaultLoadControl.Builder applied = builders.constructed().get(0);
            verify(applied).setBufferDurationsMs(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 2,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 2, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
            verify(applied, never()).setTargetBufferBytes(anyInt());
        }
    }
}
