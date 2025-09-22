package com.casic.otitan.demo.module;



import com.casic.otitan.demo.api.ApiServiceHelper;
import com.casic.otitan.demo.repository.KtDemoPagingRepositoryImpl;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import com.casic.otitan.common.api.RepositoryFactory;
import com.casic.otitan.common.inter.RetryService;

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

