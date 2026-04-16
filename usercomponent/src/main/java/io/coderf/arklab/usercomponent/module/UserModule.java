package io.coderf.arklab.usercomponent.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.usercomponent.R;
import io.coderf.arklab.usercomponent.api.UserApiService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.common.PropertiesUtil;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class UserModule {

    @Provides
    public UserApiService provideUserApiService(Application application,ErrorService errorService)
    {
        String baseUrl = PropertiesUtil.getInstance().loadConfig(application, ContextCompat.getString(application, R.string.user_config_file)).getBaseUrl();
        LogUtil.show(ApiRetrofit.TAG,"登录baseUrl:"+baseUrl);
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

