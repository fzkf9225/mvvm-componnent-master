package io.coderf.arklab.common.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.base.ToolbarConfig;
import io.coderf.arklab.common.databinding.TextureViewPlayerActivityBinding;
import io.coderf.arklab.common.viewmodel.VideoPlayerViewModel;
import io.coderf.arklab.common.widget.dialog.MessageDialog;
import io.coderf.arklab.common.widget.video.VideoPlayerClarityOption;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerController;
import io.coderf.arklab.common.widget.video.VideoPlayerHostMode;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;

/**
 * 横屏 Activity 视频播放页，使用定制 {@link io.coderf.arklab.common.widget.video.ArkVideoPlayerView}。
 * <p>
 * 支持网络地址（http/https）与本地 {@link Uri}（{@code content://} / {@code file://}）。
 * 自定义配置请通过 {@link #PLAYER_CONFIG} 放入 Intent Bundle，勿在 Activity 实例上设置（存在竞态）。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/6/26 9:49
 * @updated 2026/10/22
 */
@AndroidEntryPoint
public class VideoPlayerActivity extends BaseActivity<VideoPlayerViewModel, TextureViewPlayerActivityBinding> {

    /** 播放地址（String）：网络 URL，或 {@link Uri#toString()} 形式的本地路径 */
    public static final String VIDEO_PATH = "videoPath";
    /** 播放地址（Parcelable {@link Uri}），与 {@link #VIDEO_PATH} 二选一；同时存在时优先 Uri */
    public static final String VIDEO_URI = "videoUri";
    /** 标题文案，可为空 */
    public static final String VIDEO_TITLE = "videoTitle";
    /** 是否启用边播边缓存；本地 Uri 建议传 false */
    public static final String CACHE_ENABLE = "cacheEnable";
    /** 可选的 {@link VideoPlayerConfig}，需实现 Serializable */
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
        String videoPath = resolveVideoPath(bundle);
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

        // 本地 content/file 地址关闭缓存，避免 GSY 对 content URI 做无意义的代理缓存
        if (isLocalMediaPath(videoPath)) {
            cacheEnable = false;
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

    /**
     * 从 Bundle 解析播放地址：优先 {@link #VIDEO_URI}，否则 {@link #VIDEO_PATH}。
     *
     * @param bundle Intent extras，不可为 null（BaseActivity 保证）
     * @return 可供 GSY {@code setUp} 使用的地址字符串，可能为空
     */
    @Nullable
    private static String resolveVideoPath(@NonNull Bundle bundle) {
        Uri uri = readUri(bundle);
        if (uri != null) {
            return uri.toString();
        }
        return bundle.getString(VIDEO_PATH);
    }

    @Nullable
    private static Uri readUri(@NonNull Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(VIDEO_URI, Uri.class);
        }
        return bundle.getParcelable(VIDEO_URI);
    }

    /**
     * 判断是否为本地媒体地址（content / file / 绝对路径），此类地址不建议开启边播边缓存。
     */
    private static boolean isLocalMediaPath(@NonNull String path) {
        String lower = path.toLowerCase();
        return lower.startsWith("content:")
                || lower.startsWith("file:")
                || lower.startsWith("/");
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
                                       @Nullable String url,
                                       @Nullable Uri uri,
                                       boolean cacheEnable,
                                       @Nullable VideoPlayerConfig config) {
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_TITLE, title);
        if (uri != null) {
            bundle.putParcelable(VIDEO_URI, uri);
            // 同步写入 String，兼容只读 VIDEO_PATH 的旧逻辑
            bundle.putString(VIDEO_PATH, uri.toString());
        } else if (url != null) {
            bundle.putString(VIDEO_PATH, url);
        }
        bundle.putBoolean(CACHE_ENABLE, cacheEnable);
        if (config != null) {
            config.setHostMode(VideoPlayerHostMode.ACTIVITY);
            bundle.putSerializable(PLAYER_CONFIG, config);
        }
        return bundle;
    }

    /**
     * 使用已组装好的 Bundle 启动播放页。
     *
     * @param context 上下文
     * @param bundle  需包含 {@link #VIDEO_PATH} 或 {@link #VIDEO_URI}
     */
    public static void show(@NonNull Context context, @NonNull Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtras(bundle);
        // content Uri 跨进程时尽量携带读权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    /**
     * 播放网络或本地 String 地址（http / file 路径 / Uri.toString()）。
     *
     * @param context 上下文
     * @param title   标题，可为 null
     * @param url     播放地址，不可为空
     */
    public static void show(@NonNull Context context, @Nullable String title, @NonNull String url) {
        show(context, title, url, true, null);
    }

    /**
     * @param cacheEnable 是否边播边缓存；本地文件建议 false
     */
    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull String url,
                            boolean cacheEnable) {
        show(context, title, url, cacheEnable, null);
    }

    /**
     * @param config 可选播放器配置，null 时使用 {@link VideoPlayerConfig#activityDefaults()}
     */
    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull String url,
                            boolean cacheEnable,
                            @Nullable VideoPlayerConfig config) {
        show(context, createBundle(title, url, null, cacheEnable, config));
    }

    /**
     * 播放 {@link Uri} 媒体（相册 content://、FileProvider file 等）。
     * <p>
     * 内部会转为 {@link Uri#toString()} 交给 GSY；本地 Uri 默认关闭缓存。
     * </p>
     *
     * @param context 上下文
     * @param title   标题，可为 null
     * @param uri     媒体 Uri，不可为 null
     */
    public static void show(@NonNull Context context, @Nullable String title, @NonNull Uri uri) {
        show(context, title, uri, false, null);
    }

    /**
     * @param cacheEnable 本地 Uri 建议传 false
     */
    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull Uri uri,
                            boolean cacheEnable) {
        show(context, title, uri, cacheEnable, null);
    }

    /**
     * @param config 可选播放器配置，null 时使用 Activity 默认配置
     */
    public static void show(@NonNull Context context,
                            @Nullable String title,
                            @NonNull Uri uri,
                            boolean cacheEnable,
                            @Nullable VideoPlayerConfig config) {
        show(context, createBundle(title, null, uri, cacheEnable, config));
    }
}
