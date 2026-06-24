package io.coderf.arklab.common.widget.dialog.bean;

import androidx.annotation.DrawableRes;

/**
 * Dialog / Activity 视频播放器控件图标配置。
 * <p>
 * GSYVideoPlayer 基于 {@link com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer}，
 * 支持替换返回键、全屏进入/退出图标；中央播放按钮为 ENPlayView 动画控件，暂不支持替换为静态图。
 * </p>
 */
public class VideoPlayerDialogIconConfig {

    /** 返回按钮图标，对应播放器 layout 中的 {@code @id/back} */
    @DrawableRes
    private int backIconRes;

    /** 进入全屏图标，对应 {@code @id/fullscreen} 默认态 */
    @DrawableRes
    private int enlargeIconRes;

    /** 退出全屏图标，全屏展开后显示 */
    @DrawableRes
    private int shrinkIconRes;

    /** Dialog 右上角关闭按钮图标 */
    @DrawableRes
    private int closeIconRes;

    /** Activity 旋转屏幕按钮图标（复用 GSY 控制栏全屏按钮位） */
    @DrawableRes
    private int rotateIconRes;

    public int getBackIconRes() {
        return backIconRes;
    }

    public VideoPlayerDialogIconConfig setBackIconRes(@DrawableRes int backIconRes) {
        this.backIconRes = backIconRes;
        return this;
    }

    public int getEnlargeIconRes() {
        return enlargeIconRes;
    }

    public VideoPlayerDialogIconConfig setEnlargeIconRes(@DrawableRes int enlargeIconRes) {
        this.enlargeIconRes = enlargeIconRes;
        return this;
    }

    public int getShrinkIconRes() {
        return shrinkIconRes;
    }

    public VideoPlayerDialogIconConfig setShrinkIconRes(@DrawableRes int shrinkIconRes) {
        this.shrinkIconRes = shrinkIconRes;
        return this;
    }

    public int getCloseIconRes() {
        return closeIconRes;
    }

    public VideoPlayerDialogIconConfig setCloseIconRes(@DrawableRes int closeIconRes) {
        this.closeIconRes = closeIconRes;
        return this;
    }

    public int getRotateIconRes() {
        return rotateIconRes;
    }

    public VideoPlayerDialogIconConfig setRotateIconRes(@DrawableRes int rotateIconRes) {
        this.rotateIconRes = rotateIconRes;
        return this;
    }
}
