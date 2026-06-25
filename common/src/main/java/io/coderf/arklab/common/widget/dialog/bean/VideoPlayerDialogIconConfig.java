package io.coderf.arklab.common.widget.dialog.bean;

import androidx.annotation.DrawableRes;

import io.coderf.arklab.common.widget.video.VideoPlayerIconConfig;

/**
 * Dialog / Activity 视频播放器控件图标配置（兼容旧 API）。
 *
 * @see VideoPlayerIconConfig
 */
public class VideoPlayerDialogIconConfig {

    @DrawableRes
    private int backIconRes;
    @DrawableRes
    private int enlargeIconRes;
    @DrawableRes
    private int shrinkIconRes;
    @DrawableRes
    private int closeIconRes;
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
