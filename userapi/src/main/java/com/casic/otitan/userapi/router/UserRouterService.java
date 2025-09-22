package com.casic.otitan.userapi.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;

/**
 * Created by fz on 2023/5/17 14:20
 * describe :
 */
public interface UserRouterService {
    /**
     * activity跳转登录
     * @param mContext fromActivity
     * @param activityResultLauncher launcher
     */
    void toLogin(Context mContext,ActivityResultLauncher<Intent> activityResultLauncher);

    void toLogin(Context mContext, Bundle bundle, ActivityResultLauncher<Intent> activityResultLauncher);

    void toLogin(Context context);

    void toLogin(Context context, Bundle bundle);

}
