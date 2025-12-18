package com.casic.otitan.demo.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;

import com.casic.otitan.demo.activity.MainActivity;
import com.casic.otitan.userapi.UserService;
import com.casic.otitan.userapi.router.UserRouterService;
import com.casic.otitan.usercomponent.api.UserAccountHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.casic.otitan.common.inter.ErrorService;

/**
 * created by fz on 2023/04/25 13:04
 */
public class ErrorServiceImpl implements ErrorService {
    @Inject
    UserRouterService userRouterService;
    @Inject
    UserService userService;

    @Inject
    public ErrorServiceImpl() {
    }

    @Override
    public boolean isLogin() {
        return userService.isLogin();
    }

    @Override
    public boolean isLoginPast(String errorCode) {
        return UserAccountHelper.isLoginPast(errorCode);
    }

    @Override
    public void toLogin(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher) {
        userRouterService.toLogin(mContext, activityResultLauncher);
    }

    @Override
    public void toLogin(Context mContext, Bundle bundle, ActivityResultLauncher<Intent> activityResultLauncher) {
        if (bundle == null || bundle.isEmpty()) {
            userRouterService.toLogin(mContext, activityResultLauncher);
            return;
        }
        userRouterService.toLogin(mContext, bundle, activityResultLauncher);
    }

    @Override
    public void toLogin(Context context) {
        userRouterService.toLogin(context);
    }

    @Override
    public void toLogin(Context context, Bundle bundle) {
        if (bundle == null || bundle.isEmpty()) {
            userRouterService.toLogin(context);
            return;
        }
        userRouterService.toLogin(context, bundle);
    }

    @Override
    public boolean hasPermission(String errorCode) {
        return !UserAccountHelper.isNoPermission(errorCode);
    }

    @Override
    public void toNoPermission(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher) {
        userRouterService.toLogin(mContext, activityResultLauncher);
    }

    @Override
    public void toNoPermission(Context mContext) {
        userRouterService.toLogin(mContext);
    }

    @Override
    public void uploadErrorInfo(String errorInfo) {

    }

    @Override
    public Class<?> getMainActivity() {
        return MainActivity.class;
    }

    @Override
    public String getToken() {
        return UserAccountHelper.getToken();
    }

    @Override
    public String getRefreshToken() {
        return UserAccountHelper.getRefreshToken();
    }

    @Override
    public Map<String, String> defaultRequestHeader(String system) {
        Map<String, String> headerMap = new HashMap<>();
        if (!TextUtils.isEmpty(userService.getToken())) {
            headerMap.put("authorization", userService.getToken());
        }
        return headerMap;
    }

}
