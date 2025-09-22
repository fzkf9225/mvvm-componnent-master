package com.casic.otitan.media.module

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import com.casic.otitan.media.MediaBuilder
import com.casic.otitan.media.MediaHelper
import com.casic.otitan.media.enums.MediaPickerTypeEnum
import javax.inject.Qualifier

/**
 * created by fz on 2024/12/19 17:53
 * describe:
 */
@Module //必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(*[ActivityComponent::class, FragmentComponent::class]) //表示这个module中的配置是用来注入到Activity中的
class MediaModule {
    @Provides
    @ActivityMediaHelper
    fun provideActivityMediaHelper(activity: Activity): MediaHelper {
        return MediaBuilder(activity)
            .bindLifeCycle(activity as ComponentActivity)
            .setImageMaxSelectedCount(MediaHelper.DEFAULT_ALBUM_MAX_COUNT)
            .setVideoMaxSelectedCount(MediaHelper.DEFAULT_VIDEO_MAX_COUNT)
            .setFileMaxSelectedCount(MediaHelper.DEFAULT_FILE_MAX_COUNT)
            .setAudioMaxSelectedCount(MediaHelper.DEFAULT_AUDIO_MAX_COUNT)
            .setMediaMaxSelectedCount(MediaHelper.DEFAULT_MEDIA_MAX_COUNT)
            .setChooseType(MediaPickerTypeEnum.PICK)
            .builder()
    }

    @Provides
    @FragmentMediaHelper
    fun provideFragmentMediaHelper(fragment: Fragment): MediaHelper {
        return MediaBuilder(fragment.requireContext())
            .bindLifeCycle(fragment)
            .setImageMaxSelectedCount(MediaHelper.DEFAULT_ALBUM_MAX_COUNT)
            .setVideoMaxSelectedCount(MediaHelper.DEFAULT_VIDEO_MAX_COUNT)
            .setFileMaxSelectedCount(MediaHelper.DEFAULT_FILE_MAX_COUNT)
            .setAudioMaxSelectedCount(MediaHelper.DEFAULT_AUDIO_MAX_COUNT)
            .setMediaMaxSelectedCount(MediaHelper.DEFAULT_MEDIA_MAX_COUNT)
            .setChooseType(MediaPickerTypeEnum.PICK)
            .builder()
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ActivityMediaHelper

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class FragmentMediaHelper
}

