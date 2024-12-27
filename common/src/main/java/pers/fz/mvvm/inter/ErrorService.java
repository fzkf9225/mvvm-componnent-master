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

    void toLogin(Context mContext, Bundle bundle, ActivityResultLauncher<Intent> activityResultLauncher);

    void toLogin(Context context);

    void toLogin(Context context,Bundle bundle);
    /**
     * 是否有操作权限
     * @return 是否有权限
     */
    boolean hasPermission(String errorCode);

    void toNoPermission(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher);

    void toNoPermission(Context context);
    /**
     * 崩溃日志
     * @param errorInfo 上传登录日志
     */
    void uploadErrorInfo(String errorInfo);

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

    Map<String,String> initHeaderMap();

}
