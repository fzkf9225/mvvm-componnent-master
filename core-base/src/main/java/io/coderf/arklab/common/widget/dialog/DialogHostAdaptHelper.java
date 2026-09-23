package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.autosize.AutoSizeCompat;
import io.coderf.arklab.common.autosize.internal.CustomAdapt;

/**
 * Dialog 横屏 inflate 密度对齐：跟随宿主 {@link CustomAdapt}，避免今日头条适配下字号/高度异常。
 * <p>
 * 竖屏不改密度，不影响现有竖屏弹窗表现。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/23
 */
public final class DialogHostAdaptHelper {

    /** 无 CustomAdapt 时横屏回退设计宽（与全局默认一致） */
    public static final float DEFAULT_DIALOG_ADAPT_SIZE_DP = 360f;

    private DialogHostAdaptHelper() {
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
            Activity activity = activityFrom(context);
            if (activity instanceof CustomAdapt) {
                AutoSizeCompat.autoConvertDensityOfCustomAdapt(resources, (CustomAdapt) activity);
            } else {
                AutoSizeCompat.autoConvertDensity(
                        resources,
                        DEFAULT_DIALOG_ADAPT_SIZE_DP,
                        false);
            }
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
