package com.casic.otitan.common.helper;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * created by fz on 2025/6/4 9:28
 * describe:
 */
public class AuthManager {
    public interface AuthCallback {
        void onAuthSuccess(@Nullable Bundle data);
        void onAuthFail(int resultCode, @Nullable Bundle data);
    }

    /**
     * 登录授权启动器
     */
    private final ActivityResultLauncher<Intent> loginLauncher;

    /**
     * 授权回调
     */
    private AuthCallback loginCallback;

    /**
     * 是否统一处理登录和权限,true：相同情况处理，false：不同处理
     */
    private final boolean unifyHandling;

    /**
     * 无权限启动器
     */
    private ActivityResultLauncher<Intent> permissionLauncher;

    /**
     * 无权限授权回调
     */
    private AuthCallback permissionCallback;

    /**
     * 跳转登录无权限授权等启动器，sameAsLogin默认为true
     * @param activity context对象
     */
    public AuthManager(ComponentActivity activity) {
        this(activity, true);
    }

    /**
     * 跳转登录无权限授权等启动器
     * @param activity context对象
     * @param unifyHandling 无权限的处理是否和登录处理相同，true：相同。false：无权限情况下处理自己的逻辑
     */
    public AuthManager(ComponentActivity activity, boolean unifyHandling) {
        this.unifyHandling = unifyHandling;
        this.loginLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleActivityResult
        );
        if (this.unifyHandling) {
            return;
        }
        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handlePermissionResult
        );
    }

    /**
     * 跳转登录无权限授权等启动器，sameAsLogin默认为true
     * @param fragment context对象
     */
    public AuthManager(Fragment fragment) {
        this(fragment, true);
    }

    /**
     * 跳转登录无权限授权等启动器
     * @param fragment context对象
     * @param unifyHandling 无权限的处理是否和登录处理相同，true：相同。false：无权限情况下处理自己的逻辑
     */
    public AuthManager(Fragment fragment, boolean unifyHandling) {
        this.unifyHandling = unifyHandling;
        this.loginLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleActivityResult
        );
        if (this.unifyHandling) {
            return;
        }
        this.permissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handlePermissionResult
        );
    }

    /**
     * 处理登录授权结果
     * @param result 登录授权结果
     */
    public void handleActivityResult(@NonNull androidx.activity.result.ActivityResult result) {
        if (loginCallback == null) {
            return;
        }

        if (result.getResultCode() == ComponentActivity.RESULT_OK) {
            loginCallback.onAuthSuccess(result.getData() != null ?
                    result.getData().getExtras() : null);
        } else {
            loginCallback.onAuthFail(result.getResultCode(),
                    result.getData() != null ? result.getData().getExtras() : null);
        }
    }

    /**
     * 处理无权限授权结果
     * @param result 无权限授权结果
     */
    public void handlePermissionResult(@NonNull androidx.activity.result.ActivityResult result) {
        if (permissionCallback == null) {
            return;
        }

        if (result.getResultCode() == ComponentActivity.RESULT_OK) {
            permissionCallback.onAuthSuccess(result.getData() != null ?
                    result.getData().getExtras() : null);
        } else {
            permissionCallback.onAuthFail(result.getResultCode(),
                    result.getData() != null ? result.getData().getExtras() : null);
        }
    }


    /**
     * 获取登录授权启动器
     * @return 登录授权启动器
     */
    public ActivityResultLauncher<Intent> getLoginLauncher() {
        return loginLauncher;
    }

    /**
     * 设置登录授权回调
     * @param loginCallback 登录授权回调
     */
    public void setLoginCallback(AuthCallback loginCallback) {
        this.loginCallback = loginCallback;
    }

    /**
     * 启动登录
     * @param intent 自定义登录授权intent
     * @param loginCallback 登录授权回调
     */
    public void launchLogin(Intent intent, AuthCallback loginCallback) {
        this.loginCallback = loginCallback;
        loginLauncher.launch(intent);
    }

    /**
     * 是否统一处理登录和权限回调
     * @return true为统一处理，false为不同处理
     */
    public boolean isUnifyHandling() {
        return unifyHandling;
    }

    /**
     * 获取无权限授权启动器
     * @return 无权限授权启动器
     */
    public ActivityResultLauncher<Intent> getPermissionLauncher() {
        if (unifyHandling) {
            return loginLauncher;
        }
        return permissionLauncher;
    }

    /**
     * 设置无权限授权回调，只有在unifyHandling为false的时候才有效
     * @param permissionCallback 无权限授权回调
     */
    public void setPermissionCallback(AuthCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }

    /**
     * 启动无权限授权
     * @param intent 自定义无权限授权intent
     * @param permissionCallback 无权限授权回调
     */
    public void launchPermission(Intent intent, AuthCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
        permissionLauncher.launch(intent);
    }

    /**
     * 注销
     */
    public void unregister() {
        if (loginLauncher != null) {
            loginLauncher.unregister();
        }
        if (permissionLauncher != null) {
            permissionLauncher.unregister();
        }
    }
}