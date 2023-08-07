package com.casic.titan.usercomponent.api;

import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.BaseApplication;

/**
 * Created by fz on 2021/2/7 15:03
 * describe:
 */
public class ApplicationHelper extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //更新拦截器和Retrofit相关配置
    }

}
