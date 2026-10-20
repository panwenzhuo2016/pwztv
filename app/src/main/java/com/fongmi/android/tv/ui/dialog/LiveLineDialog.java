package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.source.LiveLineStore;
import com.fongmi.android.tv.bean.Channel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class LiveLineDialog {

    // 展示来源线路并支持标记与恢复错误内容。
    public static void show(Activity activity, Channel channel, IntConsumer selected) {
        List<String> routes = new ArrayList<>(channel.getUrls());
        if (routes.isEmpty()) return;
        int current = channel.getLine();
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        String[] labels = new String[routes.size()];
        for (int i = 0; i < labels.length; i++) {
            String[] parts = routes.get(i).split("\\$", 2);
            String name = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : activity.getString(R.string.live_line, i + 1);
            labels[i] = activity.getString(R.string.live_line_source, name, i + 1, routes.size());
            // 查询地址是否被用户标记为内容不符。
            if (store.isBlocked(routes.get(i))) labels[i] += activity.getString(R.string.live_line_rejected);
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.live_line_title, channel.getName()))
                .setSingleChoiceItems(labels, current, (view, index) -> {
                    // 保存或解除内容不符标记并立即持久化。
                    store.setBlocked(routes.get(index), false);
                    view.dismiss();
                    selected.accept(index);
                })
                .setNeutralButton(R.string.live_line_reject, (view, which) -> {
                    // 保存或解除内容不符标记并立即持久化。
                    store.setBlocked(routes.get(current), true);
                    // 有限选择未被标记且符合条件的备用线路。
                    selected.accept(store.next(routes, current, url -> true));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        dialog.getListView().setSelection(current);
    }
}
