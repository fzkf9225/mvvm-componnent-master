package com.casic.otitan.usercomponent.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;

import com.casic.otitan.userapi.router.UserRouterService;
import com.casic.otitan.usercomponent.activity.LoginActivity;

import javax.inject.Inject;

/**
 * Created by fz on 2023/5/17 14:23
 * describe :
 */
public class UserRouterServiceImpl implements UserRouterService {

    @Inject
    public UserRouterServiceImpl() {
    }


    @Override
    public void toLogin(Context mContext, ActivityResultLauncher<Intent> activityResultLauncher) {
        activityResultLauncher.launch(new Intent(mContext, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    @Override
    public void toLogin(Context mContext, Bundle bundle, ActivityResultLauncher<Intent> activityResultLauncher) {
        if (bundle == null || bundle.isEmpty()) {
            toLogin(mContext, activityResultLauncher);
            return;
        }
        activityResultLauncher.launch(new Intent(mContext, LoginActivity.class).putExtras(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    @Override
    public void toLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    @Override
    public void toLogin(Context context, Bundle bundle) {
        if (bundle == null || bundle.isEmpty()) {
            toLogin(context);
            return;
        }
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
