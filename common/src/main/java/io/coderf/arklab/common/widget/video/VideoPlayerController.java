package io.coderf.arklab.common.widget.video;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.LifecycleOwner;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import java.util.List;
import java.util.Map;

import io.coderf.arklab.common.utils.theme.ThemeUtils;

/**
 * 绑定 Activity 与 {@link ArkVideoPlayerView}，统一处理全屏、重力旋转、倍速与清晰度切换。
 * <p>
 * 典型用法：{@code attach(onBack)} → {@link #bindLifecycle(LifecycleOwner)} →
 * {@link #bindBackPressed(OnBackPressedDispatcher, LifecycleOwner)}。
 * </p>
 */
public class VideoPlayerController {

    @NonNull
    private final Activity activity;
    @NonNull
    private final ArkVideoPlayerView player;
    @NonNull
    private VideoPlayerConfig config;

    @NonNull
    private final VideoPlayerOrientationHelper orientationHelper = new VideoPlayerOrientationHelper();
    @Nullable
    private OrientationUtils orientationUtils;
    @Nullable
    private Runnable onBackAction;
    @Nullable
    private Runnable onSystemUiRestore;
    private float currentSpeed = 1f;
    private boolean released;

    public VideoPlayerController(@NonNull Activity activity,
                                 @NonNull ArkVideoPlayerView player,
                                 @NonNull VideoPlayerConfig config) {
        this.activity = activity;
        this.player = player;
        this.config = config;
    }

    public void attach(@Nullable Runnable onBackAction) {
        attach(onBackAction, null);
    }

    /**
     * 初始化播放器交互：旋转、倍速、清晰度、全屏回调等。
     *
     * @param onBackAction       返回键或返回按钮回调（如 finish / dismiss）
     * @param onSystemUiRestore  嵌入模式退出全屏后恢复宿主状态栏 / Toolbar（可选）
     */
    public void attach(@Nullable Runnable onBackAction, @Nullable Runnable onSystemUiRestore) {
        this.onBackAction = onBackAction;
        this.onSystemUiRestore = onSystemUiRestore;
        player.applyConfig(config);
        player.setNeedOrientationUtils(false);
        player.setRotateViewAuto(false);
        orientationUtils = new OrientationUtils(activity, player);
        orientationUtils.setEnable(false);
        orientationHelper.bindOrientationUtils(orientationUtils);
        orientationHelper.setGravityEnabled(config.isGravityRotationEnabled());

        if (config.getHostMode() == VideoPlayerHostMode.ACTIVITY) {
            orientationHelper.setExpanded(true);
            if (config.isGravityRotationEnabled()) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }

        bindToCurrentPlayer();
        player.setVideoAllCallBack(createVideoCallback());
        refreshToolbar();
    }

    /**
     * 绑定宿主 Lifecycle，自动处理 pause / resume / destroy 与屏幕旋转。
     */
    public void bindLifecycle(@NonNull LifecycleOwner owner) {
        Activity host = VideoPlayerLifecycleObserver.resolveActivity(owner, activity);
        VideoPlayerLifecycleObserver.bind(this, owner, host);
    }

    /**
     * 绑定返回键，优先处理播放器全屏退出。
     */
    public void bindBackPressed(@NonNull OnBackPressedDispatcher dispatcher,
                                @NonNull LifecycleOwner owner) {
        dispatcher.addCallback(owner, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (VideoPlayerController.this.onBackPressed()) {
                    return;
                }
                setEnabled(false);
                dispatcher.onBackPressed();
            }
        });
    }

    public void updateConfig(@NonNull VideoPlayerConfig config) {
        this.config = config;
        player.applyConfig(config);
        orientationHelper.setGravityEnabled(config.isGravityRotationEnabled());
        refreshToolbar();
    }

    public void onHostPause() {
        player.getCurrentPlayer().onVideoPause();
        orientationHelper.onHostPause();
    }

    public void onHostResume() {
        player.getCurrentPlayer().onVideoResume();
        orientationHelper.onHostResume();
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        orientationHelper.onConfigurationChanged(activity, newConfig);
        refreshToolbar();
    }

    /** Dialog 进入全屏展开时调用 */
    public void onEnterExpanded() {
        orientationHelper.setExpanded(true);
        orientationHelper.onEnterExpanded(activity);
        bindToCurrentPlayer();
        refreshToolbar();
    }

    /** Dialog 退出全屏展开时调用 */
    public void onExitExpanded() {
        orientationHelper.onExitExpanded(activity);
        orientationHelper.setExpanded(false);
        bindToCurrentPlayer();
        refreshToolbar();
    }

    /**
     * 处理返回：优先退出 GSY 全屏，否则执行 {@link #attach} 传入的 onBackAction。
     *
     * @return true 表示已消费
     */
    public boolean onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(activity)) {
            orientationHelper.onExitExpanded(activity);
            orientationHelper.setExpanded(false);
            restoreHostSystemUi();
            bindToCurrentPlayer();
            refreshToolbar();
            return true;
        }
        if (onBackAction != null) {
            onBackAction.run();
            return true;
        }
        return false;
    }

    /** 释放播放器与 OrientationUtils，在宿主 onDestroy 或 {@link #bindLifecycle} 自动触发 */
    public void release() {
        if (released) {
            return;
        }
        released = true;
        orientationHelper.release();
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
            orientationUtils = null;
        }
        player.setVideoAllCallBack(null);
        player.release();
        GSYVideoManager.releaseAllVideos();
    }

    @Nullable
    public OrientationUtils getOrientationUtils() {
        return orientationUtils;
    }

    @NonNull
    private ArkVideoPlayerView getArkPlayer() {
        GSYBaseVideoPlayer current = player.getCurrentPlayer();
        if (current instanceof ArkVideoPlayerView) {
            return (ArkVideoPlayerView) current;
        }
        return player;
    }

    private void bindToCurrentPlayer() {
        ArkVideoPlayerView target = getArkPlayer();
        bindActions(target);
        target.setSpeedLabel(currentSpeed);
        target.refreshToolbarVisibility();
    }

    private void bindActions(@NonNull ArkVideoPlayerView target) {
        if (target.getBackButton() != null) {
            target.getBackButton().setOnClickListener(v -> onBackPressed());
        }
        target.setBackFromFullScreenListener(v -> onBackPressed());

        if (target.getRotateButton() != null) {
            target.getRotateButton().setOnClickListener(v ->
                orientationHelper.toggleManualRotation(activity));
        }

        AppCompatTextView speedButton = target.getSpeedButton();
        if (speedButton != null) {
            speedButton.setOnClickListener(v -> cycleSpeed());
        }

        AppCompatTextView clarityButton = target.getClarityButton();
        if (clarityButton != null) {
            clarityButton.setOnClickListener(v -> showClarityDialog());
        }

        if (config.getHostMode() == VideoPlayerHostMode.EMBED && target.getFullscreenButton() != null) {
            target.getFullscreenButton().setOnClickListener(v -> {
                if (target.isToolbarExpanded()) {
                    GSYVideoManager.backFromWindowFull(activity);
                    orientationHelper.onExitExpanded(activity);
                    orientationHelper.setExpanded(false);
                    restoreHostSystemUi();
                    bindToCurrentPlayer();
                } else {
                    orientationHelper.setExpanded(true);
                    orientationHelper.onEnterExpanded(activity);
                    target.startWindowFullscreen(activity, false, true);
                    ThemeUtils.applyHideSystemBarsImmersive(activity);
                    bindToCurrentPlayer();
                }
            });
        }
    }

    private void restoreHostSystemUi() {
        if (config.getHostMode() != VideoPlayerHostMode.EMBED) {
            return;
        }
        ThemeUtils.restoreSystemBarsAfterImmersive(activity);
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            View toolbar = ((AppCompatActivity) activity).findViewById(
                io.coderf.arklab.common.R.id.main_bar);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
        if (onSystemUiRestore != null) {
            onSystemUiRestore.run();
        }
    }

    private void cycleSpeed() {
        currentSpeed = config.nextSpeed(currentSpeed);
        getArkPlayer().setSpeedLabel(currentSpeed);
        player.getCurrentPlayer().setSpeedPlaying(currentSpeed, true);
    }

    private void showClarityDialog() {
        List<VideoPlayerClarityOption> options = config.getClarityOptions();
        if (options.isEmpty()) {
            Toast.makeText(activity, io.coderf.arklab.common.R.string.video_clarity_origin, Toast.LENGTH_SHORT).show();
            return;
        }
        new VideoPlayerClarityDialog(activity, options, config.getSelectedClarityIndex(),
            this::switchClarity).show();
    }

    private void switchClarity(int index, @NonNull VideoPlayerClarityOption option) {
        if (index == config.getSelectedClarityIndex()) {
            return;
        }
        long position = player.getCurrentPlayer().getCurrentPositionWhenPlaying();
        boolean wasPlaying = player.getCurrentPlayer().getCurrentState()
            == GSYVideoView.CURRENT_STATE_PLAYING;
        config.setSelectedClarityIndex(index);

        String title = player.getTitleTextView() != null
            ? player.getTitleTextView().getText().toString() : "";
        player.setUp(option.url(), config.isCacheEnable(), title);
        Map<String, String> headers = option.headers();
        if (!headers.isEmpty()) {
            player.setMapHeadData(headers);
        }
        player.setSeekOnStart(position);
        player.startPlayLogic();
        if (!wasPlaying) {
            player.getCurrentPlayer().onVideoPause();
        }
        getArkPlayer().refreshToolbarVisibility();
        Toast.makeText(activity, option.name(), Toast.LENGTH_SHORT).show();
    }

    private void refreshToolbar() {
        getArkPlayer().refreshToolbarVisibility();
    }

    @NonNull
    private VideoAllCallBack createVideoCallback() {
        return new VideoAllCallBack() {
            @Override
            public void onStartPrepared(String url, Object... objects) {
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                player.getCurrentPlayer().setSpeedPlaying(config.getDefaultSpeed(), true);
                currentSpeed = config.getDefaultSpeed();
                getArkPlayer().setSpeedLabel(currentSpeed);
            }

            @Override
            public void onClickStartIcon(String url, Object... objects) {
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
            }

            @Override
            public void onClickStop(String url, Object... objects) {
            }

            @Override
            public void onClickStopFullscreen(String url, Object... objects) {
            }

            @Override
            public void onClickResume(String url, Object... objects) {
            }

            @Override
            public void onClickResumeFullscreen(String url, Object... objects) {
            }

            @Override
            public void onClickSeekbar(String url, Object... objects) {
            }

            @Override
            public void onClickSeekbarFullscreen(String url, Object... objects) {
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
            }

            @Override
            public void onComplete(String url, Object... objects) {
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                orientationHelper.setExpanded(true);
                orientationHelper.onEnterExpanded(activity);
                bindToCurrentPlayer();
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                orientationHelper.onExitExpanded(activity);
                orientationHelper.setExpanded(false);
                restoreHostSystemUi();
                bindToCurrentPlayer();
            }

            @Override
            public void onQuitSmallWidget(String url, Object... objects) {
            }

            @Override
            public void onEnterSmallWidget(String url, Object... objects) {
            }

            @Override
            public void onClickStartThumb(String url, Object... objects) {
            }

            @Override
            public void onClickBlank(String url, Object... objects) {
            }

            @Override
            public void onClickBlankFullscreen(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekVolume(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekPosition(String url, Object... objects) {
            }

            @Override
            public void onTouchScreenSeekLight(String url, Object... objects) {
            }

            @Override
            public void onPlayError(String url, Object... objects) {
            }
        };
    }
}
