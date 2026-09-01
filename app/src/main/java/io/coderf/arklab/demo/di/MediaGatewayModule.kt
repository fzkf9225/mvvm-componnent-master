package io.coderf.arklab.demo.di

import android.app.Activity
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.coderf.arklab.base.gateway.MediaGateway
import io.coderf.arklab.demo.media.MediaHelperGateway
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.module.ActivityMediaHelper

/**
 * Demo 组装层绑定 [MediaGateway]；业务模块只依赖 :base。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
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
