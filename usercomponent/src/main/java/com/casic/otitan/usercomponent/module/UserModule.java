package com.casic.otitan.usercomponent.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import com.casic.otitan.usercomponent.R;
import com.casic.otitan.usercomponent.api.UserApiService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import com.casic.otitan.common.api.ApiRetrofit;
import com.casic.otitan.common.inter.ErrorService;
import com.casic.otitan.common.utils.common.PropertiesUtil;
import com.casic.otitan.common.utils.log.LogUtil;

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

