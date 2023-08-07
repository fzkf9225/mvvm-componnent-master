package pers.fz.mvvm.inter;


import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import java.util.Map;

/**
 * created by fz on 2023/4/24 13:04
 * describe:
 **/
public interface ErrorService {
    /**
     * 是否登录
     * @return
     */
    boolean isLogin(String errorCode);

    /**
     * activity跳转登录
     * @param mContext fromActivity
     * @param activityResultLauncher launcher
     */
    void toLogin(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher);

    void toLogin(Context context);
    /**
     * 是否有操作权限
     * @return
     */
    boolean hasPermission(String errorCode);

    void toNoPermission(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher);

    void toNoPermission(Context context);
    /**
     * 崩溃日志
     * @param errorInfo
     */
    void uploadErrorInfo(String errorInfo);

    Class<?> getMainActivity();
    /**
     * 获取用户token
     * @return
     */
    String getToken();

    /**
     * 获取用户RefreshToken
     * @return
     */
    String getRefreshToken();

    Map<String,String> initHeaderMap();

}
