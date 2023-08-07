package com.casic.titan.usercomponent.impl;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.casic.titan.userapi.router.UserRouterService;
import com.casic.titan.usercomponent.activity.LoginActivity;

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
    public void toLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
