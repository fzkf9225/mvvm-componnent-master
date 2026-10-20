package io.coderf.arklab.common.widget.feedback;

import android.annotation.SuppressLint;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.ExperimentalBadgeUtils;

/**
 * 角标样式配置。未显式设置的项保持官方默认或主题色：
 * 背景/文字颜色取 {@code ThemeAttrs.error/onPrimary}，文字大小取主题 badgeStyle（默认 12sp），
 * 位置偏移取官方默认 4dp。
 *
 * <p>注意：{@code textSizeSp} 依赖反射设置 paint 字号（官方未公开对应 API），
 * 若反射失败会静默回退默认字号；需要完整控制内边距/圆角时，
 * 请通过主题 {@code badgeStyle} 或 {@code BadgeDrawable#createFromResource} 定制。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/20 14:20
 */
@ExperimentalBadgeUtils
public final class BadgeConfig {

    /** 颜色哨兵：0 视为未设置（角标背景设为透明属边缘场景，可拿到 BadgeDrawable 后自行设置）。 */
    static final int UNSET_COLOR = 0;
    /** 字号哨兵：&lt;=0 视为未设置。 */
    static final float UNSET_TEXT_SIZE_SP = -1f;
    /** 偏移哨兵：视为未设置，沿用官方默认 4dp。 */
    static final int UNSET_OFFSET_DP = Integer.MIN_VALUE;
    /** 上限哨兵：&lt;=0 视为未设置。 */
    static final int UNSET_MAX = -1;

    /** 角标相对锚点的位置，默认 {@link BadgeDrawable#TOP_END}（右上角，RTL 自动翻转）。 */
    final int badgeGravity;
    /** 角标背景色，{@link #UNSET_COLOR} 表示用主题错误色。 */
    @ColorInt final int backgroundColor;
    /** 角标文字颜色，{@link #UNSET_COLOR} 表示用主题 onPrimary。 */
    @ColorInt final int textColor;
    /** 文字大小（sp），{@link #UNSET_TEXT_SIZE_SP} 表示用默认值。 */
    final float textSizeSp;
    /** 文字样式资源（textAppearance），非 0 时优先于 {@code textSizeSp}。 */
    @StyleRes final int textAppearanceRes;
    /** 水平偏移（dp），{@link #UNSET_OFFSET_DP} 表示沿用官方默认。 */
    final int horizontalOffsetDp;
    /** 垂直偏移（dp），{@link #UNSET_OFFSET_DP} 表示沿用官方默认。 */
    final int verticalOffsetDp;
    /** 数字上限，超过显示 {@code maxNumber+}；{@link #UNSET_MAX} 表示不限制。 */
    final int maxNumber;
    /** 文字/数字最大字符数，超出截断；{@link #UNSET_MAX} 表示不设置。 */
    final int maxCharacterCount;

    private BadgeConfig(Builder builder) {
        this.badgeGravity = builder.badgeGravity;
        this.backgroundColor = builder.backgroundColor;
        this.textColor = builder.textColor;
        this.textSizeSp = builder.textSizeSp;
        this.textAppearanceRes = builder.textAppearanceRes;
        this.horizontalOffsetDp = builder.horizontalOffsetDp;
        this.verticalOffsetDp = builder.verticalOffsetDp;
        this.maxNumber = builder.maxNumber;
        this.maxCharacterCount = builder.maxCharacterCount;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /** BadgeConfig 建造者，所有方法均可链式调用。 */
    public static final class Builder {

        private int badgeGravity = BadgeDrawable.TOP_END;
        @ColorInt private int backgroundColor = UNSET_COLOR;
        @ColorInt private int textColor = UNSET_COLOR;
        private float textSizeSp = UNSET_TEXT_SIZE_SP;
        @StyleRes private int textAppearanceRes = 0;
        private int horizontalOffsetDp = UNSET_OFFSET_DP;
        private int verticalOffsetDp = UNSET_OFFSET_DP;
        private int maxNumber = UNSET_MAX;
        private int maxCharacterCount = UNSET_MAX;

        private Builder() {
        }

        /** 角标位置，取值见 {@link BadgeDrawable#TOP_END} / {@link BadgeDrawable#TOP_START} 等。 */
        @NonNull
        public Builder badgeGravity(int gravity) {
            this.badgeGravity = gravity;
            return this;
        }

        /** 角标背景色，不设置时用主题错误色。 */
        @NonNull
        public Builder backgroundColor(@ColorInt int color) {
            this.backgroundColor = color;
            return this;
        }

        /** 角标文字颜色，不设置时用主题 onPrimary。 */
        @NonNull
        public Builder textColor(@ColorInt int color) {
            this.textColor = color;
            return this;
        }

        /** 角标文字大小（sp 具体值），不设置时用默认 12sp。 */
        @NonNull
        public Builder textSizeSp(@SuppressLint("SupportAnnotationUsage") @IntRange(from = 1) float sp) {
            this.textSizeSp = sp;
            return this;
        }

        /** 角标文字样式（textAppearance 资源），设置后优先于 {@link #textSizeSp(float)}。 */
        @NonNull
        public Builder textAppearance(@StyleRes int textAppearanceRes) {
            this.textAppearanceRes = textAppearanceRes;
            return this;
        }

        /** 相对锚点的水平偏移（dp），不设置时沿用官方默认 4dp。 */
        @NonNull
        public Builder horizontalOffsetDp(int dp) {
            this.horizontalOffsetDp = dp;
            return this;
        }

        /** 相对锚点的垂直偏移（dp），不设置时沿用官方默认 4dp。 */
        @NonNull
        public Builder verticalOffsetDp(int dp) {
            this.verticalOffsetDp = dp;
            return this;
        }

        /** 数字上限，超过后显示 {@code maxNumber+}；不设置时按最大字符数截断。 */
        @NonNull
        public Builder maxNumber(@IntRange(from = 1) int max) {
            this.maxNumber = max;
            return this;
        }

        /** 最大字符数（数字与文字共用），超出截断；不设置时用官方默认 4。 */
        @NonNull
        public Builder maxCharacterCount(@IntRange(from = 1) int max) {
            this.maxCharacterCount = max;
            return this;
        }

        @NonNull
        public BadgeConfig build() {
            return new BadgeConfig(this);
        }
    }
}
