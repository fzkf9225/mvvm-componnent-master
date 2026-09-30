package io.coderf.arklab.core.autosize.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import io.coderf.arklab.core.autosize.AutoSizeConfig;

/**
 * Android 14+（API 34）非线性字体缩放兼容：
 * {@link DisplayMetrics#scaledDensity} 已不可靠，优先用 {@link Configuration#fontScale}。
 * <p>
 * 不改变今日头条 density 适配主路径，仅修正 sp / 屏蔽系统字体时的计算与 Configuration。
 */
public final class FontScaleCompat {

    private FontScaleCompat() {
    }

    /**
     * 目标 scaledDensity = targetDensity * 本方法返回值。
     */
    public static float resolveTargetScaledDensity(float targetDensity) {
        float privateScale = AutoSizeConfig.getInstance().getPrivateFontScale();
        if (privateScale > 0f) {
            return targetDensity * privateScale;
        }
        return targetDensity * resolveSystemFontScaleFactor();
    }

    /**
     * 相对「设计 density」的字体倍率：exclude 时为 1；API 34+ 读 fontScale；否则用 initScaled/initDensity。
     */
    public static float resolveSystemFontScaleFactor() {
        if (AutoSizeConfig.getInstance().isExcludeFontScale()) {
            return 1f;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            float fontScale = Resources.getSystem().getConfiguration().fontScale;
            return fontScale > 0f ? fontScale : 1f;
        }
        float initDensity = AutoSizeConfig.getInstance().getInitDensity();
        if (initDensity <= 0f) {
            return 1f;
        }
        return AutoSizeConfig.getInstance().getInitScaledDensity() / initDensity;
    }

    /**
     * API 34+ 在屏蔽系统字体或使用私有倍率时，同步写 {@link Configuration#fontScale}，
     * 避免仅改 scaledDensity 失效。
     */
    public static void applyFontScaleConfiguration(@NonNull Resources resources) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return;
        }
        float desired;
        float privateScale = AutoSizeConfig.getInstance().getPrivateFontScale();
        if (privateScale > 0f) {
            desired = privateScale;
        } else if (AutoSizeConfig.getInstance().isExcludeFontScale()) {
            desired = 1f;
        } else {
            return;
        }
        setFontScaleIfNeeded(resources.getConfiguration(), desired);
        setFontScaleIfNeeded(
                AutoSizeConfig.getInstance().getApplication().getResources().getConfiguration(),
                desired);
    }

    private static void setFontScaleIfNeeded(@NonNull Configuration configuration, float desired) {
        if (Math.abs(configuration.fontScale - desired) > 0.001f) {
            configuration.fontScale = desired;
        }
    }
}
