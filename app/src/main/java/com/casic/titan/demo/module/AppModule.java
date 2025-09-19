package com.casic.titan.demo.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.api.FileApiService;
import com.casic.titan.demo.R;
import com.casic.titan.demo.api.ApiServiceHelper;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.utils.common.PropertiesUtil;
import pers.fz.mvvm.utils.log.LogUtil;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class AppModule {

    @Provides
    public ApiServiceHelper provideApiServiceHelper(Application application, ErrorService errorService, RetryService retryService) {
        String baseUrl = PropertiesUtil.getInstance().loadConfig(application).getBaseUrl();
        LogUtil.show(ApiRetrofit.TAG, "App模块baseUrl:" + baseUrl);
        return new ApiRetrofit
                .Builder(application)
                .setSingleInstance(false)
                .setBaseUrl(baseUrl)
                .setRetryService(retryService)
                .setErrorService(errorService)
                .builder()
                .getApiService(ApiServiceHelper.class);
    }

    @Provides
    public FileApiService provideFileApiService(
            Application application, ErrorService errorService
    ) {
        String baseUrl = PropertiesUtil.getInstance().loadConfig(
                application,
                ContextCompat.getString(application, R.string.app_config_file)
        ).getBaseUrl();
        return new ApiRetrofit
                .Builder(application)
                .setSingleInstance(false)
                .setBaseUrl(baseUrl)
                .setTimeOut(30)
                .setErrorService(errorService)
                .builder()
                .getApiService(FileApiService.class);
    }
}

