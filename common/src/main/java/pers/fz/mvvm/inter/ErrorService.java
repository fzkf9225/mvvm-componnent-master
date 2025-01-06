package pers.fz.mvvm.inter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;

import java.util.Map;

/**
 * created by fz on 2023/4/24 13:04
 * describe:
 **/
public interface ErrorService {

    /**
     * 是否登录
     * @return 是否已登录
     */
    boolean isLogin();
    /**
     * 是否登录超时或者登录无效等情况，他包含是否登录
     * @return 是否为登录过期
     */
    boolean isLoginPast(String errorCode);

    /**
     * activity跳转登录
     * @param mContext fromActivity
     * @param activityResultLauncher launcher
     */
    void toLogin(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher);

    /**
     * activity跳转登录
     * @param mContext fromActivity
     * @param bundle 传递参数
     * @param activityResultLauncher launcher
     */
    void toLogin(Context mContext, Bundle bundle, ActivityResultLauncher<Intent> activityResultLauncher);
    /**
     * activity跳转登录
     * @param context 上下文
     */
    void toLogin(Context context);

    void toLogin(Context context,Bundle bundle);
    /**
     * 是否有操作权限
     * @return 是否有权限
     */
    boolean hasPermission(String errorCode);

    /**
     * 跳转到无权限页面目标页，大部分是跳转到登录页
     * @param mContext 上下文
     * @param activityResultLauncher launcher
     */
    void toNoPermission(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher);
    /**
     * 跳转到无权限页面目标页，大部分是跳转到登录页
     * @param context 上下文
     */
    void toNoPermission(Context context);
    /**
     * 崩溃日志
     * @param errorInfo 上传登录日志
     */
    void uploadErrorInfo(String errorInfo);

    /**
     * app主页Activity，因为登录页在其他模块，这个主要是其他子模块使用
     * @return app主页Activity
     */
    Class<?> getMainActivity();
    /**
     * 获取用户token
     * @return token
     */
    String getToken();

    /**
     * 获取用户RefreshToken
     * @return 刷新token
     */
    String getRefreshToken();

    /**
     * 默认的请求头
     * @return Map请求头
     */
    Map<String,String> initHeaderMap();

}
