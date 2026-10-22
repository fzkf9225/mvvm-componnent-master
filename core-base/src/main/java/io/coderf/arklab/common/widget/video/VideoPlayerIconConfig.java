package io.coderf.arklab.common.widget.video;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.io.Serializable;

import io.coderf.arklab.common.R;

/**
 * 播放器控件图标配置，各功能图标独立，不可共用。
 * <p>
 * 默认值对应 {@code res/drawable/ic_ark_video_*} 系列矢量图。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class VideoPlayerIconConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 顶部返回按钮图标 */
    @DrawableRes
    private int backIconRes = R.drawable.ic_ark_video_back;
    /** 播放状态图标（中央/底部播放按钮） */
    @DrawableRes
    private int playIconRes = R.drawable.ic_ark_video_play;
    /** 暂停状态图标（中央/底部暂停按钮） */
    @DrawableRes
    private int pauseIconRes = R.drawable.ic_ark_video_pause;
    /** 进入全屏图标（非全屏态） */
    @DrawableRes
    private int fullscreenIconRes = R.drawable.ic_ark_video_expand;
    /** 退出全屏图标（已全屏/展开态） */
    @DrawableRes
    private int fullscreenExitIconRes = R.drawable.ic_ark_video_shrink;
    /** 手动旋转屏幕图标 */
    @DrawableRes
    private int rotateIconRes = R.drawable.ic_ark_video_rotate;
    /** 锁屏（锁定手势与控件）图标 */
    @DrawableRes
    private int lockIconRes = R.drawable.ic_ark_video_lock;
    /** 解锁图标 */
    @DrawableRes
    private int unlockIconRes = R.drawable.ic_ark_video_unlock;
    /** 关闭按钮图标（Dialog 等可关闭宿主场景） */
    @DrawableRes
    private int closeIconRes = R.drawable.ic_ark_video_close;

    /** 返回带默认矢量图标的配置实例 */
    @NonNull
    public static VideoPlayerIconConfig defaults() {
        return new VideoPlayerIconConfig();
    }

    /** @return 返回按钮图标 resId */
    public int getBackIconRes() {
        return backIconRes;
    }

    /** @param backIconRes 返回按钮图标 */
    public VideoPlayerIconConfig setBackIconRes(@DrawableRes int backIconRes) {
        this.backIconRes = backIconRes;
        return this;
    }

    /** @return 播放图标 resId */
    public int getPlayIconRes() {
        return playIconRes;
    }

    /** @param playIconRes 播放图标 */
    public VideoPlayerIconConfig setPlayIconRes(@DrawableRes int playIconRes) {
        this.playIconRes = playIconRes;
        return this;
    }

    /** @return 暂停图标 resId */
    public int getPauseIconRes() {
        return pauseIconRes;
    }

    /** @param pauseIconRes 暂停图标 */
    public VideoPlayerIconConfig setPauseIconRes(@DrawableRes int pauseIconRes) {
        this.pauseIconRes = pauseIconRes;
        return this;
    }

    /** @return 进入全屏图标 resId */
    public int getFullscreenIconRes() {
        return fullscreenIconRes;
    }

    /** @param fullscreenIconRes 进入全屏图标 */
    public VideoPlayerIconConfig setFullscreenIconRes(@DrawableRes int fullscreenIconRes) {
        this.fullscreenIconRes = fullscreenIconRes;
        return this;
    }

    /** @return 退出全屏图标 resId */
    public int getFullscreenExitIconRes() {
        return fullscreenExitIconRes;
    }

    /** @param fullscreenExitIconRes 退出全屏图标 */
    public VideoPlayerIconConfig setFullscreenExitIconRes(@DrawableRes int fullscreenExitIconRes) {
        this.fullscreenExitIconRes = fullscreenExitIconRes;
        return this;
    }

    /** @return 旋转图标 resId */
    public int getRotateIconRes() {
        return rotateIconRes;
    }

    /** @param rotateIconRes 旋转图标 */
    public VideoPlayerIconConfig setRotateIconRes(@DrawableRes int rotateIconRes) {
        this.rotateIconRes = rotateIconRes;
        return this;
    }

    /** @return 锁屏图标 resId */
    public int getLockIconRes() {
        return lockIconRes;
    }

    /** @param lockIconRes 锁屏图标 */
    public VideoPlayerIconConfig setLockIconRes(@DrawableRes int lockIconRes) {
        this.lockIconRes = lockIconRes;
        return this;
    }

    /** @return 解锁图标 resId */
    public int getUnlockIconRes() {
        return unlockIconRes;
    }

    /** @param unlockIconRes 解锁图标 */
    public VideoPlayerIconConfig setUnlockIconRes(@DrawableRes int unlockIconRes) {
        this.unlockIconRes = unlockIconRes;
        return this;
    }

    /** @return 关闭图标 resId（Dialog 等场景） */
    public int getCloseIconRes() {
        return closeIconRes;
    }

    /** @param closeIconRes 关闭图标 */
    public VideoPlayerIconConfig setCloseIconRes(@DrawableRes int closeIconRes) {
        this.closeIconRes = closeIconRes;
        return this;
    }
}
