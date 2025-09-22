package com.casic.otitan.common.api;

import android.app.Application;

/**
 * Created by fz on 2017/6/20.
 * Application
 */

public abstract class BaseApplication extends Application {
    private static BaseApplication applicationHelper;
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        applicationHelper = this;
    }

    public static BaseApplication getInstance() {
        return applicationHelper;
    }

}
