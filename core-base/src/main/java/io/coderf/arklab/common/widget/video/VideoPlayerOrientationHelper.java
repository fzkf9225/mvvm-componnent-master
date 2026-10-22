package io.coderf.arklab.common.widget.video;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

/**
 * 屏幕旋转辅助：进入全屏保存方向、退出恢复；手动旋转与重力感应协同。
 * <p>
 * 由 {@link VideoPlayerController} 内部持有，业务侧一般无需直接使用。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class VideoPlayerOrientationHelper {

    /** 手动旋转后延迟恢复 FULL_SENSOR，避免与 GSY OrientationUtils 冲突 */
    private static final long RESTORE_GRAVITY_DELAY_MS = 350L;

    /** 主线程 Handler，用于延迟恢复重力感应 */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 进入全屏前宿主 Activity 的 requestedOrientation，退出时用于还原 */
    private int savedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    /** 进入全屏时是否曾因竖屏而强制切到横屏，退出时据此决定是否还原竖屏 */
    private boolean rotatedToLandscapeForFullscreen;
    /** 是否启用重力感应自动横竖屏（对应 config.gravityRotationEnabled） */
    private boolean gravityEnabled;
    /** 当前是否处于全屏/Dialog 展开态 */
    private boolean expanded;

    /** GSY OrientationUtils，可为空；为空时手动旋转直接改 Activity 方向 */
    @Nullable
    private OrientationUtils orientationUtils;

    /**
     * 绑定 GSY 提供的 OrientationUtils（可为 null，则手动旋转走 Activity.setRequestedOrientation）。
     *
     * @param orientationUtils GSY 方向工具，可在 release 时置空
     */
    public void bindOrientationUtils(@Nullable OrientationUtils orientationUtils) {
        this.orientationUtils = orientationUtils;
    }

    /**
     * 是否启用重力感应自动横竖屏。
     *
     * @param gravityEnabled true 时在展开态开启 FULL_SENSOR
     */
    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }

    /**
     * 标记当前是否处于全屏/展开态（影响重力恢复逻辑）。
     *
     * @param expanded true 表示全屏或 Dialog 展开
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * 进入全屏/展开：保存当前方向，必要时强制横屏并开启重力。
     *
     * @param activity 宿主 Activity
     */
    public void onEnterExpanded(@NonNull Activity activity) {
        expanded = true;
        saveWindowOrientation(activity);
        if (shouldRotateToLandscape(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            rotatedToLandscapeForFullscreen = true;
        }
        applyGravityRotation(activity, true);
    }

    /**
     * 退出全屏/展开：关闭重力并恢复进入前的屏幕方向。
     *
     * @param activity 宿主 Activity
     */
    public void onExitExpanded(@NonNull Activity activity) {
        expanded = false;
        applyGravityRotation(activity, false);
        restoreOrientation(activity);
    }

    /**
     * 手动旋转：竖屏 ↔ 横屏，延迟后恢复重力感应，避免与 GSY OrientationUtils 抢状态。
     *
     * @param activity 宿主 Activity
     */
    public void toggleManualRotation(@NonNull Activity activity) {
        if (orientationUtils != null) {
            orientationUtils.resolveByClick();
            scheduleRestoreGravity(activity);
            return;
        }
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        scheduleRestoreGravity(activity);
    }

    /**
     * 屏幕配置变化时，在展开且开启重力的情况下延迟恢复 FULL_SENSOR。
     *
     * @param activity  宿主
     * @param newConfig 新配置
     */
    public void onConfigurationChanged(@NonNull Activity activity, @NonNull Configuration newConfig) {
        if (expanded && gravityEnabled) {
            scheduleRestoreGravity(activity);
        }
    }

    /** 宿主暂停：暂停 OrientationUtils 内部监听 */
    public void onHostPause() {
        if (orientationUtils != null) {
            orientationUtils.setIsPause(true);
        }
    }

    /** 宿主恢复：继续监听，并在展开态重新启用重力 */
    public void onHostResume() {
        if (orientationUtils != null) {
            orientationUtils.setIsPause(false);
        }
        if (expanded && gravityEnabled && orientationUtils != null) {
            orientationUtils.setEnable(true);
        }
    }

    /** 释放 Handler 回调与 OrientationUtils 监听 */
    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
            orientationUtils = null;
        }
    }

    private void scheduleRestoreGravity(@NonNull Activity activity) {
        if (!gravityEnabled || !expanded) {
            return;
        }
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler.postDelayed(() -> applyGravityRotation(activity, true), RESTORE_GRAVITY_DELAY_MS);
    }

    private void saveWindowOrientation(@NonNull Activity activity) {
        savedOrientation = activity.getRequestedOrientation();
    }

    private boolean shouldRotateToLandscape(@NonNull Activity activity) {
        return activity.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    private void restoreOrientation(@NonNull Activity activity) {
        if (rotatedToLandscapeForFullscreen) {
            if (isPortraitOrientation(savedOrientation)) {
                activity.setRequestedOrientation(savedOrientation);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            rotatedToLandscapeForFullscreen = false;
        } else if (savedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.setRequestedOrientation(savedOrientation);
        }
    }

    private void applyGravityRotation(@NonNull Activity activity, boolean expandedNow) {
        if (!gravityEnabled) {
            if (orientationUtils != null) {
                orientationUtils.setEnable(false);
            }
            return;
        }
        if (expandedNow) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            if (orientationUtils != null) {
                orientationUtils.setEnable(true);
            }
        } else if (orientationUtils != null) {
            orientationUtils.setEnable(false);
        }
    }

    private static boolean isPortraitOrientation(int orientation) {
        return orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
    }
}
