package io.coderf.arklab.common.utils.theme;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;

/**
 * 从当前主题解析 Material3 颜色 token。
 * <p>
 * 自定义 View 请用这里的方法，不要写 {@code Color.WHITE} / {@code R.color.autoColor}。
 * 角色说明见 {@code values/colors.xml} 顶部注释。
 */
public final class ThemeAttrs {

    private ThemeAttrs() {
    }

    @ColorInt
    public static int color(@NonNull Context context, @AttrRes int attr, @ColorInt int fallback) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, value, true)) {
            if (value.resourceId != 0) {
                return ContextCompat.getColor(context, value.resourceId);
            }
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return value.data;
            }
        }
        return fallback;
    }

    /** 主色：按钮填充、选中态、聚焦描边。 */
    @ColorInt
    public static int primary(@NonNull Context context) {
        return color(context, androidx.appcompat.R.attr.colorPrimary,
                ContextCompat.getColor(context, R.color.themeColor));
    }

    /** 主色上的字/图标，不是页面正文色。 */
    @ColorInt
    public static int onPrimary(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOnPrimary,
                ContextCompat.getColor(context, R.color.onPrimary));
    }

    /** 卡片/控件表面。 */
    @ColorInt
    public static int surface(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorSurface,
                ContextCompat.getColor(context, R.color.cardSurface));
    }

    /** 表面上的正文/标题。 */
    @ColorInt
    public static int onSurface(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOnSurface,
                ContextCompat.getColor(context, R.color.cardOnSurface));
    }

    /** 次要文字、hint、未选中态。 */
    @ColorInt
    public static int onSurfaceVariant(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOnSurfaceVariant,
                ContextCompat.getColor(context, R.color.cardOnSurfaceVariant));
    }

    /** 列表行、底栏等贴在页面上的一层。 */
    @ColorInt
    public static int surfaceContainer(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorSurfaceContainer,
                ContextCompat.getColor(context, R.color.surfaceContainer));
    }

    /** Dialog / Popup / BottomSheet / 菜单。 */
    @ColorInt
    public static int surfaceContainerHigh(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorSurfaceContainerHigh,
                ContextCompat.getColor(context, R.color.surfaceContainerHigh));
    }

    /** 更高一层叠层（嵌套菜单、最上层 sheet）。 */
    @ColorInt
    public static int surfaceContainerHighest(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorSurfaceContainerHighest,
                ContextCompat.getColor(context, R.color.surfaceContainerHighest));
    }

    /** 描边、图标轮廓、未聚焦边框。 */
    @ColorInt
    public static int outline(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOutline,
                ContextCompat.getColor(context, R.color.outline));
    }

    /** 分割线、弱边框，不要当正文色。 */
    @ColorInt
    public static int outlineVariant(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOutlineVariant,
                ContextCompat.getColor(context, R.color.outlineVariant));
    }

    /** 错误态文字、必填星号、校验描边。 */
    @ColorInt
    public static int error(@NonNull Context context) {
        return color(context, androidx.appcompat.R.attr.colorError,
                ContextCompat.getColor(context, R.color.theme_red));
    }

    /** 铺在 colorError 上的字/图标。 */
    @ColorInt
    public static int onError(@NonNull Context context) {
        return color(context, com.google.android.material.R.attr.colorOnError,
                ContextCompat.getColor(context, R.color.onError));
    }
}
