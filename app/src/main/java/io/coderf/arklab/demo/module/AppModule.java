package io.coderf.arklab.demo.module;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.base.api.AppPropertiesConfig;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.inter.FlowRetryService;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.demo.api.ApiServiceHelper;
import io.coderf.arklab.ui.api.FileApiService;

/**
 * created by fz on 2024/9/26 14:53
 * describe: 网络 API 绑定；baseUrl 等来自注入的 {@link AppPropertiesConfig}
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    public ApiServiceHelper provideApiServiceHelper(
            Application application,
            AppPropertiesConfig config,
            ErrorService errorService,
            RetryService retryService,
            FlowRetryService flowRetryService
    ) {
        return new ApiRetrofit
                .Builder(application)
                .setSingleInstance(false)
                .setBaseUrl(config.getBaseUrl())
                .setRetryService(retryService)
                .setFlowRetryService(flowRetryService)
                .setErrorService(errorService)
                .builder()
                .getApiService(ApiServiceHelper.class);
    }

    @Provides
    public FileApiService provideFileApiService(
            Application application,
            AppPropertiesConfig config,
            ErrorService errorService
    ) {
        String fileBaseUrl = config.getFileBaseUrl();
        String baseUrl = (fileBaseUrl == null || fileBaseUrl.isEmpty())
                ? config.getBaseUrl()
                : fileBaseUrl;
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
