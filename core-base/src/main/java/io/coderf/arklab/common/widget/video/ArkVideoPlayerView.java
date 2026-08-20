package io.coderf.arklab.common.widget.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;

/**
 * Ark 定制 GSY 播放器 View，支持嵌入 / Activity / Dialog 三种宿主模式。
 * <p>
 * 工具栏显隐由 {@link VideoPlayerConfig} 与 {@link #isToolbarExpanded()} 共同决定：
 * 嵌入模式非全屏仅展示全屏按钮；Activity 始终展示完整工具栏；Dialog 展开后展示完整工具栏。
 * 业务侧需配合 {@link VideoPlayerController} 处理旋转、倍速、清晰度与生命周期。
 * </p>
 *
 * @see VideoPlayerConfig
 * @see VideoPlayerController
 */
public class ArkVideoPlayerView extends StandardGSYVideoPlayer {

    /** 播放器行为与 UI 配置 */
    @NonNull
    private VideoPlayerConfig config = VideoPlayerConfig.embedDefaults();

    @Nullable
    private AppCompatImageView btnRotate;
    @Nullable
    private AppCompatTextView btnSpeed;
    @Nullable
    private AppCompatTextView btnClarity;
    @Nullable
    private AppCompatImageView btnBottomPlay;

    /** Dialog 模式下由 {@link VideoPlayerDialog} 控制的全屏展开态（非 GSY Window 全屏） */
    private boolean externalExpanded;
    /** 当前倍速，用于底部倍速 Chip 展示 */
    private float speedLabel = 1f;

    @Nullable
    private OnFullscreenToggleListener fullscreenToggleListener;

    public ArkVideoPlayerView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ArkVideoPlayerView(Context context) {
        super(context);
    }

    public ArkVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_ark;
    }

    @Override
    protected void init(Context context) {
        ensureConfig();
        super.init(context);
        btnRotate = findViewById(R.id.btn_rotate);
        btnSpeed = findViewById(R.id.btn_speed);
        btnClarity = findViewById(R.id.btn_clarity);
        btnBottomPlay = findViewById(R.id.btn_bottom_play);

        setNeedLockFull(true);
        setRotateViewAuto(false);
        setRotateWithSystem(false);
        setShowFullAnimation(true);

        if (btnRotate != null) {
            btnRotate.setClickable(true);
        }
        if (btnSpeed != null) {
            btnSpeed.setClickable(true);
        }
        if (btnClarity != null) {
            btnClarity.setClickable(true);
        }
        if (btnBottomPlay != null) {
            btnBottomPlay.setOnClickListener(v -> clickStartIcon());
        }
        applyConfigInternal();
    }

    @NonNull
    public VideoPlayerConfig getPlayerConfig() {
        ensureConfig();
        return config;
    }

    public void applyConfig(@NonNull VideoPlayerConfig config) {
        this.config = config;
        applyConfigInternal();
    }

    /**
     * Dialog 宿主下设置外部全屏展开态，并刷新工具栏。
     */
    public void setExternalExpanded(boolean externalExpanded) {
        this.externalExpanded = externalExpanded;
        refreshToolbarVisibility();
    }

    public boolean isExternalExpanded() {
        return externalExpanded;
    }

    /**
     * 是否处于「工具栏展开」态：Activity 恒为 true；Dialog 看 {@link #externalExpanded}；嵌入看 GSY 全屏。
     */
    public boolean isToolbarExpanded() {
        ensureConfig();
        if (config.getHostMode() == VideoPlayerHostMode.ACTIVITY) {
            return true;
        }
        if (config.getHostMode() == VideoPlayerHostMode.DIALOG) {
            return externalExpanded;
        }
        return mIfCurrentIsFullscreen;
    }

    public void setSpeedLabel(float speed) {
        this.speedLabel = speed;
        updateSpeedChipText();
    }

    /**
     * Dialog 模式绑定全屏按钮切换，由 {@link VideoPlayerDialog} 实现窗口尺寸变化。
     */
    public void bindDialogFullscreenToggle(@Nullable OnFullscreenToggleListener listener) {
        this.fullscreenToggleListener = listener;
        bindFullscreenClick();
    }

    private void bindFullscreenClick() {
        if (mFullscreenButton == null) {
            return;
        }
        if (config.getHostMode() == VideoPlayerHostMode.DIALOG && fullscreenToggleListener != null) {
            mFullscreenButton.setOnClickListener(v -> fullscreenToggleListener.onToggleFullscreen());
        }
    }

    /**
     * 按 {@link VideoPlayerConfig} 刷新各控件显隐、图标与嵌入圆角。
     */
    public void refreshToolbarVisibility() {
        ensureConfig();
        boolean expanded = isToolbarExpanded();
        VideoPlayerIconConfig icons = config.getIconConfig();

        boolean showBack = shouldShowBack(expanded);
        if (mBackButton != null) {
            mBackButton.setVisibility(showBack ? VISIBLE : GONE);
            mBackButton.setImageResource(icons.getBackIconRes());
        }
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(config.isShowTitle() && showBack ? VISIBLE : GONE);
        }
        if (mStartButton != null) {
            mStartButton.setVisibility(config.isShowCenterPlay() ? VISIBLE : GONE);
        }
        if (btnBottomPlay != null) {
            btnBottomPlay.setVisibility(config.isShowBottomPlay() ? VISIBLE : GONE);
        }

        boolean showFullscreenBtn = config.isShowFullscreen()
            && config.getHostMode() != VideoPlayerHostMode.ACTIVITY;
        if (mFullscreenButton != null) {
            mFullscreenButton.setVisibility(showFullscreenBtn ? VISIBLE : GONE);
            if (showFullscreenBtn) {
                mFullscreenButton.setImageResource(expanded
                    ? icons.getFullscreenExitIconRes() : icons.getFullscreenIconRes());
            }
        }

        if (btnRotate != null) {
            btnRotate.setVisibility(expanded && config.isShowRotate() ? VISIBLE : GONE);
            btnRotate.setImageResource(icons.getRotateIconRes());
        }
        if (btnSpeed != null) {
            btnSpeed.setVisibility(expanded && config.isShowSpeed() ? VISIBLE : GONE);
            updateSpeedChipText();
        }
        if (btnClarity != null) {
            btnClarity.setVisibility(expanded && config.isShowClarity() ? VISIBLE : GONE);
            updateClarityChipText();
        }

        applyLockScreenState();

        setEnlargeImageRes(icons.getFullscreenIconRes());
        setShrinkImageRes(icons.getFullscreenExitIconRes());
        updateStartImage();
        bindFullscreenClick();
        applyCornerRadius();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        applyCornerRadius();
    }

    private void applyCornerRadius() {
        float radiusDp = config.getCornerRadiusDp();
        if (radiusDp <= 0f || isToolbarExpanded()) {
            setClipToOutline(false);
            setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            return;
        }
        final float radiusPx = DensityUtil.dp2px(getContext(), radiusDp);
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        invalidateOutline();
    }

    private void applyLockScreenState() {
        if (mLockScreen == null) {
            return;
        }
        VideoPlayerIconConfig icons = config.getIconConfig();
        boolean allowLock = isToolbarExpanded() && config.isShowLock();
        if (mLockCurScreen && mNeedLockFull) {
            mLockScreen.setVisibility(VISIBLE);
            mLockScreen.setImageResource(icons.getLockIconRes());
        } else if (allowLock) {
            mLockScreen.setVisibility(VISIBLE);
            mLockScreen.setImageResource(icons.getUnlockIconRes());
        } else {
            mLockScreen.setVisibility(GONE);
        }
    }

    private boolean shouldShowBack(boolean expanded) {
        if (config.getHostMode() == VideoPlayerHostMode.EMBED) {
            return expanded;
        }
        if (config.getHostMode() == VideoPlayerHostMode.DIALOG) {
            return false;
        }
        return config.isShowBack();
    }

    @SuppressLint("DefaultLocale")
    private void updateSpeedChipText() {
        if (btnSpeed == null) {
            return;
        }
        if (speedLabel >= 10f) {
            btnSpeed.setText(String.format("%.0fx", speedLabel));
        } else if (speedLabel == (int) speedLabel) {
            btnSpeed.setText(String.format("%.1fx", speedLabel));
        } else {
            btnSpeed.setText(String.format("%.2fx", speedLabel));
        }
    }

    private void updateClarityChipText() {
        if (btnClarity == null) {
            return;
        }
        String label = config.getCurrentClarityLabel();
        if (label.isEmpty()) {
            btnClarity.setText(R.string.video_clarity_origin);
        } else {
            btnClarity.setText(label);
        }
    }

    @Override
    public int getEnlargeImageRes() {
        return config.getIconConfig().getFullscreenIconRes();
    }

    @Override
    public int getShrinkImageRes() {
        return config.getIconConfig().getFullscreenExitIconRes();
    }

    @Override
    protected void updateStartImage() {
        if (!(mStartButton instanceof AppCompatImageView ivStart)) {
            super.updateStartImage();
            return;
        }
        VideoPlayerIconConfig icons = config.getIconConfig();
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            ivStart.setImageResource(icons.getPauseIconRes());
        } else {
            ivStart.setImageResource(icons.getPlayIconRes());
        }
        if (btnBottomPlay != null) {
            btnBottomPlay.setImageResource(mCurrentState == CURRENT_STATE_PLAYING
                ? icons.getPauseIconRes() : icons.getPlayIconRes());
        }
    }

    @Override
    protected void lockTouchLogic() {
        super.lockTouchLogic();
        applyLockScreenState();
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        refreshToolbarVisibility();
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        refreshToolbarVisibility();
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        refreshToolbarVisibility();
    }

    @Override
    protected void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
        applyLockScreenState();
    }

    @Override
    protected void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        applyLockScreenState();
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer player = super.startWindowFullscreen(context, actionBar, statusBar);
        if (player instanceof ArkVideoPlayerView arkPlayer) {
            arkPlayer.config = this.config;
            arkPlayer.externalExpanded = this.externalExpanded;
            arkPlayer.fullscreenToggleListener = this.fullscreenToggleListener;
            arkPlayer.speedLabel = this.speedLabel;
            arkPlayer.applyConfigInternal();
        }
        return player;
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        if (from instanceof ArkVideoPlayerView source && to instanceof ArkVideoPlayerView target) {
            target.config = source.config;
            target.externalExpanded = source.externalExpanded;
            target.fullscreenToggleListener = source.fullscreenToggleListener;
            target.speedLabel = source.speedLabel;
        }
    }

    @Override
    protected void showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            startPlayLogic();
            return;
        }
        new ConfirmDialog(mContext)
                .setMessage(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi))
                .setPositiveText(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_confirm))
                .setNegativeText(getResources().getString(com.shuyu.gsyvideoplayer.R.string.tips_not_wifi_cancel))
                .setOnPositiveClickListener(dialog -> {
                    dialog.dismiss();
                    startPlayLogic();
                })
                .setOnNegativeClickListener(dialog -> dialog.dismiss())
                .builder()
                .show();

    }

    @Nullable
    public AppCompatImageView getRotateButton() {
        return btnRotate;
    }

    @Nullable
    public AppCompatTextView getSpeedButton() {
        return btnSpeed;
    }

    @Nullable
    public AppCompatTextView getClarityButton() {
        return btnClarity;
    }

    private void applyConfigInternal() {
        ensureConfig();
        refreshToolbarVisibility();
    }

    private void ensureConfig() {
        if (config == null) {
            config = VideoPlayerConfig.embedDefaults();
        }
    }

    /** Dialog 全屏按钮点击回调 */
    public interface OnFullscreenToggleListener {
        void onToggleFullscreen();
    }
}
