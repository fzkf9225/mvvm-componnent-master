package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import io.coderf.arklab.common.utils.theme.ThemeUtils;
import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;

/**
 * 视频播放器通用配置工具。
 * <p>
 * 抽取 Activity / Dialog 共用的封面、标题、图标与手势配置，避免重复代码。
 * </p>
 */
public final class VideoPlayerViewHelper {

  private VideoPlayerViewHelper() {
  }

  /**
   * 为 GSY 播放器设置封面、标题、手势等基础能力。
   *
   * @param player       播放器实例
   * @param videoUrl     播放地址
   * @param cacheEnable  是否边播边缓存
   * @param title        标题，可为空
   * @param iconConfig   图标配置，可为空
   * @param thumbUrl     封面地址，为空时回退为 videoUrl
   */
  public static void setupPlayer(@NonNull StandardGSYVideoPlayer player,
                                 @NonNull String videoUrl,
                                 boolean cacheEnable,
                                 @Nullable String title,
                                 @Nullable VideoPlayerDialogIconConfig iconConfig,
                                 @Nullable String thumbUrl) {
    player.setUp(videoUrl, cacheEnable, TextUtils.isEmpty(title) ? "" : title);
    player.setThumbImageView(createThumbImageView(player.getContext(),
        TextUtils.isEmpty(thumbUrl) ? videoUrl : thumbUrl));
    if (player.getTitleTextView() != null) {
      player.getTitleTextView().setVisibility(
          TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    }
  }

  /**
   * 应用 Activity 全屏播放场景：显示返回键与旋转按钮，开启手势调节。
   * <p>
   * Activity 本身已铺满屏幕，不再使用 GSY 窗口全屏能力；底部原「全屏」按钮位改为旋转屏幕。
   * </p>
   */
  public static void applyFullscreenActivityStyle(@NonNull StandardGSYVideoPlayer player,
                                                  @Nullable VideoPlayerDialogIconConfig iconConfig) {
    if (player.getBackButton() != null) {
      player.getBackButton().setVisibility(View.VISIBLE);
    }
    player.setIsTouchWiget(true);
    applyIconConfig(player, iconConfig);
  }

  /**
   * 绑定返回键：同时覆盖普通态与 GSY 窗口全屏态（旋转后库内部可能切入全屏层）。
   */
  public static void bindBackButton(@NonNull StandardGSYVideoPlayer player, @NonNull Runnable onBack) {
    View.OnClickListener listener = v -> onBack.run();
    if (player.getBackButton() != null) {
      player.getBackButton().setOnClickListener(listener);
    }
    player.setBackFromFullScreenListener(listener);
  }

  /**
   * 隐藏系统状态栏/导航栏，视频真正铺满屏幕（沉浸式）。
   *
   * @see io.coderf.arklab.common.utils.theme.ThemeUtils#applyHideSystemBarsImmersive(Activity)
   */
  public static void applyImmersiveFullscreen(@NonNull Activity activity) {
    ThemeUtils.applyHideSystemBarsImmersive(activity);
  }

  /**
   * 配置旋转按钮图标（必须在 {@link #setupPlayer} / {@code setUp} 之前调用）。
   */
  public static void prepareRotateButtonIcons(@NonNull StandardGSYVideoPlayer player,
                                              @Nullable VideoPlayerDialogIconConfig iconConfig) {
    int rotateIcon = io.coderf.arklab.common.R.drawable.ic_video_screen_rotate;
    if (iconConfig != null && iconConfig.getRotateIconRes() != 0) {
      rotateIcon = iconConfig.getRotateIconRes();
    }
    player.setEnlargeImageRes(rotateIcon);
    player.setShrinkImageRes(rotateIcon);
  }

  /**
   * 将 GSY 控制栏全屏按钮绑定为旋转屏幕（须在播放器 {@code setUp} 之后调用）。
   */
  public static void bindRotateButton(@NonNull StandardGSYVideoPlayer player,
                                      @NonNull com.shuyu.gsyvideoplayer.utils.OrientationUtils orientationUtils,
                                      @Nullable VideoPlayerDialogIconConfig iconConfig) {
    int rotateIcon = io.coderf.arklab.common.R.drawable.ic_video_screen_rotate;
    if (iconConfig != null && iconConfig.getRotateIconRes() != 0) {
      rotateIcon = iconConfig.getRotateIconRes();
    }
    if (player.getFullscreenButton() == null) {
      return;
    }
    player.getFullscreenButton().setVisibility(View.VISIBLE);
    player.getFullscreenButton().setImageResource(rotateIcon);
    player.getFullscreenButton().setOnClickListener(
        v -> orientationUtils.resolveByClick());
  }

  /**
   * @deprecated 使用 {@link #applyFullscreenActivityStyle}
   */
  @Deprecated
  public static void applyLandscapeActivityStyle(@NonNull StandardGSYVideoPlayer player,
                                                 @Nullable VideoPlayerDialogIconConfig iconConfig) {
    applyFullscreenActivityStyle(player, iconConfig);
  }

  /**
   * 应用 Dialog 场景：隐藏 GSY 自带返回键（使用 Dialog 关闭按钮），保留全屏切换。
   */
  public static void applyDialogPlayerStyle(@NonNull DialogVideoPlayerView player,
                                            @Nullable VideoPlayerDialogIconConfig iconConfig) {
    if (player.getBackButton() != null) {
      player.getBackButton().setVisibility(View.GONE);
    }
    if (player.getFullscreenButton() != null) {
      player.getFullscreenButton().setVisibility(View.VISIBLE);
    }
    player.setIsTouchWiget(true);
    player.applyIconConfig(iconConfig);
  }

  /**
   * 将图标配置应用到任意 StandardGSYVideoPlayer。
   */
  public static void applyIconConfig(@NonNull StandardGSYVideoPlayer player,
                                     @Nullable VideoPlayerDialogIconConfig iconConfig) {
    if (iconConfig == null) {
      return;
    }
    if (player instanceof DialogVideoPlayerView) {
      ((DialogVideoPlayerView) player).applyIconConfig(iconConfig);
      return;
    }
    if (iconConfig.getEnlargeIconRes() != 0 && player.getFullscreenButton() != null) {
      player.getFullscreenButton().setImageResource(iconConfig.getEnlargeIconRes());
    }
    if (iconConfig.getBackIconRes() != 0 && player.getBackButton() != null) {
      player.getBackButton().setImageResource(iconConfig.getBackIconRes());
    }
  }

  /**
   * 创建居中裁剪的封面 ImageView，供 GSY 在首帧加载前展示。
   */
  @NonNull
  public static ImageView createThumbImageView(@NonNull Context context, @NonNull String imageUrl) {
    AppCompatImageView imageView = new AppCompatImageView(context);
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    Glide.with(context).load(imageUrl).into(imageView);
    return imageView;
  }
}
