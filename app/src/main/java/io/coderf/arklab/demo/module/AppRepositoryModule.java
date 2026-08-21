package io.coderf.arklab.demo.module;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import io.coderf.arklab.demo.api.ApiServiceHelper;
import io.coderf.arklab.demo.repository.KtDemoPagingRepositoryImpl;

/**
 * ViewModel 作用域仓库提供。
 * Token 刷新使用 [io.coderf.arklab.core.request.TokenRefresherHolder] 全局默认，无需再注入 TokenRefresher。
 */
@Module
@InstallIn(ViewModelComponent.class)
public class AppRepositoryModule {

    @Provides
    public KtDemoPagingRepositoryImpl provideKtDemoPagingRepositoryImpl(
            ApiServiceHelper apiServiceHelper
    ) {
        return new KtDemoPagingRepositoryImpl(apiServiceHelper);
    }
}
