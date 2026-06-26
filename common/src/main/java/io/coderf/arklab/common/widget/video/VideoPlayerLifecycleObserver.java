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
 */
final class VideoPlayerLifecycleObserver implements DefaultLifecycleObserver, ComponentCallbacks {

    @NonNull
    private final VideoPlayerController controller;
    @NonNull
    private final Activity activity;
    private boolean configurationRegistered;

    VideoPlayerLifecycleObserver(@NonNull VideoPlayerController controller, @NonNull Activity activity) {
        this.controller = controller;
        this.activity = activity;
    }

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
