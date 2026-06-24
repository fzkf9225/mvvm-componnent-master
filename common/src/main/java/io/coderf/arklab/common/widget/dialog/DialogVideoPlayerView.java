package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.dialog.bean.VideoPlayerDialogIconConfig;

/**
 * Dialog 场景下的 GSY 播放器视图。
 * <p>
 * 继承 {@link StandardGSYVideoPlayer}，使用自定义布局 {@link R.layout#video_layout_dialog}，
 * 并支持通过 {@link VideoPlayerDialogIconConfig} 替换部分控制栏图标。
 * </p>
 */
public class DialogVideoPlayerView extends StandardGSYVideoPlayer {

  /** 全屏切换回调，由 {@link VideoPlayerDialog} 注入，用于 Dialog 展开/收起而非跳转新 Activity */
  @Nullable
  private OnFullscreenToggleListener fullscreenToggleListener;

  @DrawableRes
  private int enlargeIconRes;
  @DrawableRes
  private int shrinkIconRes;

  public DialogVideoPlayerView(Context context, Boolean fullFlag) {
    super(context, fullFlag);
  }

  public DialogVideoPlayerView(Context context) {
    super(context);
  }

  public DialogVideoPlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * 使用 Dialog 专用控制栏布局，保留 GSY 要求的控件 id，确保播放内核正常工作。
   */
  @Override
  public int getLayoutId() {
    return R.layout.video_layout_dialog;
  }

  @Override
  public int getEnlargeImageRes() {
    return enlargeIconRes != 0 ? enlargeIconRes : super.getEnlargeImageRes();
  }

  @Override
  public int getShrinkImageRes() {
    return shrinkIconRes != 0 ? shrinkIconRes : super.getShrinkImageRes();
  }

  /**
   * 应用图标配置；应在 {@link #setUp(String, boolean, String)} 之前或之后立即调用。
   */
  public void applyIconConfig(@Nullable VideoPlayerDialogIconConfig iconConfig) {
    if (iconConfig == null) {
      return;
    }
    if (iconConfig.getEnlargeIconRes() != 0) {
      enlargeIconRes = iconConfig.getEnlargeIconRes();
    }
    if (iconConfig.getShrinkIconRes() != 0) {
      shrinkIconRes = iconConfig.getShrinkIconRes();
    }
    if (getFullscreenButton() != null && enlargeIconRes != 0) {
      getFullscreenButton().setImageResource(enlargeIconRes);
    }
    if (iconConfig.getBackIconRes() != 0 && getBackButton() != null) {
      getBackButton().setImageResource(iconConfig.getBackIconRes());
    }
  }

  /**
   * 绑定全屏按钮：拦截 GSY 默认的 {@code startWindowFullscreen}，改由外部控制 Dialog 尺寸与屏幕方向。
   */
  public void bindDialogFullscreenToggle(@Nullable OnFullscreenToggleListener listener) {
    this.fullscreenToggleListener = listener;
    if (getFullscreenButton() == null) {
      return;
    }
    getFullscreenButton().setOnClickListener(v -> {
      if (fullscreenToggleListener != null) {
        fullscreenToggleListener.onToggleFullscreen();
      }
    });
  }

  /** 全屏态下图标切换为“缩小” */
  public void showShrinkFullscreenIcon() {
    if (getFullscreenButton() != null) {
      getFullscreenButton().setImageResource(getShrinkImageRes());
    }
  }

  /** 非全屏态下图标切换为“放大” */
  public void showEnlargeFullscreenIcon() {
    if (getFullscreenButton() != null) {
      getFullscreenButton().setImageResource(getEnlargeImageRes());
    }
  }

  /**
   * Dialog 全屏切换监听。
   */
  public interface OnFullscreenToggleListener {
    void onToggleFullscreen();
  }
}
