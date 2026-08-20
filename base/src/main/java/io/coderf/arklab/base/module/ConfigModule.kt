package io.coderf.arklab.base.module

import android.content.Context
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.coderf.arklab.base.R
import io.coderf.arklab.base.api.AppPropertiesConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideAppConfig(
        @ApplicationContext context: Context,
        configLoader: ConfigLoader
    ): AppPropertiesConfig {
        val configFile = ContextCompat.getString(context, R.string.base_config_file)
        return configLoader.loadConfig(configFile)
    }
}
