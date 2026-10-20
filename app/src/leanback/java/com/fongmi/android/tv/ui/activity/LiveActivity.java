package com.fongmi.android.tv.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.source.SourceHealth;
import com.fongmi.android.tv.api.source.LiveLineStore;
import com.fongmi.android.tv.ui.dialog.LiveLineDialog;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Epg;
import com.fongmi.android.tv.bean.EpgData;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Track;
import com.fongmi.android.tv.databinding.ActivityLiveBinding;
import com.fongmi.android.tv.event.ActionEvent;
import com.fongmi.android.tv.event.ErrorEvent;
import com.fongmi.android.tv.event.PlayerEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.impl.CustomTarget;
import com.fongmi.android.tv.impl.LiveCallback;
import com.fongmi.android.tv.impl.PassCallback;
import com.fongmi.android.tv.model.LiveViewModel;
import com.fongmi.android.tv.player.Players;
import com.fongmi.android.tv.player.Source;
import com.fongmi.android.tv.player.exo.ExoUtil;
import com.fongmi.android.tv.service.PlaybackService;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.CustomKeyDownLive;
import com.fongmi.android.tv.ui.custom.CustomLiveListView;
import com.fongmi.android.tv.ui.dialog.HistoryDialog;
import com.fongmi.android.tv.ui.dialog.LiveDialog;
import com.fongmi.android.tv.ui.dialog.PassDialog;
import com.fongmi.android.tv.ui.dialog.SubtitleDialog;
import com.fongmi.android.tv.ui.dialog.TrackDialog;
import com.fongmi.android.tv.ui.presenter.ChannelPresenter;
import com.fongmi.android.tv.ui.presenter.EpgDataPresenter;
import com.fongmi.android.tv.ui.presenter.GroupPresenter;
import com.fongmi.android.tv.utils.Clock;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.DiagnosticLog;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Traffic;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class LiveActivity extends BaseActivity implements GroupPresenter.OnClickListener, ChannelPresenter.OnClickListener, EpgDataPresenter.OnClickListener, CustomKeyDownLive.Listener, CustomLiveListView.Callback, TrackDialog.Listener, PassCallback, ConfigCallback, LiveCallback {

    private ActivityLiveBinding mBinding;
    private ArrayObjectAdapter mChannelAdapter;
    private ArrayObjectAdapter mEpgDataAdapter;
    private ArrayObjectAdapter mGroupAdapter;
    private Observer<Result> mObserveUrl;
    private CustomKeyDownLive mKeyDown;
    private Observer<Epg> mObserveEpg;
    private LiveViewModel mViewModel;
    private List<Group> mHides;
    private Players mPlayers;
    private Channel mChannel;
    private final java.util.Set<String> mFailedLines = new java.util.HashSet<>();
    private View mOldView;
    private Group mGroup;
    private Runnable mR0;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    private Runnable mR4;
    private Clock mClock;
    private View mFocus2;
    private boolean redirect;
    private String tag;
    private int count;

    public static void start(Context context) {
        context.startActivity(new Intent(context, LiveActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("empty", LiveConfig.isEmpty()));
    }

    private boolean isEmpty() {
        return getIntent().getBooleanExtra("empty", true);
    }

    private Group getKeep() {
        return (Group) mGroupAdapter.get(0);
    }

    private Live getHome() {
        return LiveConfig.get().getHome();
    }

    private long getTimeout() {
        return getHome().isEmpty() ? Constant.TIMEOUT_PLAY : getHome().getTimeout();
    }

    @Override
    protected boolean customWall() {
        return false;
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityLiveBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mClock = Clock.create(mBinding.widget.clock);
        mKeyDown = CustomKeyDownLive.create(this);
        mPlayers = Players.create(this);
        mObserveEpg = this::setEpg;
        // 阻止已标记地址的迟到结果重新开始播放。
        mObserveUrl = this::start;
        mHides = new ArrayList<>();
        mR0 = this::setActivated;
        mR1 = this::hideControl;
        mR2 = this::setTraffic;
        mR3 = this::hideInfo;
        mR4 = this::hideUI;
        setRecyclerView();
        setVideoView();
        setViewModel();
        checkLive();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
        mBinding.group.setListener(this);
        mBinding.channel.setListener(this);
        mBinding.epgData.setListener(this);
        mBinding.control.seek.setPlayer(mPlayers);
        mBinding.control.text.setOnClickListener(this::onTrack);
        mBinding.control.audio.setOnClickListener(this::onTrack);
        mBinding.control.video.setOnClickListener(this::onTrack);
        mBinding.control.speed.setUpListener(this::onSpeedAdd);
        mBinding.control.speed.setDownListener(this::onSpeedSub);
        mBinding.control.text.setUpListener(this::onSubtitleClick);
        mBinding.control.text.setDownListener(this::onSubtitleClick);
        mBinding.control.home.setOnClickListener(view -> onHome());
        mBinding.control.line.setOnClickListener(view -> {
            // 展示同频道线路并应用手动选择或跳过。
            onLine();
        });
        mBinding.control.scale.setOnClickListener(view -> onScale());
        mBinding.control.speed.setOnClickListener(view -> onSpeed());
        mBinding.control.config.setOnClickListener(view -> onConfig());
        mBinding.control.action.setOnClickListener(view -> onAction());
        mBinding.control.invert.setOnClickListener(view -> onInvert());
        mBinding.control.across.setOnClickListener(view -> onAcross());
        mBinding.control.change.setOnClickListener(view -> onChange());
        mBinding.control.player.setOnClickListener(view -> onChoose());
        mBinding.control.decode.setOnClickListener(view -> onDecode());
        mBinding.control.speed.setOnLongClickListener(view -> onSpeedLong());
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown.onTouchEvent(event));
        mBinding.group.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mGroupAdapter.size() > 0) onChildSelected(child, mGroup = (Group) mGroupAdapter.get(position));
            }
        });
    }

    private void setRecyclerView() {
        mBinding.group.setItemAnimator(null);
        mBinding.channel.setItemAnimator(null);
        mBinding.epgData.setItemAnimator(null);
        mBinding.group.setAdapter(new ItemBridgeAdapter(mGroupAdapter = new ArrayObjectAdapter(new GroupPresenter(this))));
        mBinding.channel.setAdapter(new ItemBridgeAdapter(mChannelAdapter = new ArrayObjectAdapter(new ChannelPresenter(this))));
        mBinding.epgData.setAdapter(new ItemBridgeAdapter(mEpgDataAdapter = new ArrayObjectAdapter(new EpgDataPresenter(this))));
    }

    private void setVideoView() {
        mPlayers.init(mBinding.exo);
        PlaybackService.start(mPlayers);
        setScale(Setting.getLiveScale());
        ExoUtil.setSubtitleView(mBinding.exo);
        mPlayers.setTag(tag = UUID.randomUUID().toString());
        findViewById(R.id.timeBar).setNextFocusUpId(R.id.config);
        mBinding.control.invert.setActivated(Setting.isInvert());
        mBinding.control.across.setActivated(Setting.isAcross());
        mBinding.control.change.setActivated(Setting.isChange());
        mBinding.control.speed.setText(mPlayers.getSpeedText());
        mBinding.control.decode.setText(mPlayers.getDecodeText());
    }

    private void setDecode() {
        mBinding.control.decode.setText(mPlayers.getDecodeText());
    }

    private void setScale(int scale) {
        mBinding.exo.setResizeMode(scale);
        mBinding.control.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        mViewModel.url.observeForever(mObserveUrl);
        mViewModel.xml.observe(this, this::setEpg);
        mViewModel.epg.observeForever(mObserveEpg);
        mViewModel.live.observe(this, live -> {
            mViewModel.getXml(live);
            setGroup(live);
            setWidth(live);
        });
    }

    private void checkLive() {
        if (isEmpty()) {
            LiveConfig.get().init().load(getCallback());
        } else {
            getLive();
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success() {
                getLive();
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
            }
        };
    }

    private void getLive() {
        mBinding.control.home.setText(LiveConfig.isOnly() ? getString(R.string.live_refresh) : getHome().getName());
        mViewModel.getLive(getHome());
        showProgress();
    }

    private void setGroup(Live live) {
        List<Group> items = new ArrayList<>();
        for (Group group : live.getGroups()) (group.isHidden() ? mHides : items).add(group);
        mGroupAdapter.setItems(items, null);
        setPosition(LiveConfig.get().find(items));
    }

    private void setWidth(Live live) {
        int padding = ResUtil.dp2px(52);
        if (live.getWidth() == 0) for (Group item : live.getGroups()) live.setWidth(Math.max(live.getWidth(), ResUtil.getTextWidth(item.getName(), 16)));
        int width = live.getWidth() == 0 ? 0 : Math.min(live.getWidth() + padding, ResUtil.getScreenWidth() / 4);
        setWidth(mBinding.group, width);
    }

    private Group setWidth(Group group) {
        int logo = ResUtil.dp2px(60);
        int padding = ResUtil.dp2px(64);
        if (group.isKeep()) group.setWidth(0);
        if (group.getWidth() == 0) for (Channel item : group.getChannel()) group.setWidth(Math.max(group.getWidth(), (item.getLogo().isEmpty() ? 0 : logo) + ResUtil.getTextWidth(item.getNumber() + item.getName(), 16)));
        int width = group.getWidth() == 0 ? 0 : Math.min(group.getWidth() + padding, ResUtil.getScreenWidth() / 2);
        setWidth(mBinding.channel, width);
        return group;
    }

    private void setWidth(Epg epg) {
        int padding = ResUtil.dp2px(52);
        if (epg.getList().isEmpty()) return;
        int minWidth = ResUtil.getTextWidth(epg.getList().get(0).getTime(), 14);
        if (epg.getWidth() == 0) for (EpgData item : epg.getList()) epg.setWidth(Math.max(epg.getWidth(), ResUtil.getTextWidth(item.getTitle(), 16)));
        int width = epg.getWidth() == 0 ? 0 : Math.min(Math.max(epg.getWidth(), minWidth) + padding, ResUtil.getScreenWidth() / 2);
        setWidth(mBinding.epgData, width);
    }

    private void setWidth(View view, int width) {
        view.post(() -> {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params.width == width) return;
            params.width = width;
            view.setLayoutParams(params);
        });
    }

    private void setPosition(int[] position) {
        if (position[0] == -1) return;
        int size = mGroupAdapter.size();
        if (size == 1 || position[0] >= size) return;
        mGroup = (Group) mGroupAdapter.get(position[0]);
        mBinding.group.setSelectedPosition(position[0]);
        mGroup.setPosition(position[1]);
        onItemClick(mGroup);
        onItemClick(mGroup.current());
    }

    private void setPosition() {
        if (mChannel == null) return;
        mGroup = mChannel.getGroup();
        int position = mGroupAdapter.indexOf(mGroup);
        boolean change = mBinding.group.getSelectedPosition() != position;
        if (change) mBinding.group.setSelectedPosition(position);
        if (change) mChannelAdapter.setItems(mGroup.getChannel(), null);
        mBinding.channel.setSelectedPosition(mGroup.getPosition());
    }

    private void onChildSelected(@Nullable RecyclerView.ViewHolder child, Group group) {
        if (mOldView != null) mOldView.setSelected(false);
        if (child == null) return;
        mOldView = child.itemView;
        mOldView.setSelected(true);
        onItemClick(group);
        resetPass();
    }

    private void setActivated() {
        for (int i = 0; i < mChannelAdapter.size(); i++) ((Channel) mChannelAdapter.get(i)).setSelected(mChannel);
        notifyItemChanged(mBinding.channel, mChannelAdapter);
        // 播放前避开用户标记并处理线路耗尽。
        fetch();
    }

    private void setActivated(EpgData item) {
        for (int i = 0; i < mEpgDataAdapter.size(); i++) ((EpgData) mEpgDataAdapter.get(i)).setSelected(item);
        notifyItemChanged(mBinding.epgData, mEpgDataAdapter);
    }

    private void checkPlay() {
        if (mPlayers.isPlaying()) onPaused();
        else onPlay();
    }

    private void onTrack(View view) {
        TrackDialog.create().player(mPlayers).type(Integer.parseInt(view.getTag().toString())).show(this);
        hideControl();
    }

    private void onHome() {
        if (LiveConfig.isOnly()) setLive(getHome());
        else LiveDialog.create(this).show();
        hideControl();
    }

    // 展示同频道线路并应用手动选择或跳过。
    private void onLine() {
        if (mChannel == null) return;
        Channel channel = mChannel;
        hideControl();
        // 展示来源线路并支持标记与恢复错误内容。
        LiveLineDialog.show(this, channel, index -> {
            if (mChannel != channel) return;
            if (index >= 0) channel.setLine(index);
            mFailedLines.clear();
            // 播放前避开用户标记并处理线路耗尽。
            fetch();
            setInfo();
        });
    }

    private void onScale() {
        int index = Setting.getLiveScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        Setting.putLiveScale(index = index == array.length - 1 ? 0 : ++index);
        setScale(index);
    }

    private void onSpeed() {
        mBinding.control.speed.setText(mPlayers.addSpeed());
    }

    private void onSpeedAdd() {
        mBinding.control.speed.setText(mPlayers.addSpeed(0.25f));
    }

    private void onSpeedSub() {
        mBinding.control.speed.setText(mPlayers.subSpeed(0.25f));
    }

    private boolean onSpeedLong() {
        mBinding.control.speed.setText(mPlayers.toggleSpeed());
        return true;
    }

    private void onConfig() {
        HistoryDialog.create(this).readOnly().type(1).show();
        hideControl();
    }

    private void onAction() {
        checkPlay();
    }

    private void onInvert() {
        Setting.putInvert(!Setting.isInvert());
        mBinding.control.invert.setActivated(Setting.isInvert());
    }

    private void onAcross() {
        Setting.putAcross(!Setting.isAcross());
        mBinding.control.across.setActivated(Setting.isAcross());
    }

    private void onChange() {
        Setting.putChange(!Setting.isChange());
        mBinding.control.change.setActivated(Setting.isChange());
    }

    private void onChoose() {
        mPlayers.choose(this, mBinding.widget.title.getText());
        setRedirect(true);
    }

    private void onDecode() {
        mPlayers.toggleDecode();
        setDecode();
    }

    private void hideUI() {
        App.removeCallbacks(mR4);
        if (isGone(mBinding.recycler)) return;
        mBinding.recycler.setVisibility(View.GONE);
        setPosition();
    }

    private void showUI() {
        if (isVisible(mBinding.recycler) || mGroupAdapter.size() == 0) return;
        mBinding.recycler.setVisibility(View.VISIBLE);
        setPosition();
        setUITimer();
        hideEpg();
    }

    @Override
    public void showEpg(Channel item) {
        if (mChannel == null || mChannel.getData().getList().isEmpty() || mEpgDataAdapter.size() == 0 || !mChannel.equals(item) || !mChannel.getGroup().equals(mGroup)) return;
        mBinding.epgData.setSelectedPosition(mChannel.getData().getSelected());
        mBinding.epgData.setVisibility(View.VISIBLE);
        mBinding.channel.setVisibility(View.GONE);
        mBinding.group.setVisibility(View.GONE);
        mBinding.epgData.requestFocus();
    }

    @Override
    public void hideEpg() {
        mBinding.channel.setVisibility(View.VISIBLE);
        mBinding.group.setVisibility(View.VISIBLE);
        mBinding.epgData.setVisibility(View.GONE);
        mBinding.channel.requestFocus();
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(mR2, 0);
        hideError();
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR2);
        Traffic.reset();
    }

    // 显示播放器异常并记录屏幕错误详情。
    private void showError(String text) {
        // 显示当前错误并记录其原始异常。
        showError(text, null);
    }

    // 显示当前错误并记录其原始异常。
    private void showError(String text, Throwable error) {
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(text);
        // 优先记录事件原始异常并兼容插件文字匹配。
        DiagnosticLog.visible("live-screen", text, true, error);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText("");
    }

    private void showControl(View view) {
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        mBinding.widget.top.setVisibility(View.VISIBLE);
        App.post(view::requestFocus, 25);
        setR1Callback();
        hideInfo();
    }

    private void hideControl() {
        mBinding.control.getRoot().setVisibility(View.GONE);
        mBinding.widget.top.setVisibility(View.GONE);
        App.removeCallbacks(mR1);
    }

    private void hideCenter() {
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play);
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void showInfo() {
        mBinding.widget.bottom.setVisibility(View.VISIBLE);
        setR3Callback();
        setInfo();
    }

    private void hideInfo() {
        mBinding.widget.bottom.setVisibility(View.GONE);
        App.removeCallbacks(mR3);
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.progress.traffic);
        App.post(mR2, 1000);
    }

    private void setR1Callback() {
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR3Callback() {
        App.post(mR3, Constant.INTERVAL_HIDE);
    }

    private void onToggle() {
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else if (isVisible(mBinding.recycler)) hideUI();
        else showUI();
        hideInfo();
    }

    private void resetPass() {
        this.count = 0;
    }

    private void setArtwork() {
        ImgUtil.load(this, mChannel.getLogo(), new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mBinding.exo.setDefaultArtwork(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mBinding.exo.setDefaultArtwork(errorDrawable);
            }
        });
    }

    @Override
    public void onItemClick(Group item) {
        mChannelAdapter.setItems(setWidth(item).getChannel(), null);
        mBinding.channel.setSelectedPosition(Math.max(item.getPosition(), 0));
        if (!item.isKeep() || ++count < 5 || mHides.isEmpty()) return;
        PassDialog.create().show(this);
        App.removeCallbacks(mR4);
        resetPass();
    }

    @Override
    // 选择频道并开始新的线路尝试。
    public void onItemClick(Channel item) {
        if (!item.getData().getList().isEmpty() && item.isSelected() && mChannel != null && mChannel.equals(item) && mChannel.getGroup().equals(mGroup)) {
            showEpg(item);
        } else if (mGroup != null) {
            mGroup.setPosition(mBinding.channel.getSelectedPosition());
            setChannel(item.group(mGroup));
            hideUI();
        }
    }

    @Override
    public boolean onLongClick(Channel item) {
        if (mGroup.isHidden()) return false;
        boolean exist = Keep.exist(item.getName());
        Notify.show(exist ? R.string.keep_del : R.string.keep_add);
        if (exist) delKeep(item);
        else addKeep(item);
        return true;
    }

    @Override
    public void onItemClick(EpgData item) {
        if (item.isSelected()) {
            // 回看播放前避开内容不符的地址。
            fetch(item);
        } else if (mChannel.hasCatchup()) {
            mBinding.widget.title.setText(getString(R.string.detail_title, mChannel.getShow(), item.getTitle()));
            Notify.show(getString(R.string.play_ready, item.getTitle()));
            setActivated(item);
            // 回看播放前避开内容不符的地址。
            fetch(item);
        }
    }

    private void addKeep(Channel item) {
        getKeep().add(item);
        Keep keep = new Keep();
        keep.setKey(item.getName());
        keep.setType(1);
        keep.save();
    }

    private void delKeep(Channel item) {
        if (mGroup.isKeep()) mChannelAdapter.remove(item);
        if (mChannelAdapter.size() == 0) mBinding.group.requestFocus();
        getKeep().getChannel().remove(item);
        Keep.delete(item.getName());
    }

    private void setChannel(Channel item) {
        App.post(mR0, 100);
        mFailedLines.clear();
        mChannel = item;
        setMetadata();
        setArtwork();
        showInfo();
    }

    private void setInfo() {
        mViewModel.getEpg(mChannel);
        mBinding.widget.play.setText("");
        mBinding.widget.name.setMaxEms(48);
        mChannel.loadLogo(mBinding.widget.logo);
        mBinding.widget.title.setSelected(true);
        mBinding.widget.name.setText(mChannel.getShow());
        mBinding.widget.title.setText(mChannel.getShow());
        // 单线路也显示来源名称或线路编号。
        mBinding.widget.line.setText(mChannel.getLineText());
        mBinding.widget.number.setText(mChannel.getNumber());
        // 单线路也显示来源名称或线路编号。
        mBinding.control.line.setText(mChannel.getLineText());
        // 单线路也保留标记与恢复入口。
        mBinding.widget.line.setVisibility(mChannel.getLineVisible());
        // 单线路也保留标记与恢复入口。
        mBinding.control.line.setVisibility(mChannel.getLineVisible());
    }

    private void setEpg() {
        EpgData data = mChannel.getData().getEpgData();
        boolean hasTitle = !data.getTitle().isEmpty();
        mEpgDataAdapter.setItems(mChannel.getData().getList(), null);
        if (hasTitle) mBinding.widget.title.setText(getString(R.string.detail_title, mChannel.getShow(), data.getTitle()));
        mBinding.widget.name.setMaxEms(hasTitle ? 12 : 48);
        mBinding.widget.play.setText(data.format());
        setWidth(mChannel.getData());
        setMetadata();
    }

    private void setEpg(boolean success) {
        if (mChannel != null && success) mViewModel.getEpg(mChannel);
    }

    private void setEpg(Epg epg) {
        if (mChannel != null && mChannel.getTvgId().equals(epg.getKey())) setEpg();
    }

    // 回看播放前避开内容不符的地址。
    private void fetch(EpgData item) {
        if (mChannel == null) return;
        // 确保当前线路未被标记且保留恢复入口。
        if (!prepareLine()) return;
        mViewModel.getUrl(mChannel, item);
        mPlayers.clear();
        mPlayers.stop();
        hideUI();
    }

    // 播放前避开用户标记并处理线路耗尽。
    private void fetch() {
        if (mChannel == null) return;
        // 确保当前线路未被标记且保留恢复入口。
        if (!prepareLine()) return;
        LiveConfig.get().setKeep(mChannel);
        mViewModel.getUrl(mChannel);
        mPlayers.clear();
        mPlayers.stop();
        showProgress();
    }

    // 阻止已标记地址的迟到结果重新开始播放。
    private void start(Result result) {
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        // 查询地址是否被用户标记为内容不符。
        if (mChannel == null || store.isBlocked(mChannel.getCurrent())) return;
        mPlayers.start(result, false, getTimeout());
    }

    // 确保当前线路未被标记且保留恢复入口。
    private boolean prepareLine() {
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        // 查询地址是否被用户标记为内容不符。
        if (!store.isBlocked(mChannel.getCurrent())) return true;
        // 有限选择未被标记且符合条件的备用线路。
        int next = store.next(mChannel.getUrls(), mChannel.getLine(), url -> true);
        if (next >= 0) {
            mChannel.setLine(next);
            setInfo();
            return true;
        }
        // 取消线路解析并使已排队的播放结果失效。
        mViewModel.cancelUrl();
        mPlayers.clear();
        mPlayers.stop();
        // 显示播放器异常并记录屏幕错误详情。
        showError(getString(R.string.live_line_all_rejected));
        return false;
    }

    private void checkPlayImg() {
        mBinding.control.action.setText(mPlayers.isPlaying() ? R.string.pause : R.string.play);
    }

    private void resetAdapter() {
        mBinding.control.line.setVisibility(View.GONE);
        mBinding.widget.title.setText("");
        mEpgDataAdapter.clear();
        mChannelAdapter.clear();
        mGroupAdapter.clear();
        mHides.clear();
        mChannel = null;
        mGroup = null;
    }

    @Override
    public void onSubtitleClick() {
        SubtitleDialog.create().view(mBinding.exo.getSubtitleView()).full(true).show(this);
        App.post(this::hideControl, 100);
    }

    @Override
    public void setConfig(Config config) {
        Config current = LiveConfig.get().getConfig();
        LiveConfig.load(config, getCallback(current));
    }

    private Callback getCallback(Config config) {
        return new Callback() {
            @Override
            public void start() {
                showProgress();
            }

            @Override
            public void success() {
                RefreshEvent.config();
                setLive(getHome());
            }

            @Override
            public void error(String msg) {
                LiveConfig.load(config, new Callback());
                Notify.show(msg);
                hideProgress();
            }
        };
    }

    @Override
    public void setLive(Live item) {
        if (item.isActivated()) item.getGroups().clear();
        LiveConfig.get().setHome(item);
        mPlayers.reset();
        mPlayers.clear();
        mPlayers.stop();
        resetAdapter();
        hideControl();
        getLive();
    }

    @Override
    public void setPass(String pass) {
        unlock(pass);
    }

    private void unlock(String pass) {
        boolean first = true;
        int position = mGroupAdapter.size();
        Iterator<Group> iterator = mHides.iterator();
        while (iterator.hasNext()) {
            Group item = iterator.next();
            if (pass != null && !pass.equals(item.getPass())) continue;
            mGroupAdapter.add(mGroupAdapter.size(), item);
            if (first) mBinding.group.setSelectedPosition(position);
            if (first) onItemClick(mGroup = item);
            iterator.remove();
            first = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActionEvent(ActionEvent event) {
        if (ActionEvent.PLAY.equals(event.getAction())) {
            onPlay();
        } else if (ActionEvent.PAUSE.equals(event.getAction())) {
            onPaused();
        } else if (ActionEvent.NEXT.equals(event.getAction())) {
            nextChannel();
        } else if (ActionEvent.PREV.equals(event.getAction())) {
            prevChannel();
        } else if (ActionEvent.STOP.equals(event.getAction())) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        switch (event.getType()) {
            case LIVE:
                setLive(getHome());
                break;
            case PLAYER:
                // 播放前避开用户标记并处理线路耗尽。
                fetch();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    // 播放成功后重置线路失败状态。
    public void onPlayerEvent(PlayerEvent event) {
        if (!event.getTag().equals(tag)) return;
        switch (event.getState()) {
            case PlayerEvent.PREPARE:
                setDecode();
                break;
            case PlayerEvent.PLAYING:
                checkPlayImg();
                break;
            case Player.STATE_BUFFERING:
                showProgress();
                break;
            case Player.STATE_READY:
                hideProgress();
                mPlayers.reset();
                // 获取设备内共享的来源状态表。
                // 成功播放后清除对应失败记录。
                if (mChannel != null) SourceHealth.get().success(mChannel.getCurrent());
                mFailedLines.clear();
                break;
            case Player.STATE_ENDED:
                checkEnded();
                break;
            case PlayerEvent.TRACK:
                setMetadata();
                setTrackVisible();
                break;
            case PlayerEvent.SIZE:
                mBinding.widget.size.setText(mPlayers.getSizeText());
                break;
        }
    }

    private void checkEnded() {
        if (mPlayers.isLive()) {
            checkNext();
        } else {
            nextChannel();
        }
    }

    private void setTrackVisible() {
        mBinding.control.text.setVisibility(mPlayers.haveTrack(C.TRACK_TYPE_TEXT) || mPlayers.isVod() ? View.VISIBLE : View.GONE);
        mBinding.control.audio.setVisibility(mPlayers.haveTrack(C.TRACK_TYPE_AUDIO) ? View.VISIBLE : View.GONE);
        mBinding.control.video.setVisibility(mPlayers.haveTrack(C.TRACK_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
        mBinding.control.speed.setVisibility(mPlayers.isVod() ? View.VISIBLE : View.GONE);
    }

    private void setMetadata() {
        String title = mBinding.widget.name.getText().toString();
        String artist = mBinding.widget.play.getText().toString();
        mPlayers.setMetadata(title, artist, mChannel.getLogo());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    // 显示原始错误并沿用直播恢复流程。
    public void onErrorEvent(ErrorEvent event) {
        if (!event.getTag().equals(tag)) return;
        Track.delete(mPlayers.getUrl());
        // 获取当前事件携带的原始异常。
        Throwable error = event.getError();
        // 显示当前错误并记录其原始异常。
        showError(event.getMsg(), error);
        mPlayers.resetTrack();
        mPlayers.reset();
        mPlayers.stop();
        // 失败后有限尝试同台的其他健康线路。
        startFlow();
    }

    // 失败后有限尝试同台的其他健康线路。
    private void startFlow() {
        if (!Setting.isChange() || mChannel == null) return;
        // 获取设备内共享的来源状态表。
        SourceHealth health = SourceHealth.get();
        // 记录失败并淘汰最早的状态。
        health.fail(mChannel.getCurrent());
        mFailedLines.add(mChannel.getCurrent());
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        // 有限选择未被标记且符合条件的备用线路。
        int next = store.next(mChannel.getUrls(), mChannel.getLine(), url -> {
            // 冷却结束后允许重新尝试来源。
            return !mFailedLines.contains(url) && health.available(url);
        });
        if (next < 0) {
            // 显示播放器异常并记录屏幕错误详情。
            showError(getString(R.string.source_exhausted));
            return;
        }
        mChannel.setLine(next);
        showInfo();
        // 播放前避开用户标记并处理线路耗尽。
        fetch();
    }

    private void prevChannel() {
        if (mGroup == null) return;
        int position = mGroup.getPosition() - 1;
        boolean limit = position < 0;
        if (Setting.isAcross() & limit) prevGroup();
        else mGroup.setPosition(limit ? mChannelAdapter.size() - 1 : position);
        if (!mGroup.isEmpty()) setChannel(mGroup.current());
    }

    private void nextChannel() {
        if (mGroup == null) return;
        int position = mGroup.getPosition() + 1;
        boolean limit = position > mChannelAdapter.size() - 1;
        if (Setting.isAcross() && limit) nextGroup();
        else mGroup.setPosition(limit ? 0 : position);
        if (!mGroup.isEmpty()) setChannel(mGroup.current());
    }

    private boolean nextGroup() {
        int position = mBinding.group.getSelectedPosition() + 1;
        if (position > mGroupAdapter.size() - 1) position = 0;
        if (mGroup.equals(mGroupAdapter.get(position))) return false;
        mGroup = (Group) mGroupAdapter.get(position);
        mBinding.group.setSelectedPosition(position);
        if (mGroup.skip()) return nextGroup();
        mChannelAdapter.setItems(mGroup.getChannel(), null);
        mGroup.setPosition(0);
        return true;
    }

    private boolean prevGroup() {
        int position = mBinding.group.getSelectedPosition() - 1;
        if (position < 0) position = mGroupAdapter.size() - 1;
        if (mGroup.equals(mGroupAdapter.get(position))) return false;
        mGroup = (Group) mGroupAdapter.get(position);
        mBinding.group.setSelectedPosition(position);
        if (mGroup.skip()) return prevGroup();
        mChannelAdapter.setItems(mGroup.getChannel(), null);
        mGroup.setPosition(mGroup.getChannel().size() - 1);
        return true;
    }

    private void checkNext() {
        int current = mChannel.getData().getInRange();
        int position = mChannel.getData().getSelected() + 1;
        boolean hasNext = position <= current && position > 0;
        if (hasNext) onItemClick(mChannel.getData().getList().get(position));
        // 播放前避开用户标记并处理线路耗尽。
        else fetch();
    }

    // 反向轮换时避开用户标记的错误内容线路。
    private void prevLine() {
        if (mChannel == null || mChannel.isOnly()) return;
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        // 反向选择未被标记且符合条件的备用线路。
        int previous = store.previous(mChannel.getUrls(), mChannel.getLine(), url -> true);
        if (previous < 0) return;
        mChannel.setLine(previous);
        mFailedLines.clear();
        showInfo();
        // 播放前避开用户标记并处理线路耗尽。
        fetch();
    }

    // 手动轮换时避开用户标记的错误内容线路。
    private void nextLine(boolean show) {
        if (mChannel == null || mChannel.isOnly()) return;
        // 获取设备共享的直播线路标记。
        LiveLineStore store = LiveLineStore.get();
        // 有限选择未被标记且符合条件的备用线路。
        int next = store.next(mChannel.getUrls(), mChannel.getLine(), url -> true);
        if (next < 0) return;
        mChannel.setLine(next);
        mFailedLines.clear();
        if (show) showInfo();
        else setInfo();
        // 播放前避开用户标记并处理线路耗尽。
        fetch();
    }

    private void seek(long time) {
        mPlayers.seek(time);
        mKeyDown.reset();
        showProgress();
        hideCenter();
    }

    private void onPaused() {
        mPlayers.pause();
    }

    private void onPlay() {
        mPlayers.play();
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    private View getFocus2() {
        return mFocus2 == null || mFocus2.getVisibility() != View.VISIBLE ? mBinding.control.config : mFocus2;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isVisible(mBinding.control.getRoot())) setR1Callback();
        if (isVisible(mBinding.control.getRoot())) mFocus2 = getCurrentFocus();
        if (mKeyDown.hasEvent(event)) mKeyDown.onKeyDown(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setUITimer() {
        App.post(mR4, Constant.INTERVAL_HIDE);
    }

    @Override
    public boolean dispatch(boolean check) {
        return !check || isGone(mBinding.recycler) && isGone(mBinding.control.getRoot());
    }

    @Override
    public void onShow(String number) {
        mBinding.widget.digital.setText(number);
        mBinding.widget.digital.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFind(String number) {
        mBinding.widget.digital.setVisibility(View.GONE);
        setPosition(LiveConfig.get().find(number, mGroupAdapter.unmodifiableList()));
    }

    @Override
    public void onSeeking(long time) {
        if (mPlayers.isLive()) return;
        mBinding.widget.center.setVisibility(View.VISIBLE);
        mBinding.widget.duration.setText(mPlayers.getDurationTime());
        mBinding.widget.position.setText(mPlayers.getPositionTime(time));
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        hideProgress();
    }

    @Override
    public void onKeyUp() {
        if (Setting.isInvert()) nextChannel();
        else prevChannel();
    }

    @Override
    public void onKeyDown() {
        if (Setting.isInvert()) prevChannel();
        else nextChannel();
    }

    @Override
    public void onKeyLeft(long time) {
        // 反向轮换时避开用户标记的错误内容线路。
        if (mPlayers.isLive()) prevLine();
        else App.post(() -> seek(time), 250);
    }

    @Override
    public void onKeyRight(long time) {
        // 手动轮换时避开用户标记的错误内容线路。
        if (mPlayers.isLive()) nextLine(true);
        else App.post(() -> seek(time), 250);
    }

    @Override
    public void onKeyCenter() {
        hideInfo();
        showUI();
    }

    @Override
    public void onMenu() {
        showControl(getFocus2());
    }

    @Override
    public void onSingleTap() {
        onToggle();
    }

    @Override
    public void onDoubleTap() {
        if (isVisible(mBinding.recycler)) hideUI();
        else if (isVisible(mBinding.control.getRoot())) hideControl();
        else onMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClock.stop().start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRedirect(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRedirect()) mPlayers.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Setting.isBackgroundOff()) onPaused();
        if (Setting.isBackgroundOff()) mClock.stop();
    }

    @Override
    protected void onBackInvoked() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else if (isVisible(mBinding.widget.bottom)) {
            hideInfo();
        } else if (isVisible(mBinding.recycler)) {
            hideUI();
        } else {
            super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        mPlayers.release();
        Source.get().exit();
        PlaybackService.stop();
        mViewModel.url.removeObserver(mObserveUrl);
        mViewModel.epg.removeObserver(mObserveEpg);
        App.removeCallbacks(mR0, mR1, mR3, mR3, mR4);
        super.onDestroy();
    }
}
