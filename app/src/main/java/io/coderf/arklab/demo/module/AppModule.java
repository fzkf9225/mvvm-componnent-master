package io.coderf.arklab.demo.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.inter.FlowRetryService;
import io.coderf.arklab.ui.api.FileApiService;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.api.ApiServiceHelper;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.utils.common.PropertiesUtil;
import io.coderf.arklab.common.utils.log.LogUtil;

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

