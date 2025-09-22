package com.casic.otitan.common.api;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.casic.otitan.common.annotations.interrupte.NeedLogin;
import com.casic.otitan.common.inter.ErrorService;
import com.casic.otitan.common.utils.log.LogUtil;

/**
 * created by fz on 2024/12/18 11:24
 * describe: 登录拦截器，配合注解NeedLogin使用
 */
public class DefaultActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    public final static String TAG = DefaultActivityLifecycleCallback.class.getSimpleName();

    private final ErrorService errorService;

    public DefaultActivityLifecycleCallback(ErrorService errorService) {
        this.errorService = errorService;
    }

    private boolean checkLogin() {
        // 检查登录状态的逻辑
        return !errorService.isLogin();
    }

    private boolean isNeedLogin(Activity activity) {
        // 通过反射或注解处理器获取当前 Activity 是否需要登录
        boolean isAnnotation = activity.getClass().isAnnotationPresent(NeedLogin.class);
        if (!isAnnotation) {
            return false;
        }
        NeedLogin needLogin = activity.getClass().getAnnotation(NeedLogin.class);
        if (needLogin == null) {
            return false;
        }
        return needLogin.enable();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        try {
            if (errorService == null) {
                return;
            }
            //不包含注解或者登录注解未开启
            if (!isNeedLogin(activity)) {

                return;
            }
            //已登录，则跳转登录
            if (!checkLogin()) {
                return;
            }
            Bundle bundle;
            //如果未登录跳转登录并且把当前页的信息传递过去，以便于登录后回传
            if (savedInstanceState == null) {
                bundle = activity.getIntent().getExtras() == null ? new Bundle() : activity.getIntent().getExtras();
            } else {
                bundle = new Bundle();
            }
            bundle.putString(ConstantsHelper.TARGET_ACTIVITY, activity.getClass().getName());
            errorService.toLogin(activity, bundle);
            activity.finish();
        } catch (Exception e) {
            LogUtil.e(TAG, "登录拦截器异常：" + e);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        LogUtil.e(TAG, "onActivitySaveInstanceState：" + bundle);

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}

