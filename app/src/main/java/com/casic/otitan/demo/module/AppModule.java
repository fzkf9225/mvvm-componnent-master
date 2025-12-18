package com.casic.otitan.demo.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import com.casic.otitan.common.inter.FlowRetryService;
import com.casic.otitan.commonui.api.FileApiService;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.api.ApiServiceHelper;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import com.casic.otitan.common.api.ApiRetrofit;
import com.casic.otitan.common.inter.ErrorService;
import com.casic.otitan.common.inter.RetryService;
import com.casic.otitan.common.utils.common.PropertiesUtil;
import com.casic.otitan.common.utils.log.LogUtil;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class AppModule {

    @Provides
    public ApiServiceHelper provideApiServiceHelper(Application application, ErrorService errorService, RetryService retryService, FlowRetryService flowRetryService) {
        String baseUrl = PropertiesUtil.getInstance().loadConfig(application).getBaseUrl();
        LogUtil.show(ApiRetrofit.TAG, "App模块baseUrl:" + baseUrl);
        return new ApiRetrofit
                .Builder(application)
                .setSingleInstance(false)
                .setBaseUrl(baseUrl)
                .setRetryService(retryService)
                .setFlowRetryService(flowRetryService)
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

