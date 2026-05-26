package io.coderf.arklab.common.utils.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * WebView 场景下的运行时权限分组与申请。
 * <p>权限<b>检查</b>仅使用 {@link ContextCompat} / {@link PermissionsChecker}，避免在 RESUMED 阶段
 * 误 new {@link PermissionManager} 触发 ActivityResult 注册崩溃。</p>
 */
public final class WebViewPermissionHelper {

    private static final String[] LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private WebViewPermissionHelper() {
    }

    @NonNull
    public static String[] locationPermissions() {
        return LOCATION;
    }

    @NonNull
    public static String[] cameraPermissions(boolean includeAudioForVideo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return includeAudioForVideo
                    ? new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}
                    : new String[]{Manifest.permission.CAMERA};
        }
        return includeAudioForVideo
                ? new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
                : new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    /**
     * 系统文件选择器（非 Photo Picker）所需的读存储权限。
     */
    @NonNull
    public static String[] readStorageForFileChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    private static boolean isGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 是否已具备定位权限（精确或粗略任一即可）。
     */
    public static boolean hasLocationPermission(@NonNull Context context) {
        return isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                || isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * 是否已具备读媒体/存储权限（兼容 Android 14 部分相册授权）。
     */
    public static boolean hasReadStorageForFileChooser(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VIDEO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                    || isGranted(context, Manifest.permission.READ_MEDIA_VIDEO);
        }
        return isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * 申请权限；已授权则直接执行 {@code onGranted}。
     * <p>{@code permissionManager} 须在 Activity {@code onCreate} 阶段创建并复用。</p>
     */
    public static void request(
            @NonNull ComponentActivity activity,
            @NonNull PermissionManager permissionManager,
            @NonNull String[] permissions,
            @NonNull PermissionManager.GrantMode grantMode,
            @NonNull Runnable onGranted,
            @NonNull Runnable onDenied
    ) {
        permissionManager.setGrantMode(grantMode);
        permissionManager.setNotifyDeniedToast(false);
        permissionManager.setOnGrantedCallback(map -> onGranted.run());
        permissionManager.setOnDeniedCallback(map -> onDenied.run());
        if (isGranted(activity, permissions, grantMode)) {
            onGranted.run();
            return;
        }
        permissionManager.request(permissions);
    }

    public static boolean isGranted(
            @NonNull Context context,
            @NonNull String[] permissions,
            @NonNull PermissionManager.GrantMode grantMode
    ) {
        if (grantMode == PermissionManager.GrantMode.ANY_GRANTED) {
            for (String permission : permissions) {
                if (isGranted(context, permission)) {
                    return true;
                }
            }
            return false;
        }
        return !PermissionsChecker.getInstance().lacksPermissions(context, permissions);
    }
}
