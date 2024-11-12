package com.casic.titan.usercomponent.module;

import android.app.Application;

import androidx.core.content.ContextCompat;

import com.casic.titan.usercomponent.R;
import com.casic.titan.usercomponent.api.UserApiService;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.common.PropertiesUtil;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class UserModule {

    @Inject
    ErrorService errorService;
    @Provides
    public UserApiService provideUserApiService(Application application)
    {
        String baseUrl = PropertiesUtil.getInstance().loadConfig(application, ContextCompat.getString(application, R.string.user_config_file)).getBaseUrl();
        LogUtil.show(ApiRetrofit.TAG,"登录baseUrl:"+baseUrl);
        return new ApiRetrofit
                .Builder(Config.getInstance().getApplication())
                .setBaseUrl(baseUrl)
                .setErrorService(errorService)
                .builder()
                .getApiService(UserApiService.class);
    }
}

