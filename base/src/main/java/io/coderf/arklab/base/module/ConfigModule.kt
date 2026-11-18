package io.coderf.arklab.base.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.coderf.arklab.base.api.AppPropertiesConfig
import io.coderf.arklab.common.api.ApiBaseUrl
import javax.inject.Singleton

/**
 * 全工程唯一的应用配置绑定。
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideAppConfig(
        @ApplicationContext context: Context,
        configLoader: ConfigLoader,
    ): AppPropertiesConfig {
        val config = configLoader.load(context)
        ApiBaseUrl.set(config.baseUrl)
        return config
    }
}
