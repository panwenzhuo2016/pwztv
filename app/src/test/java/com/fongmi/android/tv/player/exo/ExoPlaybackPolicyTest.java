package com.fongmi.android.tv.player.exo;

import android.os.Process;

import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Setting;
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
            // 点播增加恢复缓冲且按设备限制缓存内存。
            assertSame(control, ExoUtil.buildLoadControl(true));
            DefaultLoadControl.Builder applied = builders.constructed().get(0);
            verify(applied).setBufferDurationsMs(30_000, 60_000, 2500, 8000);
            verify(applied).setTargetBufferBytes(12 * 1024 * 1024);
            verify(applied).setPrioritizeTimeOverSizeThresholds(false);
        }
    }

    // 反查点播启用自适应码率且直播保持原有配置。
    @Test public void allowsAdaptiveVideoSelectionWithoutChangingLivePolicy() {
        for (boolean vod : new boolean[]{true, false}) {
            DefaultTrackSelector.Parameters.Builder parameters = mock(DefaultTrackSelector.Parameters.Builder.class, RETURNS_SELF);
            DefaultTrackSelector.Parameters selected = mock(DefaultTrackSelector.Parameters.class);
            when(parameters.build()).thenReturn(selected);
            try (MockedStatic<App> app = mockStatic(App.class);
                 MockedStatic<Setting> setting = mockStatic(Setting.class);
                 MockedConstruction<DefaultTrackSelector> selectors = mockConstruction(DefaultTrackSelector.class,
                         (selector, context) -> when(selector.buildUponParameters()).thenReturn(parameters))) {
                // 点播允许自适应码率并保留用户手选轨道。
                DefaultTrackSelector selector = (DefaultTrackSelector) ExoUtil.buildTrackSelector(vod);
                verify(parameters).setForceHighestSupportedBitrate(!vod);
                verify(selector).setParameters(selected);
            }
        }
    }
}
