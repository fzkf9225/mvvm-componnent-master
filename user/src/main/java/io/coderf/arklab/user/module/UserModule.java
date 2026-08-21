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
 * 用户 API 绑定。
 * <p>
 * 注意：{@link UserApiService} 客户端<strong>不</strong>挂载 RetryService / FlowRetryService，
 * 避免 refresh-token 请求再触发鉴权刷新形成递归；业务仓库通过构造注入
 * {@link io.coderf.arklab.core.request.TokenRefresher} 完成无感刷新。
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
