package com.casic.titan.demo.api;

import com.casic.titan.demo.BuildConfig;
import com.casic.titan.googlegps.common.AppSettings;

import dagger.hilt.android.HiltAndroidApp;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.api.Config;

/**
 * Created by fz on 2023/5/7 15:03
 * describe:
 */
@HiltAndroidApp
public class ApplicationHelper extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Config.getInstance().init(this);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
        AppSettings.getInstance().onCreate(this);
    }

}
