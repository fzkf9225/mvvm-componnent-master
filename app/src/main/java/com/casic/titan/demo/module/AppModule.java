package com.casic.titan.demo.module;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.usercomponent.api.UserApiService;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.inter.ErrorService;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(SingletonComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class AppModule {

    @Inject
    ErrorService errorService;
    @Provides
    public ApiServiceHelper provideApiServiceHelper()
    {
        return new ApiRetrofit.Builder(Config.getInstance().getApplication())
                .addDefaultHeader()
                .setErrorService(errorService)
                .builder().getApiService(ApiServiceHelper.class);
    }
}

