package pers.fz.media.module

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import pers.fz.media.MediaBuilder
import pers.fz.media.MediaHelper

/**
 * created by fz on 2024/12/19 17:53
 * describe:
 */
@Module //必须配置的注解，表示这个对象是Module的配置规则
@InstallIn(FragmentComponent::class) //表示这个module中的配置是用来注入到Activity中的
class MediaFragmentModule {
    @Provides
    @MediaFragmentComponent
    fun provideMediaHelper(fragment: Fragment): MediaHelper {
        return MediaBuilder(fragment)
            .setImageMaxSelectedCount(MediaHelper.DEFAULT_ALBUM_MAX_COUNT)
            .setVideoMaxSelectedCount(MediaHelper.DEFAULT_VIDEO_MAX_COUNT)
            .setFileMaxSelectedCount(MediaHelper.DEFAULT_FILE_MAX_COUNT)
            .setAudioMaxSelectedCount(MediaHelper.DEFAULT_AUDIO_MAX_COUNT)
            .setChooseType(MediaHelper.PICK_TYPE)
            .builder()
    }
}

