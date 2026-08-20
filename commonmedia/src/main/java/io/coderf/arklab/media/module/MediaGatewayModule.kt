package io.coderf.arklab.media.module

import android.app.Activity
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.gateway.MediaHelperGateway
import io.coderf.arklab.userapi.gateway.MediaGateway

/**
 * Activity 作用域绑定 [MediaGateway]，业务模块只依赖 userapi。
 */
@Module
@InstallIn(ActivityComponent::class)
object MediaGatewayModule {

    @Provides
    fun provideMediaGateway(
        activity: Activity,
        @ActivityMediaHelper mediaHelper: MediaHelper
    ): MediaGateway {
        return MediaHelperGateway(activity as ComponentActivity, mediaHelper)
    }
}
