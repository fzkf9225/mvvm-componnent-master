package io.coderf.arklab.common.widget.video;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * 将 {@link VideoPlayerController} 绑定到宿主 Lifecycle，自动处理 pause / resume / destroy 与屏幕旋转。
 * <p>
 * 通过 {@link VideoPlayerController#bindLifecycle(LifecycleOwner)} 注册，包内可见。
 * </p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
final class VideoPlayerLifecycleObserver implements DefaultLifecycleObserver, ComponentCallbacks {

    /** 绑定的播放控制器，生命周期事件会转发到其上 */
    @NonNull
    private final VideoPlayerController controller;
    /** 用于注册/注销 {@link ComponentCallbacks} 的宿主 Activity */
    @NonNull
    private final Activity activity;
    /** 是否已向 Activity 注册 configuration 回调，避免重复 register */
    private boolean configurationRegistered;

    /**
     * @param controller 播放控制器
     * @param activity   用于注册 {@link ComponentCallbacks} 的 Activity
     */
    VideoPlayerLifecycleObserver(@NonNull VideoPlayerController controller, @NonNull Activity activity) {
        this.controller = controller;
        this.activity = activity;
    }

    /**
     * 向 LifecycleOwner 注册本观察者。
     *
     * @param controller 播放控制器
     * @param owner      生命周期拥有者
     * @param activity   屏幕旋转回调所绑定的 Activity
     */
    static void bind(@NonNull VideoPlayerController controller,
                     @NonNull LifecycleOwner owner,
                     @NonNull Activity activity) {
        owner.getLifecycle().addObserver(new VideoPlayerLifecycleObserver(controller, activity));
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        registerConfigurationCallbackIfNeeded();
        controller.onHostResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        controller.onHostPause();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        unregisterConfigurationCallback();
        controller.release();
        owner.getLifecycle().removeObserver(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        controller.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
    }

    private void registerConfigurationCallbackIfNeeded() {
        if (configurationRegistered) {
            return;
        }
        activity.registerComponentCallbacks(this);
        configurationRegistered = true;
    }

    private void unregisterConfigurationCallback() {
        if (!configurationRegistered) {
            return;
        }
        activity.unregisterComponentCallbacks(this);
        configurationRegistered = false;
    }

    /**
     * 从 LifecycleOwner 解析 Activity；Fragment 未 attach 时回退到 fallback。
     *
     * @param owner    LifecycleOwner（Activity 或 Fragment）
     * @param fallback 无法解析时的兜底 Activity
     * @return 非空 Activity
     */
    @Nullable
    static Activity resolveActivity(@NonNull LifecycleOwner owner, @NonNull Activity fallback) {
        if (owner instanceof Activity) {
            return (Activity) owner;
        }
        if (owner instanceof Fragment) {
            Activity activity = ((Fragment) owner).getActivity();
            if (activity != null) {
                return activity;
            }
        }
        return fallback;
    }
}
