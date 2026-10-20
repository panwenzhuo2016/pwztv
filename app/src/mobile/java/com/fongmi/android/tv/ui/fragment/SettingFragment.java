package com.fongmi.android.tv.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.FragmentSettingBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.ConfigDialog;
import com.fongmi.android.tv.ui.dialog.HistoryDialog;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.DiagnosticLog;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingFragment extends BaseFragment implements ConfigCallback {

    private static final int WALL_TYPE = 2;
    private FragmentSettingBinding mBinding;
    private String[] size;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    private String getSwitch(boolean value) {
        return getString(value ? R.string.setting_on : R.string.setting_off);
    }

    private HomeActivity getRoot() {
        return (HomeActivity) requireActivity();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingBinding.inflate(inflater, container, false);
    }

    // 初始化精简设置与壁纸状态。
    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        mBinding.wallUrl.setText(WallConfig.getDesc());
        // 显示无痕模式与图片尺寸。
        setOtherText();
        setCacheText();
    }

    // 显示无痕模式与图片尺寸。
    private void setOtherText() {
        mBinding.incognitoText.setText(getSwitch(Setting.isIncognito()));
        mBinding.sizeText.setText((size = ResUtil.getStringArray(R.array.select_size))[Setting.getSize()]);
    }

    private void setCacheText() {
        FileUtil.getCacheSize(new Callback() {
            @Override
            public void success(String result) {
                mBinding.cacheText.setText(result);
            }
        });
    }

    // 绑定保留的设置入口。
    @Override
    protected void initEvent() {
        // 打开壁纸地址设置。
        mBinding.wall.setOnClickListener(this::onWall);
        mBinding.size.setOnClickListener(this::setSize);
        mBinding.cache.setOnClickListener(this::onCache);
        // 后台生成最近一小时日志并复制到剪贴板。
        mBinding.copyError.setOnClickListener(view -> DiagnosticLog.copyRecent());
        mBinding.player.setOnClickListener(this::onPlayer);
        // 编辑当前壁纸地址。
        mBinding.wall.setOnLongClickListener(this::onWallEdit);
        mBinding.incognito.setOnClickListener(this::setIncognito);
        mBinding.wallDefault.setOnClickListener(this::setWallDefault);
        // 重新加载当前壁纸。
        mBinding.wallRefresh.setOnClickListener(this::setWallRefresh);
        // 显示壁纸配置历史。
        mBinding.wallRefresh.setOnLongClickListener(this::onWallHistory);
    }

    // 按文件权限要求加载所选壁纸。
    @Override
    public void setConfig(Config config) {
        if (config.getUrl().startsWith("file")) {
            // 加载壁纸配置并恢复自定义背景。
            PermissionUtil.requestFile(this, allGranted -> load(config));
        } else {
            // 加载壁纸配置并恢复自定义背景。
            load(config);
        }
    }

    // 加载壁纸配置并恢复自定义背景。
    private void load(Config config) {
        Setting.putWall(0);
        // 生成壁纸加载完成后的界面回调。
        WallConfig.load(config, getCallback());
    }

    // 生成壁纸加载完成后的界面回调。
    private Callback getCallback() {
        return new Callback() {
            @Override
            public void start() {
                Notify.progress(requireActivity());
            }

            @Override
            public void success(String result) {
                Notify.show(result);
            }

            // 刷新壁纸地址并结束加载提示。
            @Override
            public void success() {
                // 刷新壁纸地址并结束加载提示。
                onWallLoaded();
            }

            // 提示壁纸加载失败并结束等待。
            @Override
            public void error(String msg) {
                Notify.show(msg);
                // 刷新壁纸地址并结束加载提示。
                onWallLoaded();
            }
        };
    }

    // 刷新壁纸地址并结束加载提示。
    private void onWallLoaded() {
        setCacheText();
        Notify.dismiss();
        RefreshEvent.config();
    }

    // 打开壁纸地址设置。
    private void onWall(View view) {
        ConfigDialog.create(this).launcher(launcher).type(WALL_TYPE).show();
    }

    // 编辑当前壁纸地址。
    private boolean onWallEdit(View view) {
        ConfigDialog.create(this).launcher(launcher).type(WALL_TYPE).edit().show();
        return true;
    }

    private void onPlayer(View view) {
        getRoot().change(2);
    }

    private void setWallDefault(View view) {
        Setting.putWall(Setting.getWall() == 4 ? 1 : Setting.getWall() + 1);
        RefreshEvent.wall();
    }

    // 重新加载当前壁纸。
    private void setWallRefresh(View view) {
        Setting.putWall(0);
        // 生成壁纸加载完成后的界面回调。
        WallConfig.get().load(getCallback());
    }

    // 显示壁纸配置历史。
    private boolean onWallHistory(View view) {
        HistoryDialog.create(this).type(WALL_TYPE).show();
        return true;
    }

    private void setIncognito(View view) {
        Setting.putIncognito(!Setting.isIncognito());
        mBinding.incognitoText.setText(getSwitch(Setting.isIncognito()));
    }

    private void setSize(View view) {
        new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.setting_size).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(size, Setting.getSize(), (dialog, which) -> {
            mBinding.sizeText.setText(size[which]);
            Setting.putSize(which);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void onCache(View view) {
        FileUtil.clearCache(new Callback() {
            @Override
            public void success() {
                setCacheText();
            }
        });
    }

    // 同步刷新后的壁纸地址。
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() != RefreshEvent.Type.CONFIG) return;
        mBinding.wallUrl.setText(WallConfig.getDesc());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) return;
        setCacheText();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        // 按文件权限要求加载所选壁纸。
        setConfig(Config.find("file:/" + FileChooser.getPathFromUri(result.getData().getData()).replace(Path.rootPath(), ""), WALL_TYPE));
    });
}
