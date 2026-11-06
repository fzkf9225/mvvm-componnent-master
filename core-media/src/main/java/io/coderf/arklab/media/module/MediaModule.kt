package io.coderf.arklab.media.module

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import io.coderf.arklab.media.MediaBuilder
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.enums.MediaPickerTypeEnum
import javax.inject.Qualifier

/**
 * Activity 作用域 [MediaHelper] 注入限定符。
 * 宿主须为 [ComponentActivity]（含 AppCompatActivity）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivityMediaHelper

/**
 * Fragment 作用域 [MediaHelper] 注入限定符。
 * Lifecycle 绑定到 Fragment 自身。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FragmentMediaHelper

/**
 * Hilt 提供默认配置的 [MediaHelper]。
 * <p>
 * 默认：Photo Picker、开启拍照 EXIF、各类型最大选 9；业务可在注入后再改 [MediaBuilder] 配置。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/10/28
 */
@Module
@InstallIn(ActivityComponent::class, FragmentComponent::class)
class MediaModule {

    /** Activity 级 MediaHelper；非 ComponentActivity 时抛异常 */
    @Provides
    @ActivityMediaHelper
    fun provideActivityMediaHelper(activity: Activity): MediaHelper {
        val owner = activity as? ComponentActivity
            ?: throw IllegalArgumentException(
                "ActivityMediaHelper requires ComponentActivity, but was ${activity.javaClass.name}"
            )
        return createDefaultMediaHelper(activity, owner)
    }

    /** Fragment 级 MediaHelper，Context 取 requireContext() */
    @Provides
    @FragmentMediaHelper
    fun provideFragmentMediaHelper(fragment: Fragment): MediaHelper {
        return createDefaultMediaHelper(fragment.requireContext(), fragment)
    }

    /**
     * 组装默认 MediaHelper（Activity / Fragment 共用）。
     *
     * @param context 用于路径、字符串资源等
     * @param lifecycleOwner 绑定权限/选择器 LifecycleObserver，ON_DESTROY 时释放
     */
    private fun createDefaultMediaHelper(context: Context, lifecycleOwner: LifecycleOwner): MediaHelper {
        return MediaBuilder(context)
            .bindLifeCycle(lifecycleOwner)
            .setWriteCaptureExifMetadata(true)
            .setImageMaxSelectedCount(MediaHelper.DEFAULT_ALBUM_MAX_COUNT)
            .setVideoMaxSelectedCount(MediaHelper.DEFAULT_VIDEO_MAX_COUNT)
            .setFileMaxSelectedCount(MediaHelper.DEFAULT_FILE_MAX_COUNT)
            .setAudioMaxSelectedCount(MediaHelper.DEFAULT_AUDIO_MAX_COUNT)
            .setMediaMaxSelectedCount(MediaHelper.DEFAULT_MEDIA_MAX_COUNT)
            .setChooseType(MediaPickerTypeEnum.PICK)
            .builder()
    }
}
