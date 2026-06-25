package io.coderf.arklab.common.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.TextureViewPlayerActivityBinding;
import io.coderf.arklab.common.viewmodel.VideoPlayerViewModel;
import io.coderf.arklab.common.widget.dialog.MessageDialog;
import java.util.Arrays;

import io.coderf.arklab.common.widget.video.VideoPlayerClarityOption;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerController;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;

/**
 * 横屏 Activity 视频播放页，使用定制 {@link io.coderf.arklab.common.widget.video.ArkVideoPlayerView}。
 */
@AndroidEntryPoint
public class VideoPlayerActivity extends BaseActivity<VideoPlayerViewModel, TextureViewPlayerActivityBinding> {

    public static final String VIDEO_PATH = "videoPath";
    public static final String VIDEO_TITLE = "videoTitle";
    public static final String CACHE_ENABLE = "cacheEnable";

    private VideoPlayerController controller;
    private VideoPlayerConfig playerConfig = VideoPlayerConfig.activityDefaults();

    @Override
    protected int getLayoutId() {
        return R.layout.texture_view_player_activity;
    }

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected boolean shouldApplyEdgeToEdge() {
        return false;
    }

    @Override
    protected boolean shouldHideStatusBar() {
        return true;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public ToolbarConfig createdToolbarConfig() {
        return new ToolbarConfig(this).setStatusBarColor(android.R.color.black).setLightMode(false).applyStatusBar();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        if (playerConfig.isDefaultLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    @Override
    public void initData(Bundle bundle) {
        String videoPath = bundle.getString(VIDEO_PATH);
        boolean cacheEnable = bundle.getBoolean(CACHE_ENABLE, playerConfig.isCacheEnable());
        String title = bundle.getString(VIDEO_TITLE);
        if (TextUtils.isEmpty(videoPath)) {
            new MessageDialog(this)
                .setCanOutSide(false)
                .setMessage(getString(R.string.video_dialog_invalid_url))
                .setPositiveText(getString(R.string.confirm))
                .setOnPositiveClickListener(dialog -> {
                    dialog.dismiss();
                    finish();
                })
                .builder()
                .show();
            return;
        }

        binding.videoPlayer.applyConfig(playerConfig);
        playerConfig.setClarityOptions(Arrays.asList(
            new VideoPlayerClarityOption("原画", videoPath, "最高画质"),
            new VideoPlayerClarityOption("流畅", videoPath, "节省流量")
        ));
        VideoPlayerViewHelper.setupPlayer(binding.videoPlayer, videoPath, cacheEnable, title, videoPath, playerConfig);
        controller = new VideoPlayerController(this, binding.videoPlayer, playerConfig);
        controller.attach(this::finish);
        VideoPlayerViewHelper.startPlay(binding.videoPlayer);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (controller != null) {
            controller.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (controller != null) {
            controller.onHostPause();
        } else {
            binding.videoPlayer.onVideoPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            controller.onHostResume();
        } else {
            binding.videoPlayer.onVideoResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (controller != null) {
            controller.release();
            controller = null;
        } else {
            GSYVideoManager.releaseAllVideos();
        }
        super.onDestroy();
    }

    public VideoPlayerActivity setPlayerConfig(VideoPlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
        return this;
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (controller != null && controller.onBackPressed()) {
                return;
            }
            finish();
        }
    };

    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String title, String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String title, String url, boolean cacheEnable) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        bundle.putBoolean(CACHE_ENABLE, cacheEnable);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
