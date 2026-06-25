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
 */
public class VideoPlayerOrientationHelper {

    private static final long RESTORE_GRAVITY_DELAY_MS = 350L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int savedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private boolean rotatedToLandscapeForFullscreen;
    private boolean gravityEnabled;
    private boolean expanded;

    @Nullable
    private OrientationUtils orientationUtils;

    public void bindOrientationUtils(@Nullable OrientationUtils orientationUtils) {
        this.orientationUtils = orientationUtils;
    }

    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void onEnterExpanded(@NonNull Activity activity) {
        expanded = true;
        saveWindowOrientation(activity);
        if (shouldRotateToLandscape(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            rotatedToLandscapeForFullscreen = true;
        }
        applyGravityRotation(activity, true);
    }

    public void onExitExpanded(@NonNull Activity activity) {
        expanded = false;
        applyGravityRotation(activity, false);
        restoreOrientation(activity);
    }

    /** 手动旋转：竖屏 ↔ 横屏，之后恢复重力感应 */
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

    public void onConfigurationChanged(@NonNull Activity activity, @NonNull Configuration newConfig) {
        if (expanded && gravityEnabled) {
            scheduleRestoreGravity(activity);
        }
    }

    public void onHostPause() {
        if (orientationUtils != null) {
            orientationUtils.setIsPause(true);
        }
    }

    public void onHostResume() {
        if (orientationUtils != null) {
            orientationUtils.setIsPause(false);
        }
        if (expanded && gravityEnabled && orientationUtils != null) {
            orientationUtils.setEnable(true);
        }
    }

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
