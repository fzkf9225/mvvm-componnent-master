package com.casic.titan.demo.api;

import com.casic.titan.mqttcomponent.MQTTHelper;
import com.casic.titan.wscomponent.WebSocketHelper;
import com.tencent.mmkv.MMKV;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import pers.fz.mvvm.BuildConfig;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.LogcatHelper;

/**
 * Created by fz on 2023/5/7 15:03
 * describe:
 */
@HiltAndroidApp
public class ApplicationHelper extends BaseApplication {
    @Inject
    ErrorService errorService;

    @Override
    public void onCreate() {
        super.onCreate();
        Config.getInstance().init(this);
        Config.getInstance().enableDebug(true);
        new ApiRetrofit.Builder(this)
                .addDefaultHeader()
                .setErrorService(errorService)
                .builder();
        WebSocketHelper.init(this);
        MQTTHelper.init(this);
    }

}
