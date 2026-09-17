package io.coderf.arklab.ui.form;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

/**
 * 表单外观配置，用于运行时批量换肤 / 覆盖默认样式。
 * <p>
 * 仅设置非 null / 有意义的字段；调用 {@link FormConstraintLayout#applyFormConfig(FormUiConfig)} 后立即生效。
 * 也可通过 {@link FormConstraintLayout#setFormStyleOverlay(int)} 直接套 style 资源。
 */
public final class FormUiConfig {

    @Nullable
    public Integer formTextColor;
    @Nullable
    public Integer formHintTextColor;
    @Nullable
    public Integer labelTextColor;
    @Nullable
    public Integer borderBottomColor;
    @Nullable
    public Integer requiredTextColor;

    @Nullable
    public Float formLabelTextSize;
    @Nullable
    public Float formTextSize;
    @Nullable
    public Float formRequiredSize;
    @Nullable
    public Float bottomBorderHeight;
    @Nullable
    public Float borderBottomStartMargin;
    @Nullable
    public Float borderBottomEndMargin;
    @Nullable
    public Float labelStartMargin;
    @Nullable
    public Float labelEndMargin;
    @Nullable
    public Float labelTopMargin;
    @Nullable
    public Float labelBottomMargin;
    @Nullable
    public Float textStartMargin;
    @Nullable
    public Float textEndMargin;
    @Nullable
    public Float defaultTextMargin;
    @Nullable
    public Float requiredStartMargin;

    @Nullable
    public Boolean bottomBorder;
    @Nullable
    public Boolean required;
    @Nullable
    public String requiredText;
    @Nullable
    public String hint;
    @Nullable
    public String label;

    @Nullable
    public Integer labelAlign;
    @Nullable
    public Integer textAlign;
    @Nullable
    public Integer labelVerticalAlign;
    @Nullable
    public Integer labelTextStyle;
    @Nullable
    public Integer line;

    @Nullable
    public Boolean showLabelIcon;
    @Nullable
    public Drawable labelIcon;
    @Nullable
    public Float labelIconWidth;
    @Nullable
    public Float labelIconHeight;
    @Nullable
    public Float labelIconStartMargin;
    @Nullable
    public Float labelIconEndMargin;

    /** 可选：一次性应用的 style 资源（与字段叠加时，字段优先）。 */
    @StyleRes
    public int styleOverlay;

    public FormUiConfig() {
    }

    public FormUiConfig formTextColor(@ColorInt int color) {
        this.formTextColor = color;
        return this;
    }

    public FormUiConfig formHintTextColor(@ColorInt int color) {
        this.formHintTextColor = color;
        return this;
    }

    public FormUiConfig labelTextColor(@ColorInt int color) {
        this.labelTextColor = color;
        return this;
    }

    public FormUiConfig borderBottomColor(@ColorInt int color) {
        this.borderBottomColor = color;
        return this;
    }

    public FormUiConfig requiredTextColor(@ColorInt int color) {
        this.requiredTextColor = color;
        return this;
    }

    public FormUiConfig formLabelTextSize(float px) {
        this.formLabelTextSize = px;
        return this;
    }

    public FormUiConfig formTextSize(float px) {
        this.formTextSize = px;
        return this;
    }

    public FormUiConfig formRequiredSize(float px) {
        this.formRequiredSize = px;
        return this;
    }

    public FormUiConfig bottomBorderHeight(float px) {
        this.bottomBorderHeight = px;
        return this;
    }

    public FormUiConfig bottomBorder(boolean show) {
        this.bottomBorder = show;
        return this;
    }

    public FormUiConfig required(boolean required) {
        this.required = required;
        return this;
    }

    public FormUiConfig requiredText(@Nullable String text) {
        this.requiredText = text;
        return this;
    }

    public FormUiConfig label(@Nullable String label) {
        this.label = label;
        return this;
    }

    public FormUiConfig hint(@Nullable String hint) {
        this.hint = hint;
        return this;
    }

    public FormUiConfig styleOverlay(@StyleRes int styleRes) {
        this.styleOverlay = styleRes;
        return this;
    }
}
