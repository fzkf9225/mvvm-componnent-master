package io.coderf.arklab.common.widget.dialog;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * {@link ImageSaveDialog} 样式与文案配置；字段为 null 表示使用当前默认样式。
 */
public class ImageSaveDialogConfig {

    @Nullable
    private final String saveText;
    @Nullable
    private final String cancelText;
    @Nullable
    @ColorInt
    private final Integer saveTextColor;
    @Nullable
    @ColorInt
    private final Integer cancelTextColor;
    /** 单位 px */
    @Nullable
    private final Float saveTextSizePx;
    /** 单位 px */
    @Nullable
    private final Float cancelTextSizePx;
    /** 两个按钮统一高度，单位 px */
    @Nullable
    private final Integer buttonHeightPx;
    @Nullable
    private final Integer horizontalMarginPx;
    @Nullable
    private final Integer bottomMarginPx;
    @Nullable
    private final Integer buttonGapPx;
    @Nullable
    private final Float cornerRadiusPx;
    @Nullable
    private final Float dimAmount;
    /**
     * 是否对下层窗口加半透明遮罩。大图预览场景应为 false，避免覆盖预览黑色背景。
     */
    @Nullable
    private final Boolean dimBehindEnabled;
    @Nullable
    private final Boolean saveTextBold;
    @Nullable
    @ColorInt
    private final Integer buttonBackgroundColor;
    @Nullable
    @ColorInt
    private final Integer rippleColor;

    private ImageSaveDialogConfig(Builder builder) {
        this.saveText = builder.saveText;
        this.cancelText = builder.cancelText;
        this.saveTextColor = builder.saveTextColor;
        this.cancelTextColor = builder.cancelTextColor;
        this.saveTextSizePx = builder.saveTextSizePx;
        this.cancelTextSizePx = builder.cancelTextSizePx;
        this.buttonHeightPx = builder.buttonHeightPx;
        this.horizontalMarginPx = builder.horizontalMarginPx;
        this.bottomMarginPx = builder.bottomMarginPx;
        this.buttonGapPx = builder.buttonGapPx;
        this.cornerRadiusPx = builder.cornerRadiusPx;
        this.dimAmount = builder.dimAmount;
        this.dimBehindEnabled = builder.dimBehindEnabled;
        this.saveTextBold = builder.saveTextBold;
        this.buttonBackgroundColor = builder.buttonBackgroundColor;
        this.rippleColor = builder.rippleColor;
    }

    /** 全部使用 {@link ImageSaveDialog} 内置默认样式 */
    public static ImageSaveDialogConfig empty() {
        return new Builder().build();
    }

    @Nullable
    public String getSaveText() {
        return saveText;
    }

    @Nullable
    public String getCancelText() {
        return cancelText;
    }

    @Nullable
    @ColorInt
    public Integer getSaveTextColor() {
        return saveTextColor;
    }

    @Nullable
    @ColorInt
    public Integer getCancelTextColor() {
        return cancelTextColor;
    }

    @Nullable
    public Float getSaveTextSizePx() {
        return saveTextSizePx;
    }

    @Nullable
    public Float getCancelTextSizePx() {
        return cancelTextSizePx;
    }

    @Nullable
    public Integer getButtonHeightPx() {
        return buttonHeightPx;
    }

    @Nullable
    public Integer getHorizontalMarginPx() {
        return horizontalMarginPx;
    }

    @Nullable
    public Integer getBottomMarginPx() {
        return bottomMarginPx;
    }

    @Nullable
    public Integer getButtonGapPx() {
        return buttonGapPx;
    }

    @Nullable
    public Float getCornerRadiusPx() {
        return cornerRadiusPx;
    }

    @Nullable
    public Float getDimAmount() {
        return dimAmount;
    }

    @Nullable
    public Boolean getDimBehindEnabled() {
        return dimBehindEnabled;
    }

    @Nullable
    public Boolean getSaveTextBold() {
        return saveTextBold;
    }

    @Nullable
    @ColorInt
    public Integer getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    @Nullable
    @ColorInt
    public Integer getRippleColor() {
        return rippleColor;
    }

    public static final class Builder {
        private String saveText;
        private String cancelText;
        private Integer saveTextColor;
        private Integer cancelTextColor;
        private Float saveTextSizePx;
        private Float cancelTextSizePx;
        private Integer buttonHeightPx;
        private Integer horizontalMarginPx;
        private Integer bottomMarginPx;
        private Integer buttonGapPx;
        private Float cornerRadiusPx;
        private Float dimAmount;
        private Boolean dimBehindEnabled = false;
        private Boolean saveTextBold;
        private Integer buttonBackgroundColor;
        private Integer rippleColor;

        public Builder setSaveText(@Nullable String saveText) {
            this.saveText = saveText;
            return this;
        }

        public Builder setCancelText(@Nullable String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public Builder setSaveTextColor(@ColorInt @Nullable Integer saveTextColor) {
            this.saveTextColor = saveTextColor;
            return this;
        }

        public Builder setCancelTextColor(@ColorInt @Nullable Integer cancelTextColor) {
            this.cancelTextColor = cancelTextColor;
            return this;
        }

        public Builder setSaveTextSizePx(@Nullable Float saveTextSizePx) {
            this.saveTextSizePx = saveTextSizePx;
            return this;
        }

        public Builder setCancelTextSizePx(@Nullable Float cancelTextSizePx) {
            this.cancelTextSizePx = cancelTextSizePx;
            return this;
        }

        public Builder setButtonHeightPx(@Nullable Integer buttonHeightPx) {
            this.buttonHeightPx = buttonHeightPx;
            return this;
        }

        public Builder setHorizontalMarginPx(@Nullable Integer horizontalMarginPx) {
            this.horizontalMarginPx = horizontalMarginPx;
            return this;
        }

        public Builder setBottomMarginPx(@Nullable Integer bottomMarginPx) {
            this.bottomMarginPx = bottomMarginPx;
            return this;
        }

        public Builder setButtonGapPx(@Nullable Integer buttonGapPx) {
            this.buttonGapPx = buttonGapPx;
            return this;
        }

        public Builder setCornerRadiusPx(@Nullable Float cornerRadiusPx) {
            this.cornerRadiusPx = cornerRadiusPx;
            return this;
        }

        public Builder setDimAmount(@Nullable Float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        public Builder setDimBehindEnabled(boolean dimBehindEnabled) {
            this.dimBehindEnabled = dimBehindEnabled;
            return this;
        }

        public Builder setSaveTextBold(@Nullable Boolean saveTextBold) {
            this.saveTextBold = saveTextBold;
            return this;
        }

        public Builder setButtonBackgroundColor(@ColorInt @Nullable Integer buttonBackgroundColor) {
            this.buttonBackgroundColor = buttonBackgroundColor;
            return this;
        }

        public Builder setRippleColor(@ColorInt @Nullable Integer rippleColor) {
            this.rippleColor = rippleColor;
            return this;
        }

        public ImageSaveDialogConfig build() {
            return new ImageSaveDialogConfig(this);
        }
    }
}
