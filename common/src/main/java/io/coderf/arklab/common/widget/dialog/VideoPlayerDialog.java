package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;
import io.coderf.arklab.common.widget.video.ArkVideoPlayerView;
import io.coderf.arklab.common.widget.video.VideoPlayerConfig;
import io.coderf.arklab.common.widget.video.VideoPlayerController;
import io.coderf.arklab.common.widget.video.VideoPlayerViewHelper;

/**
 * Dialog 小窗视频播放器，基于 GSY v13 定制 {@link ArkVideoPlayerView}。
 * <p>
 * 默认以小窗（16:9）展示，点击全屏按钮或 {@link #setStartExpanded(boolean)} 可进入 Dialog 内全屏模式。
 * 链式调用 {@link #builder()} 完成初始化后再 {@link #show()}。
 * </p>
 *
 * <pre>{@code
 * new VideoPlayerDialog(context)
 *     .setTitle("标题")
 *     .setVideoUrl(url)
 *     .setPlayerConfig(VideoPlayerConfig.dialogDefaults())
 *     .setStartExpanded(false)
 *     .builder()
 *     .show();
 * }</pre>
 */
public class VideoPlayerDialog extends Dialog {

    /** 播放地址 */
    private String videoUrl;
    private String title;
    private String thumbUrl;
    private boolean cacheEnable = true;
    private boolean cancelOutside = true;
    @Nullable
    private VideoPlayerDialogIconConfig iconConfig;
    @Nullable
    private VideoPlayerConfig playerConfig;
    @Nullable
    private Drawable dialogBackground;
    @Nullable
    private OnDialogInterfaceClickListener onCloseClickListener;

    private View dialogRoot;
    private ArkVideoPlayerView playerView;
    private View closeButton;
    @Nullable
    private VideoPlayerController controller;

    /** 当前是否处于 Dialog 全屏展开态 */
    private boolean expandedFullscreen;
    /** {@link #show()} 时是否直接进入全屏 */
    private boolean startExpanded;
    /** 小窗宽度占屏幕短边比例 */
    private float dialogWidthRatio = 0.88f;
    /** 小窗模式缓存的窗口与播放器尺寸（基于短边计算，避免横屏退出全屏后尺寸异常） */
    private int compactDialogWidth;
    private int compactDialogHeight;
    private int compactPlayerHeight;
    /** {@link #builder()} 完成后方可 show */
    private boolean readyToShow;
    @Nullable
    private ComponentCallbacks configurationCallbacks;
    @NonNull
    private final Context hostContext;

    public VideoPlayerDialog(@NonNull Context context) {
        super(context);
        hostContext = context;
    }

    public VideoPlayerDialog setVideoUrl(@Nullable String videoUrl) {
        this.videoUrl = videoUrl;
        return this;
    }

    public VideoPlayerDialog setTitle(@Nullable String title) {
        this.title = title;
        return this;
    }

    public VideoPlayerDialog setThumbUrl(@Nullable String thumbUrl) {
        this.thumbUrl = thumbUrl;
        return this;
    }

    public VideoPlayerDialog setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
        return this;
    }

    public VideoPlayerDialog setCanOutSide(boolean cancelOutside) {
        this.cancelOutside = cancelOutside;
        return this;
    }

    public VideoPlayerDialog setIconConfig(@Nullable VideoPlayerDialogIconConfig iconConfig) {
        this.iconConfig = iconConfig;
        return this;
    }

    public VideoPlayerDialog setPlayerConfig(@Nullable VideoPlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
        return this;
    }

    public VideoPlayerDialog setDialogBackground(@Nullable Drawable dialogBackground) {
        this.dialogBackground = dialogBackground;
        return this;
    }

    public VideoPlayerDialog setDialogWidthRatio(float dialogWidthRatio) {
        this.dialogWidthRatio = dialogWidthRatio;
        return this;
    }

    /**
     * 显示时直接进入 Dialog 全屏播放模式（跳过小窗）。
     */
    public VideoPlayerDialog setStartExpanded(boolean startExpanded) {
        this.startExpanded = startExpanded;
        return this;
    }

    public VideoPlayerDialog setOnCloseClickListener(@Nullable OnDialogInterfaceClickListener listener) {
        this.onCloseClickListener = listener;
        return this;
    }

    /**
     * 构建 Dialog 内容与播放器，必须在 {@link #show()} 之前调用。
     */
    public VideoPlayerDialog builder() {
        Context context = getContext();
        if (TextUtils.isEmpty(videoUrl)) {
            new MessageDialog(context)
                .setCanOutSide(false)
                .setMessage(context.getString(R.string.video_dialog_invalid_url))
                .setPositiveText(context.getString(R.string.confirm))
                .builder()
                .show();
            return this;
        }

        View content = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_video_player, null);
        setContentView(content);
        dialogRoot = content.findViewById(R.id.dialog_video_root);
        playerView = content.findViewById(R.id.dialog_video_player);
        closeButton = content.findViewById(R.id.btn_dialog_video_close);

        setCancelable(cancelOutside);
        setCanceledOnTouchOutside(cancelOutside);

        VideoPlayerConfig config = playerConfig != null ? playerConfig : VideoPlayerConfig.dialogDefaults();
        config.setHostMode(io.coderf.arklab.common.widget.video.VideoPlayerHostMode.DIALOG);
        config.setCacheEnable(cacheEnable);
        playerView.applyConfig(config);

        VideoPlayerViewHelper.setupPlayer(playerView, videoUrl, cacheEnable, title, iconConfig, thumbUrl);
        playerView.bindDialogFullscreenToggle(this::toggleExpandedFullscreen);

        ensureController(config);

        updateCompactDialogMetrics();
        applyDialogWindowStyle(false);
        setupCloseButton();
        setupBackKeyHandler();
        playerView.startPlayLogic();
        readyToShow = true;
        return this;
    }

    @Override
    public void show() {
        if (!readyToShow || playerView == null) {
            return;
        }
        super.show();
        if (startExpanded && !expandedFullscreen) {
            Window window = getWindow();
            if (window != null) {
                window.getDecorView().post(this::enterExpandedFullscreen);
            } else {
                enterExpandedFullscreen();
            }
        }
    }

    private void registerConfigurationCallback(@NonNull Activity activity) {
        unregisterConfigurationCallback(activity);
        configurationCallbacks = new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(@NonNull Configuration newConfig) {
                if (expandedFullscreen && controller != null) {
                    controller.onConfigurationChanged(newConfig);
                } else if (!expandedFullscreen) {
                    applyCompactDialogLayout();
                }
            }

            @Override
            public void onLowMemory() {
            }
        };
        activity.registerComponentCallbacks(configurationCallbacks);
    }

    private void unregisterConfigurationCallback(@Nullable Activity activity) {
        if (activity != null && configurationCallbacks != null) {
            activity.unregisterComponentCallbacks(configurationCallbacks);
        }
        configurationCallbacks = null;
    }

    private void setupCloseButton() {
        VideoPlayerConfig config = playerView.getPlayerConfig();
        if (iconConfig != null && iconConfig.getCloseIconRes() != 0 && closeButton instanceof android.widget.ImageView) {
            ((android.widget.ImageView) closeButton).setImageResource(iconConfig.getCloseIconRes());
        } else if (closeButton instanceof android.widget.ImageView) {
            ((android.widget.ImageView) closeButton).setImageResource(config.getIconConfig().getCloseIconRes());
        }
        closeButton.setOnClickListener(v -> {
            if (onCloseClickListener != null) {
                onCloseClickListener.onDialogClick(this);
            }
            dismiss();
        });
    }

    private void setupBackKeyHandler() {
        setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (expandedFullscreen) {
                    exitExpandedFullscreen();
                    return true;
                }
            }
            return false;
        });
    }

    private void toggleExpandedFullscreen() {
        if (expandedFullscreen) {
            exitExpandedFullscreen();
        } else {
            enterExpandedFullscreen();
        }
    }

    private void enterExpandedFullscreen() {
        expandedFullscreen = true;
        playerView.setExternalExpanded(true);
        VideoPlayerConfig config = playerView.getPlayerConfig();
        ensureController(config);
        if (controller != null) {
            controller.onEnterExpanded();
        }
        applyDialogWindowStyle(true);
        dialogRoot.setBackground(null);
        dialogRoot.setPadding(0, 0, 0, 0);
        closeButton.setVisibility(View.GONE);
        updatePlayerLayoutParams(true);
        if (playerView.getFullscreenButton() != null) {
            playerView.getFullscreenButton().setImageResource(
                playerView.getPlayerConfig().getIconConfig().getFullscreenExitIconRes());
        }
        playerView.refreshToolbarVisibility();
    }

    private void exitExpandedFullscreen() {
        expandedFullscreen = false;
        playerView.setExternalExpanded(false);
        if (controller != null) {
            controller.onExitExpanded();
        }
        Window window = getWindow();
        if (window != null) {
            View decorView = window.getDecorView();
            decorView.post(this::applyCompactDialogLayout);
            decorView.postDelayed(this::applyCompactDialogLayout, 350);
        } else {
            applyCompactDialogLayout();
        }
    }

    private void applyCompactDialogLayout() {
        updateCompactDialogMetrics();
        applyDialogWindowStyle(false);
        applyDialogCardBackground();
        if (closeButton != null) {
            closeButton.setVisibility(View.VISIBLE);
        }
        updatePlayerLayoutParams(false);
        if (playerView != null && playerView.getFullscreenButton() != null) {
            playerView.getFullscreenButton().setImageResource(
                playerView.getPlayerConfig().getIconConfig().getFullscreenIconRes());
        }
        if (playerView != null) {
            playerView.refreshToolbarVisibility();
        }
    }

    private void updateCompactDialogMetrics() {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int shortEdge = Math.min(dm.widthPixels, dm.heightPixels);
        compactDialogWidth = (int) (shortEdge * dialogWidthRatio);
        compactPlayerHeight = compactDialogWidth * 9 / 16;
        compactDialogHeight = compactPlayerHeight + DensityUtil.dp2px(getContext(), 8f);
    }

    private void applyDialogWindowStyle(boolean fullscreen) {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        if (fullscreen) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
            hideSystemUi(window);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.55f);
            applyDialogCardBackground();
            if (compactDialogWidth <= 0 || compactDialogHeight <= 0) {
                updateCompactDialogMetrics();
            }
            window.setLayout(compactDialogWidth, compactDialogHeight);
            window.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                window.setAttributes(lp);
            }
            showSystemUi(window);
        }
    }

    private void applyDialogCardBackground() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        Drawable background = dialogBackground != null ? dialogBackground : DrawableUtil.createRectDrawable(
            ContextCompat.getColor(getContext(), R.color.black),
            DensityUtil.dp2px(getContext(), 12f),
            DensityUtil.dp2px(getContext(), 12f),
            DensityUtil.dp2px(getContext(), 12f),
            DensityUtil.dp2px(getContext(), 12f)
        );
        window.setBackgroundDrawable(background);
        if (dialogRoot != null) {
            dialogRoot.setBackground(background);
            int padding = DensityUtil.dp2px(getContext(), 2f);
            dialogRoot.setPadding(padding, padding, padding, padding);
        }
    }

    private void updatePlayerLayoutParams(boolean fullscreen) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            if (compactPlayerHeight <= 0) {
                updateCompactDialogMetrics();
            }
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = compactPlayerHeight;
        }
        playerView.setLayoutParams(lp);
    }

    private static void hideSystemUi(@NonNull Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private static void showSystemUi(@NonNull Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Nullable
    private Activity resolveActivity() {
        Activity activity = unwrapActivity(getContext());
        if (activity != null) {
            return activity;
        }
        return unwrapActivity(hostContext);
    }

    private void ensureController(@NonNull VideoPlayerConfig config) {
        if (controller != null) {
            return;
        }
        Activity activity = resolveActivity();
        if (activity == null || playerView == null) {
            return;
        }
        controller = new VideoPlayerController(activity, playerView, config);
        controller.attach(() -> dismiss());
        registerConfigurationCallback(activity);
    }

    @Nullable
    private static Activity unwrapActivity(@Nullable Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == current) {
                break;
            }
            current = base;
        }
        return current instanceof Activity ? (Activity) current : null;
    }

    /**
     * 宿主 Activity onPause 时调用，暂停播放。
     */
    public void onHostPause() {
        if (controller != null) {
            controller.onHostPause();
        } else if (playerView != null) {
            playerView.onVideoPause();
        }
    }

    /**
     * 宿主 Activity onResume 时调用，恢复播放。
     */
    public void onHostResume() {
        if (controller != null) {
            controller.onHostResume();
        } else if (playerView != null) {
            playerView.onVideoResume();
        }
    }

    @Override
    public void dismiss() {
        releasePlayerResources();
        if (expandedFullscreen && controller != null) {
            controller.onExitExpanded();
            expandedFullscreen = false;
        }
        super.dismiss();
    }

    private void releasePlayerResources() {
        unregisterConfigurationCallback(resolveActivity());
        if (controller != null) {
            controller.release();
            controller = null;
        } else if (playerView != null) {
            playerView.setVideoAllCallBack(null);
            playerView.release();
            GSYVideoManager.releaseAllVideos();
        }
        playerView = null;
        readyToShow = false;
    }

    /** 快捷展示：默认小窗、不缓存 */
    public static void show(@NonNull Context context, @NonNull String title, @NonNull String url) {
        new VideoPlayerDialog(context)
            .setTitle(title)
            .setVideoUrl(url)
            .setCacheEnable(false)
            .builder()
            .show();
    }
}
