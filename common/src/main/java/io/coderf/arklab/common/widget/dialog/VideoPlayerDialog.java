package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogVideoPlayerBinding;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;

/**
 * Dialog 样式视频播放器。
 * <p>
 * 默认以圆角卡片形式居中展示（约 16:9 画面）；点击全屏后旋转为横屏并铺满屏幕，
 * 再次点击全屏或按返回键恢复 Dialog 样式。
 * </p>
 * <p>
 * 基于 GSYVideoPlayer v9.x，兼容当前项目 jitpack 依赖，无需升级播放器库即可在 SDK 35 使用。
 * </p>
 *
 * <pre>{@code
 * new VideoPlayerDialog(context)
 *     .setVideoUrl(url)
 *     .setTitle("示例")
 *     .setCacheEnable(false)
 *     .builder()
 *     .show();
 * }</pre>
 */
public class VideoPlayerDialog extends Dialog {

  /** 播放地址 */
  private String videoUrl;
  /** 标题 */
  private String title;
  /** 封面地址，默认与播放地址相同 */
  private String thumbUrl;
  /** 是否开启边播边缓存 */
  private boolean cacheEnable = true;
  /** 是否允许点击外部关闭 */
  private boolean cancelOutside = true;
  /** 控制栏图标配置 */
  @Nullable
  private VideoPlayerDialogIconConfig iconConfig;
  /** Dialog 圆角背景 */
  @Nullable
  private Drawable dialogBackground;
  /** 关闭按钮监听 */
  @Nullable
  private OnDialogInterfaceClickListener onCloseClickListener;

  private DialogVideoPlayerBinding binding;
  @Nullable
  private OrientationUtils orientationUtils;
  /** 是否处于“铺满屏幕”的全屏态 */
  private boolean expandedFullscreen;
  /** 进入全屏前 Activity 的屏幕方向，用于恢复 */
  private int savedScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
  /** Dialog 默认宽度占屏比 */
  private float dialogWidthRatio = 0.88f;
  /** builder 是否已成功初始化 */
  private boolean readyToShow;

  public VideoPlayerDialog(@NonNull Context context) {
    super(context);
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

  public VideoPlayerDialog setDialogBackground(@Nullable Drawable dialogBackground) {
    this.dialogBackground = dialogBackground;
    return this;
  }

  public VideoPlayerDialog setDialogWidthRatio(float dialogWidthRatio) {
    this.dialogWidthRatio = dialogWidthRatio;
    return this;
  }

  public VideoPlayerDialog setOnCloseClickListener(@Nullable OnDialogInterfaceClickListener listener) {
    this.onCloseClickListener = listener;
    return this;
  }

  /**
   * 构建并初始化 Dialog，调用后可直接 {@link #show()}。
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

    binding = DialogVideoPlayerBinding.inflate(LayoutInflater.from(context));
    setContentView(binding.getRoot());
    setCancelable(cancelOutside);
    setCanceledOnTouchOutside(cancelOutside);

    applyDialogWindowStyle(false);
    setupPlayer();
    setupCloseButton();
    setupBackKeyHandler();
    readyToShow = true;
    return this;
  }

  @Override
  public void show() {
    if (!readyToShow || binding == null) {
      return;
    }
    super.show();
  }

  private void setupPlayer() {
    DialogVideoPlayerView player = binding.dialogVideoPlayer;
    VideoPlayerViewHelper.setupPlayer(player, videoUrl, cacheEnable, title, iconConfig, thumbUrl);
    VideoPlayerViewHelper.applyDialogPlayerStyle(player, iconConfig);

    Activity activity = resolveActivity();
    if (activity != null) {
      orientationUtils = new OrientationUtils(activity, player);
      orientationUtils.setEnable(false);
    }

    player.bindDialogFullscreenToggle(this::toggleExpandedFullscreen);
    player.startPlayLogic();
  }

  private void setupCloseButton() {
    if (iconConfig != null && iconConfig.getCloseIconRes() != 0) {
      binding.btnDialogVideoClose.setImageResource(iconConfig.getCloseIconRes());
    }
    binding.btnDialogVideoClose.setOnClickListener(v -> {
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

  /**
   * 切换 Dialog 默认样式与全屏铺满样式。
   */
  private void toggleExpandedFullscreen() {
    if (expandedFullscreen) {
      exitExpandedFullscreen();
    } else {
      enterExpandedFullscreen();
    }
  }

  /** 进入全屏：横屏 + Dialog 窗口铺满 */
  private void enterExpandedFullscreen() {
    expandedFullscreen = true;
    Activity activity = resolveActivity();
    if (activity != null) {
      savedScreenOrientation = activity.getRequestedOrientation();
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }
    applyDialogWindowStyle(true);
    binding.dialogVideoRoot.setBackground(null);
    binding.dialogVideoRoot.setPadding(0, 0, 0, 0);
    binding.btnDialogVideoClose.setVisibility(View.GONE);
    updatePlayerLayoutParams(true);
    binding.dialogVideoPlayer.showShrinkFullscreenIcon();
    // GSY v9 标准布局无独立旋转按钮，仅提供 OrientationUtils 编程接口，故全屏态不额外添加旋转按钮
  }

  /** 退出全屏：恢复 Dialog 卡片样式 */
  private void exitExpandedFullscreen() {
    expandedFullscreen = false;
    Activity activity = resolveActivity();
    if (activity != null) {
      activity.setRequestedOrientation(savedScreenOrientation);
    }
    applyDialogWindowStyle(false);
    applyDialogCardBackground();
    binding.btnDialogVideoClose.setVisibility(View.VISIBLE);
    updatePlayerLayoutParams(false);
    binding.dialogVideoPlayer.showEnlargeFullscreenIcon();
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
      int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
      int dialogWidth = (int) (screenWidth * dialogWidthRatio);
      int dialogHeight = dialogWidth * 9 / 16;
      window.setLayout(dialogWidth, dialogHeight + DensityUtil.dp2px(getContext(), 8f));
      window.setGravity(Gravity.CENTER);
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
    if (binding != null) {
      binding.dialogVideoRoot.setBackground(background);
      int padding = DensityUtil.dp2px(getContext(), 2f);
      binding.dialogVideoRoot.setPadding(padding, padding, padding, padding);
    }
  }

  private void updatePlayerLayoutParams(boolean fullscreen) {
    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) binding.dialogVideoPlayer.getLayoutParams();
    if (fullscreen) {
      lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
      lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
    } else {
      lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
      int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
      int dialogWidth = (int) (screenWidth * dialogWidthRatio);
      lp.height = dialogWidth * 9 / 16;
    }
    binding.dialogVideoPlayer.setLayoutParams(lp);
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
    Context context = getContext();
    if (context instanceof Activity) {
      return (Activity) context;
    }
    return null;
  }

  /**
   * 在 dismiss 前暂停播放；建议在 Activity onPause 时若 Dialog 正在显示则调用。
   */
  public void onHostPause() {
    if (binding != null) {
      binding.dialogVideoPlayer.onVideoPause();
    }
    if (orientationUtils != null) {
      orientationUtils.setIsPause(true);
    }
  }

  /**
   * 在 Activity onResume 时恢复播放。
   */
  public void onHostResume() {
    if (binding != null) {
      binding.dialogVideoPlayer.onVideoResume();
    }
    if (orientationUtils != null) {
      orientationUtils.setIsPause(false);
    }
  }

  @Override
  public void dismiss() {
    releasePlayerResources();
    if (expandedFullscreen) {
      Activity activity = resolveActivity();
      if (activity != null) {
        activity.setRequestedOrientation(savedScreenOrientation);
      }
      expandedFullscreen = false;
    }
    super.dismiss();
  }

  private void releasePlayerResources() {
    if (binding == null) {
      return;
    }
    if (orientationUtils != null) {
      orientationUtils.releaseListener();
      orientationUtils = null;
    }
    if (binding != null) {
      binding.dialogVideoPlayer.setVideoAllCallBack(null);
      binding.dialogVideoPlayer.release();
    }
    GSYVideoManager.releaseAllVideos();
    binding = null;
    readyToShow = false;
  }

  /**
   * 快捷展示。
   */
  public static void show(@NonNull Context context, @NonNull String title, @NonNull String url) {
    new VideoPlayerDialog(context)
        .setTitle(title)
        .setVideoUrl(url)
        .setCacheEnable(false)
        .builder()
        .show();
  }
}
