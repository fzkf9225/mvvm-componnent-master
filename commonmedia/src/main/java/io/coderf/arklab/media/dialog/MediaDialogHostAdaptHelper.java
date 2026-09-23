package io.coderf.arklab.media.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

/**
 * Dialog 横屏 inflate 密度对齐：运行时若 classpath 存在 core-base 的 AutoSize / CustomAdapt，
 * 则跟随宿主密度，避免今日头条适配下字号/高度异常；否则直接 inflate。
 * <p>
 * 本模块不编译依赖 core-base，通过反射可选接入。竖屏不改密度。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/23
 */
public final class MediaDialogHostAdaptHelper {

    /** 无 CustomAdapt 时横屏回退设计宽（与全局默认一致） */
    public static final float DEFAULT_DIALOG_ADAPT_SIZE_DP = 360f;

    private static final String CUSTOM_ADAPT =
            "io.coderf.arklab.common.autosize.internal.CustomAdapt";
    private static final String AUTO_SIZE_COMPAT =
            "io.coderf.arklab.common.autosize.AutoSizeCompat";

    private MediaDialogHostAdaptHelper() {
    }

    @FunctionalInterface
    public interface InflateAction<T> {
        T inflate();
    }

    /**
     * 横屏时临时同步宿主密度再 inflate，结束后还原；竖屏直接 inflate。
     */
    @NonNull
    public static <T> T inflateWithHostAdapt(
            @NonNull Context context,
            @NonNull InflateAction<T> inflate) {
        Resources resources = context.getResources();
        boolean landscape =
                resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        DisplayMetrics savedMetrics = null;
        Configuration savedConfiguration = null;
        if (landscape) {
            savedMetrics = new DisplayMetrics();
            savedMetrics.setTo(resources.getDisplayMetrics());
            savedConfiguration = new Configuration(resources.getConfiguration());
            tryApplyHostDensity(context, resources);
        }
        try {
            return inflate.inflate();
        } finally {
            if (savedMetrics != null) {
                resources.getDisplayMetrics().setTo(savedMetrics);
                if (savedConfiguration != null) {
                    //noinspection deprecation
                    resources.updateConfiguration(savedConfiguration, resources.getDisplayMetrics());
                }
            }
        }
    }

    /**
     * 可选接入 core-base AutoSize；不在 classpath 时静默跳过。
     */
    private static void tryApplyHostDensity(@NonNull Context context, @NonNull Resources resources) {
        try {
            Class<?> customAdaptClass = Class.forName(CUSTOM_ADAPT);
            Class<?> autoSizeCompatClass = Class.forName(AUTO_SIZE_COMPAT);
            Activity activity = activityFrom(context);
            if (activity != null && customAdaptClass.isInstance(activity)) {
                Method method = autoSizeCompatClass.getMethod(
                        "autoConvertDensityOfCustomAdapt", Resources.class, customAdaptClass);
                method.invoke(null, resources, activity);
            } else {
                Method method = autoSizeCompatClass.getMethod(
                        "autoConvertDensity", Resources.class, float.class, boolean.class);
                method.invoke(null, resources, DEFAULT_DIALOG_ADAPT_SIZE_DP, false);
            }
        } catch (Throwable ignored) {
            // commonmedia 独立使用或未集成 AutoSize 时不做密度转换
        }
    }

    @Nullable
    public static Activity activityFrom(@Nullable Context context) {
        Context ctx = context;
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                return (Activity) ctx;
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return null;
    }
}
