package io.coderf.arklab.ui.form;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;

/**
 * FormSpinner 下拉列表项样式。
 */
public class FormSpinnerDropdownStyle {

    /** 下拉项高度（px），对应 XML {@code spinnerItemHeight} */
    public float itemHeightPx;
    /** 下拉项左 padding（px），对应 XML {@code spinnerItemPaddingStart} */
    public int paddingLeftPx;
    /** 下拉项右 padding（px），对应 XML {@code spinnerItemPaddingEnd} */
    public int paddingRightPx;
    /** 下拉项文字颜色，对应 XML {@code spinnerItemTextColor} */
    @ColorInt
    public int textColor;
    /** 下拉项分割线/边框颜色，对应 XML {@code spinnerItemBorderColor} */
    @ColorInt
    public int itemBorderColor;
    /** 下拉项选中文字颜色，对应 XML {@code spinnerItemTextSelectedColor} */
    @ColorInt
    public int textSelectedColor;
    /** 下拉项文字大小（px），对应 XML {@code spinnerItemTextSize}，null 表示使用默认 */
    @Nullable
    public Float textSizePx;
    /** 下拉项默认背景 */
    @Nullable
    public Drawable itemBackground;
    /** 下拉项选中背景 */
    @Nullable
    public Drawable itemSelectedBackground;
    /** 下拉弹窗背景，对应 XML {@code spinnerBackground} */
    public Drawable spinnerBackground;

    /** 创建默认样式 */
    @NonNull
    public static FormSpinnerDropdownStyle defaultStyle(@NonNull Context context) {
        FormSpinnerDropdownStyle style = new FormSpinnerDropdownStyle();
        style.itemHeightPx = DensityUtil.dp2px(context, 44f);
        style.paddingLeftPx = DensityUtil.dp2px(context, 16f);
        style.paddingRightPx = DensityUtil.dp2px(context, 16f);
        style.textColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.autoColor);
        style.textSelectedColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.themeColor);
        style.textSizePx = null;
        style.itemBackground = ContextCompat.getDrawable(context, io.coderf.arklab.common.R.color.white);
        style.itemSelectedBackground = ContextCompat.getDrawable(
                context, io.coderf.arklab.common.R.color.default_background);
        style.spinnerBackground = ContextCompat.getDrawable(
                context, R.drawable.form_spinner_dropdown_bg);
        style.itemBorderColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.h_line_color);
        return style;
    }

    /** 将另一份样式复制到当前实例 */
    public void apply(@NonNull FormSpinnerDropdownStyle other) {
        itemHeightPx = other.itemHeightPx;
        paddingLeftPx = other.paddingLeftPx;
        paddingRightPx = other.paddingRightPx;
        textColor = other.textColor;
        textSelectedColor = other.textSelectedColor;
        textSizePx = other.textSizePx;
        itemBackground = other.itemBackground;
        itemSelectedBackground = other.itemSelectedBackground;
        spinnerBackground = other.spinnerBackground;
        itemBorderColor = other.itemBorderColor;
    }
}
