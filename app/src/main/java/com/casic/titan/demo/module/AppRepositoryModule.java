package com.casic.titan.demo.module;



import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.repository.KtDemoPagingRepositoryImpl;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import pers.fz.mvvm.api.RepositoryFactory;
import pers.fz.mvvm.inter.RetryService;

/**
 * created by fz on 2024/9/26 14:53
 * describe:
 */
@Module//必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(ViewModelComponent.class)//表示这个module中的配置是用来注入到Activity中的
public class AppRepositoryModule {

    @Provides
    public KtDemoPagingRepositoryImpl provideKtDemoPagingRepositoryImpl(RetryService retryService, ApiServiceHelper apiServiceHelper) {
        return RepositoryFactory.create(KtDemoPagingRepositoryImpl.class,retryService,apiServiceHelper);
    }
}

