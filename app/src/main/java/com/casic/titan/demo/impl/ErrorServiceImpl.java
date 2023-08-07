package com.casic.titan.demo.impl;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;

import com.casic.titan.demo.activity.MainActivity;
import com.casic.titan.demo.api.ConstantsHelper;
import com.casic.titan.userapi.router.UserRouterService;
import com.casic.titan.usercomponent.api.UserAccountHelper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import pers.fz.mvvm.inter.ErrorService;

/**
 * created by fz on 2023/04/25 13:04
 */
public  class ErrorServiceImpl implements ErrorService {
    @Inject
    UserRouterService userRouterService;
    @Inject
    public ErrorServiceImpl() {
    }

    @Override
    public boolean isLogin(String errorCode) {
        return !UserAccountHelper.isLoginPast(errorCode);
    }

    @Override
    public void toLogin(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher) {
        userRouterService.toLogin(mContext,activityResultLauncher);
    }


    @Override
    public void toLogin(Context context) {
        userRouterService.toLogin(context);
    }

    @Override
    public boolean hasPermission(String errorCode) {
        return true;
    }

    @Override
    public void toNoPermission(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher) {

    }

    @Override
    public void toNoPermission(Context context) {

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
    public Map<String, String> initHeaderMap() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", ConstantsHelper.AUTHORIZATION);
        headerMap.put("Tenant-Id", ConstantsHelper.TENANT_ID);
        if (!TextUtils.isEmpty(UserAccountHelper.getToken())) {
            headerMap.put("Blade-Auth", UserAccountHelper.getToken());
        }
        return headerMap;
    }

}
