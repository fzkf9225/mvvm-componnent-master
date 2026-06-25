package io.coderf.arklab.common.widget.video;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import java.util.List;
import java.util.Map;

import io.coderf.arklab.common.utils.theme.ThemeUtils;

/**
 * 绑定 Activity 与 {@link ArkVideoPlayerView}，处理全屏、重力旋转、倍速与清晰度切换。
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

    public void onEnterExpanded() {
        orientationHelper.setExpanded(true);
        orientationHelper.onEnterExpanded(activity);
        bindToCurrentPlayer();
        refreshToolbar();
    }

    public void onExitExpanded() {
        orientationHelper.onExitExpanded(activity);
        orientationHelper.setExpanded(false);
        bindToCurrentPlayer();
        refreshToolbar();
    }

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

        TextView speedButton = target.getSpeedButton();
        if (speedButton != null) {
            speedButton.setOnClickListener(v -> cycleSpeed());
        }

        TextView clarityButton = target.getClarityButton();
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
            Toast.makeText(activity, io.coderf.arklab.common.R.string.video_clarity_origin,
                Toast.LENGTH_SHORT).show();
            return;
        }
        if (options.size() == 1) {
            Toast.makeText(activity, options.get(0).name(), Toast.LENGTH_SHORT).show();
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
