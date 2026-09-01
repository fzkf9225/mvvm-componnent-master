package io.coderf.arklab.common.model

import android.app.Application
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.coderf.arklab.common.R
import io.coderf.arklab.common.utils.common.PropertiesUtil

/**
 * PropertiesModule 对象。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@Module
@InstallIn(SingletonComponent::class)
object PropertiesModule {

    @Provides
    fun providePropertyUtil(application: Application): PropertiesUtil {
        return PropertiesUtil.getInstance().loadConfig(
            application,
            ContextCompat.getString(
                application,
                R.string.app_config_file
            )
        )
    }
}