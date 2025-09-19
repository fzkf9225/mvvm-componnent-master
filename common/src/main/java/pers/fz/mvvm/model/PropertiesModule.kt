package pers.fz.mvvm.model

import android.app.Application
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pers.fz.mvvm.R
import pers.fz.mvvm.utils.common.PropertiesUtil

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