package io.coderf.arklab.userapi.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;

/**
 * UserRouterService 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/17 14:20
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
