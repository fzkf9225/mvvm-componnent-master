package io.coderf.arklab.common.widget.feedback;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import io.coderf.arklab.common.api.BaseApplication;

/**
 * 全局 Toast 封装：统一主线程调用与空文案过滤，可在无 {@link io.coderf.arklab.common.helper.UIController} 的场景直接使用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:25
 */
public final class ToastHelper {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ToastHelper() {
    }

    public static void showShort(@Nullable Context context, @Nullable CharSequence message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLong(@Nullable Context context, @Nullable CharSequence message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    public static void showShort(@StringRes int messageRes) {
        Context context = BaseApplication.getInstance();
        if (context == null) {
            return;
        }
        showShort(context, context.getString(messageRes));
    }

    public static void show(@Nullable Context context, @Nullable CharSequence message, int duration) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        Context appContext = resolveContext(context);
        if (appContext == null) {
            return;
        }
        Runnable task = () -> Toast.makeText(appContext, message, duration).show();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            MAIN_HANDLER.post(task);
        }
    }

    @NonNull
    private static Context resolveContext(@Nullable Context context) {
        if (context != null) {
            return context.getApplicationContext();
        }
        Context app = BaseApplication.getInstance();
        return app == null ? context : app;
    }
}
