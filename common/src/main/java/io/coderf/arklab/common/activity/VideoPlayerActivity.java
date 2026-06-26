package io.coderf.arklab.common.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.TextureViewPlayerActivityBinding;
import io.coderf.arklab.common.viewmodel.VideoPlayerViewModel;
import io.coderf.arklab.common.widget.dialog.MessageDialog;
import java.util.Arrays;
import java.util.List;

import io.coderf.arklab.common.widget.video.VideoPlayerClarityOption;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerController;
import io.coderf.arklab.common.widget.video.VideoPlayerHostMode;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;

/**
 * 横屏 Activity 视频播放页，使用定制 {@link io.coderf.arklab.common.widget.video.ArkVideoPlayerView}。
 * <p>
 * 自定义配置请通过 {@link #PLAYER_CONFIG} 放入 Intent Bundle，勿在 Activity 实例上设置（存在竞态）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/6/26 9:49
 */
@AndroidEntryPoint
public class VideoPlayerActivity extends BaseActivity<VideoPlayerViewModel, TextureViewPlayerActivityBinding> {

    public static final String VIDEO_PATH = "videoPath";
    public static final String VIDEO_TITLE = "videoTitle";
    public static final String CACHE_ENABLE = "cacheEnable";
    public static final String PLAYER_CONFIG = "playerConfig";

    private VideoPlayerController controller;

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
        VideoPlayerConfig playerConfig = resolvePlayerConfig(getIntent().getExtras());
        if (playerConfig.isDefaultLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    @Override
    public void initData(Bundle bundle) {
        VideoPlayerConfig playerConfig = resolvePlayerConfig(bundle);
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

        playerConfig.setHostMode(VideoPlayerHostMode.ACTIVITY);
        playerConfig.setCacheEnable(cacheEnable);
        binding.videoPlayer.applyConfig(playerConfig);
        if (!playerConfig.hasClarityOptions()) {
            playerConfig.setClarityOptions(List.of(
                    new VideoPlayerClarityOption("原画", videoPath, "最高画质")
            ));
        }
        VideoPlayerViewHelper.setupPlayer(binding.videoPlayer, videoPath, cacheEnable, title, videoPath, playerConfig);
        controller = new VideoPlayerController(this, binding.videoPlayer, playerConfig);
        controller.attach(this::finish);
        controller.bindLifecycle(this);
        controller.bindBackPressed(getOnBackPressedDispatcher(), this);
        VideoPlayerViewHelper.startPlay(binding.videoPlayer);
    }

    @NonNull
    private static VideoPlayerConfig resolvePlayerConfig(@Nullable Bundle bundle) {
        if (bundle == null) {
            return VideoPlayerConfig.activityDefaults();
        }
        VideoPlayerConfig config = readConfig(bundle);
        return config != null ? config : VideoPlayerConfig.activityDefaults();
    }

    @Nullable
    private static VideoPlayerConfig readConfig(@NonNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getSerializable(PLAYER_CONFIG, VideoPlayerConfig.class);
        }
        Object value = bundle.getSerializable(PLAYER_CONFIG);
        return value instanceof VideoPlayerConfig ? (VideoPlayerConfig) value : null;
    }

    @NonNull
    private static Bundle createBundle(@Nullable String title,
                                       @NonNull String url,
                                       boolean cacheEnable,
                                       @Nullable VideoPlayerConfig config) {
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        bundle.putString(VIDEO_PATH, url);
        bundle.putBoolean(CACHE_ENABLE, cacheEnable);
        if (config != null) {
            config.setHostMode(VideoPlayerHostMode.ACTIVITY);
            bundle.putSerializable(PLAYER_CONFIG, config);
        }
        return bundle;
    }

    public static void show(@NonNull Context context, @NonNull Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(@NonNull Context context, @Nullable String title, @NonNull String url) {
        show(context, title, url, true, null);
    }

    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull String url,
                            boolean cacheEnable) {
        show(context, title, url, cacheEnable, null);
    }

    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull String url,
                            boolean cacheEnable,
                            @Nullable VideoPlayerConfig config) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(createBundle(title, url, cacheEnable, config));
        context.startActivity(intent);
    }
}
