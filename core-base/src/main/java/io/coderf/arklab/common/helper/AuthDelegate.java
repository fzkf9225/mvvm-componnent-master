package io.coderf.arklab.common.helper;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 登录 / 无权限跳转委托：包装 {@link AuthManager}。
 * <p>
 * 供 BaseActivity / BaseFragment 内部使用；对外仍可通过 {@link #getAuthManager()} 访问原实例，
 * 回调接口仍为 {@link AuthManager.AuthCallback}，行为与改造前一致。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
public class AuthDelegate {

    @NonNull
    private final AuthManager authManager;

    public AuthDelegate(@NonNull ComponentActivity activity, boolean unifyHandling) {
        this.authManager = new AuthManager(activity, unifyHandling);
    }

    public AuthDelegate(@NonNull Fragment fragment, boolean unifyHandling) {
        this.authManager = new AuthManager(fragment, unifyHandling);
    }

    @NonNull
    public static AuthDelegate forActivity(@NonNull ComponentActivity activity, boolean unifyHandling) {
        return new AuthDelegate(activity, unifyHandling);
    }

    @NonNull
    public static AuthDelegate forFragment(@NonNull Fragment fragment, boolean unifyHandling) {
        return new AuthDelegate(fragment, unifyHandling);
    }

    @NonNull
    public AuthManager getAuthManager() {
        return authManager;
    }

    public void setLoginCallback(@Nullable AuthManager.AuthCallback callback) {
        authManager.setLoginCallback(callback);
    }

    public void setPermissionCallback(@Nullable AuthManager.AuthCallback callback) {
        authManager.setPermissionCallback(callback);
    }

    @NonNull
    public ActivityResultLauncher<Intent> getLoginLauncher() {
        return authManager.getLoginLauncher();
    }

    @NonNull
    public ActivityResultLauncher<Intent> getPermissionLauncher() {
        return authManager.getPermissionLauncher();
    }

    public void launchLogin(@NonNull Intent intent, @Nullable AuthManager.AuthCallback callback) {
        authManager.launchLogin(intent, callback);
    }

    public boolean isUnifyHandling() {
        return authManager.isUnifyHandling();
    }

    /** 释放 launcher 回调引用（页面销毁时调用）。 */
    public void unregister() {
        authManager.unregister();
    }
}
