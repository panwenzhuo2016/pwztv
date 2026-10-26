package com.fongmi.android.tv.player;

import android.util.Log;

import androidx.media3.common.PlaybackException;

import com.fongmi.android.tv.event.ErrorEvent;
import com.fongmi.android.tv.player.exo.ErrorMsgProvider;
import com.orhanobut.logger.Logger;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class PlayerFailureTest {
    // 验证日志兼容错误仍可将播放失败交给换源流程。
    @Test public void loggingFailureDoesNotStopPlaybackErrorDelivery() throws Exception {
        Players player = mock(Players.class, CALLS_REAL_METHODS);
        ErrorMsgProvider provider = mock(ErrorMsgProvider.class);
        Field messages = Players.class.getDeclaredField("provider");
        messages.setAccessible(true);
        messages.set(player, provider);
        try (MockedStatic<Logger> logger = mockStatic(Logger.class);
             MockedStatic<ErrorEvent> events = mockStatic(ErrorEvent.class);
             MockedStatic<Log> logs = mockStatic(Log.class);
             MockedStatic<android.os.SystemClock> clocks = mockStatic(android.os.SystemClock.class)) {
            PlaybackException error = new PlaybackException("HTTP 404", null, PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS);
            when(provider.get(error)).thenReturn("HTTP 404");
            logger.when(() -> Logger.t(anyString())).thenThrow(new NoSuchMethodError("logger API"));
            // 恢复播放或传递屏幕提示及其原始异常。
            player.onPlayerError(error);
            events.verify(() -> ErrorEvent.extract(null, "HTTP 404", error));
        }
    }
}
