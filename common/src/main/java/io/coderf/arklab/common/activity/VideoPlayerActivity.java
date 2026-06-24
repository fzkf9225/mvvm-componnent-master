package io.coderf.arklab.common.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.TextureViewPlayerActivityBinding;
import io.coderf.arklab.common.viewmodel.VideoPlayerViewModel;
import io.coderf.arklab.common.widget.dialog.MessageDialog;
import io.coderf.arklab.common.widget.dialog.VideoPlayerViewHelper;

/**
 * Create by fz on 2019/11/27
 * describe: 全屏视频播放页，默认横屏；底部按钮仅用于横竖屏旋转，不触发 GSY 窗口全屏。
 * <p>
 * GSY 无独立旋转按钮，复用控制栏 {@code fullscreen} 位 + {@link OrientationUtils#resolveByClick()}。
 * </p>
 */
@AndroidEntryPoint
public class VideoPlayerActivity extends BaseActivity<VideoPlayerViewModel, TextureViewPlayerActivityBinding> {
    public final static String VIDEO_PATH = "videoPath";
    public final static String VIDEO_TITLE = "videoTitle";
    public final static String CACHE_ENABLE = "cacheEnable";
    private boolean cacheEnable = true;
    @Nullable
    private OrientationUtils orientationUtils;

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
    protected boolean enableImmersionBar() {
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
        return new ToolbarConfig(this)
                .setStatusBarColor(android.R.color.black)
                .setLightMode(false)
                .setEnableImmersionBar(false)
                .applyStatusBar();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void initData(Bundle bundle) {
        String videoPath = bundle.getString(VIDEO_PATH);
        cacheEnable = bundle.getBoolean(CACHE_ENABLE, cacheEnable);
        if (TextUtils.isEmpty(videoPath)) {
            new MessageDialog(this)
                    .setCanOutSide(false)
                    .setMessage(getString(R.string.video_dialog_invalid_url))
                    .setPositiveText(getString(R.string.confirm))
                    .setOnPositiveClickListener(dialog -> {
                        dialog.dismiss();
                        VideoPlayerActivity.this.finish();
                    })
                    .builder()
                    .show();
            return;
        }

        orientationUtils = new OrientationUtils(this, binding.videoPlayer);
        orientationUtils.setEnable(false);

        VideoPlayerViewHelper.prepareRotateButtonIcons(binding.videoPlayer, null);

        String title = bundle.getString(VIDEO_TITLE);
        VideoPlayerViewHelper.setupPlayer(binding.videoPlayer, videoPath, cacheEnable, title, null, videoPath);
        VideoPlayerViewHelper.applyFullscreenActivityStyle(binding.videoPlayer, null);
        VideoPlayerViewHelper.bindRotateButton(binding.videoPlayer, orientationUtils, null);
        VideoPlayerViewHelper.bindBackButton(binding.videoPlayer, this::handlePlayerBack);

        binding.videoPlayer.startPlayLogic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.videoPlayer.onVideoResume();
        if (orientationUtils != null) {
            orientationUtils.setIsPause(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.videoPlayer.onVideoPause();
        if (orientationUtils != null) {
            orientationUtils.setIsPause(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
            orientationUtils = null;
        }
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    private void handlePlayerBack() {
        binding.videoPlayer.setVideoAllCallBack(null);
        finish();
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            handlePlayerBack();
        }
    };

    public static void show(@NonNull Context context, @NonNull Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(@NonNull Context context, @NonNull String title, @NonNull String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(@NonNull Context context, @NonNull String title, @NonNull String url, boolean cacheEnable) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        bundle.putBoolean(CACHE_ENABLE, cacheEnable);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
