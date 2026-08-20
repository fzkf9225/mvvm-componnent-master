package io.coderf.arklab.user.module;

import android.app.Application;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.base.api.AppPropertiesConfig;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.user.api.UserApiService;

/**
 * created by fz on 2024/9/26 14:53
 * describe: 用户 API 绑定；baseUrl 来自注入的 {@link AppPropertiesConfig}
 */
@Module
@InstallIn(SingletonComponent.class)
public class UserModule {

    @Provides
    public UserApiService provideUserApiService(
            Application application,
            AppPropertiesConfig config,
            ErrorService errorService
    ) {
        String baseUrl = config.getBaseUrl();
        LogUtil.logger(ApiRetrofit.TAG, "登录baseUrl:" + baseUrl);
        return new ApiRetrofit
                .Builder(application)
                .setSingleInstance(false)
                .setBaseUrl(baseUrl)
                .setErrorService(errorService)
                .setTimeOut(15)
                .builder()
                .getApiService(UserApiService.class);
    }
}
