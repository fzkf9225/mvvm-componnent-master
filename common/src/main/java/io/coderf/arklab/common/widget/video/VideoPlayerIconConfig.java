package io.coderf.arklab.common.widget.video;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.coderf.arklab.common.R;

/**
 * 播放器控件图标配置，各功能图标独立，不可共用。
 */
public class VideoPlayerIconConfig {

    @DrawableRes
    private int backIconRes = R.drawable.ic_ark_video_back;
    @DrawableRes
    private int playIconRes = R.drawable.ic_ark_video_play;
    @DrawableRes
    private int pauseIconRes = R.drawable.ic_ark_video_pause;
    @DrawableRes
    private int fullscreenIconRes = R.drawable.ic_ark_video_expand;
    @DrawableRes
    private int fullscreenExitIconRes = R.drawable.ic_ark_video_shrink;
    @DrawableRes
    private int rotateIconRes = R.drawable.ic_ark_video_rotate;
    @DrawableRes
    private int lockIconRes = R.drawable.ic_ark_video_lock;
    @DrawableRes
    private int unlockIconRes = R.drawable.ic_ark_video_unlock;
    @DrawableRes
    private int closeIconRes = R.drawable.ic_ark_video_close;

    @NonNull
    public static VideoPlayerIconConfig defaults() {
        return new VideoPlayerIconConfig();
    }

    public int getBackIconRes() {
        return backIconRes;
    }

    public VideoPlayerIconConfig setBackIconRes(@DrawableRes int backIconRes) {
        this.backIconRes = backIconRes;
        return this;
    }

    public int getPlayIconRes() {
        return playIconRes;
    }

    public VideoPlayerIconConfig setPlayIconRes(@DrawableRes int playIconRes) {
        this.playIconRes = playIconRes;
        return this;
    }

    public int getPauseIconRes() {
        return pauseIconRes;
    }

    public VideoPlayerIconConfig setPauseIconRes(@DrawableRes int pauseIconRes) {
        this.pauseIconRes = pauseIconRes;
        return this;
    }

    public int getFullscreenIconRes() {
        return fullscreenIconRes;
    }

    public VideoPlayerIconConfig setFullscreenIconRes(@DrawableRes int fullscreenIconRes) {
        this.fullscreenIconRes = fullscreenIconRes;
        return this;
    }

    public int getFullscreenExitIconRes() {
        return fullscreenExitIconRes;
    }

    public VideoPlayerIconConfig setFullscreenExitIconRes(@DrawableRes int fullscreenExitIconRes) {
        this.fullscreenExitIconRes = fullscreenExitIconRes;
        return this;
    }

    public int getRotateIconRes() {
        return rotateIconRes;
    }

    public VideoPlayerIconConfig setRotateIconRes(@DrawableRes int rotateIconRes) {
        this.rotateIconRes = rotateIconRes;
        return this;
    }

    public int getLockIconRes() {
        return lockIconRes;
    }

    public VideoPlayerIconConfig setLockIconRes(@DrawableRes int lockIconRes) {
        this.lockIconRes = lockIconRes;
        return this;
    }

    public int getUnlockIconRes() {
        return unlockIconRes;
    }

    public VideoPlayerIconConfig setUnlockIconRes(@DrawableRes int unlockIconRes) {
        this.unlockIconRes = unlockIconRes;
        return this;
    }

    public int getCloseIconRes() {
        return closeIconRes;
    }

    public VideoPlayerIconConfig setCloseIconRes(@DrawableRes int closeIconRes) {
        this.closeIconRes = closeIconRes;
        return this;
    }
}
