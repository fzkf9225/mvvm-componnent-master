package io.coderf.arklab.common.widget.dialog.bean;

import androidx.annotation.DrawableRes;

import io.coderf.arklab.common.widget.video.VideoPlayerIconConfig;

/**
 * Dialog / Activity 视频播放器控件图标配置（兼容旧 API）。
 * <p>
 * 新代码请优先使用 {@link VideoPlayerIconConfig}；本类保留用于
 * {@link io.coderf.arklab.common.widget.dialog.VideoPlayerDialog#setIconConfig(VideoPlayerDialogIconConfig)} 等旧调用链，
 * 内部会通过 {@link io.coderf.arklab.common.widget.video.VideoPlayerViewHelper} 合并到 {@link VideoPlayerIconConfig}。
 * </p>
 *
 * @see VideoPlayerIconConfig
 */
public class VideoPlayerDialogIconConfig {

    /** 返回 / 退出按钮图标，0 表示使用默认 */
    @DrawableRes
    private int backIconRes;
    /** 进入全屏图标，0 表示使用默认 */
    @DrawableRes
    private int enlargeIconRes;
    /** 退出全屏图标，0 表示使用默认 */
    @DrawableRes
    private int shrinkIconRes;
    /** Dialog 右上角关闭按钮图标，0 表示使用默认 */
    @DrawableRes
    private int closeIconRes;
    /** 手动旋转屏幕图标，0 表示使用默认 */
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
