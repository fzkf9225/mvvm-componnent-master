package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * Dialog 公共基类：统一 Window 尺寸、居中、圆角背景与外部点击取消配置，
 * 并在 {@link #show()} 前校验 Activity 是否仍可展示，降低 Window 泄漏风险。
 * <p>
 * 默认背景为 Material3 {@code surfaceContainerHigh} + 28dp 圆角（与官方 AlertDialog 接近）。
 * <p>
 * 横屏适配：
 * <ul>
 *   <li>inflate 经 {@link #inflateWithHostAdapt} 跟随宿主 CustomAdapt 密度</li>
 *   <li>居中弹窗未 {@link #setWidthRatio} 时默认宽 2/5（竖屏仍 4/5）</li>
 *   <li>底部 Sheet 未强制全宽时：横屏改为约 1/2 宽并居中，避免过宽与底部按钮被裁切</li>
 * </ul>
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @created 2026/7/13 10:00
 * @updated 2026/9/23
 */
public abstract class BaseDialog extends Dialog {

    /** 竖屏居中弹窗默认宽度占屏幕比例：4/5 */
    protected static final int DEFAULT_WIDTH_NUMERATOR = 4;
    protected static final int DEFAULT_WIDTH_DENOMINATOR = 5;

    /** 横屏居中弹窗默认宽度占屏幕比例：2/5（未 setWidthRatio 时） */
    protected static final int LANDSCAPE_WIDTH_NUMERATOR = 2;
    protected static final int LANDSCAPE_WIDTH_DENOMINATOR = 5;

    /** 横屏底部/选择类弹窗默认宽度占屏幕比例：1/2 */
    protected static final int LANDSCAPE_SHEET_WIDTH_NUMERATOR = 1;
    protected static final int LANDSCAPE_SHEET_WIDTH_DENOMINATOR = 2;

    /** 居中弹窗默认圆角（dp）。M3 规范为 28dp；本工程用 16dp，避免过圆。 */
    protected static final float DEFAULT_DIALOG_CORNER_DP = 16f;

    /** 底部 Sheet 顶部圆角（dp），与居中弹窗一致。 */
    protected static final float DEFAULT_SHEET_TOP_CORNER_DP = 16f;

    /** 横屏选择类内容区预留：标题 + 按钮栏等（dp） */
    protected static final float LANDSCAPE_CONTENT_RESERVED_DP = 140f;

    protected final LayoutInflater layoutInflater;
    protected boolean outSide = true;
    @Nullable
    protected Drawable bgDrawable;

    /** 居中弹窗宽度分子/分母；未自定义时竖屏 4/5、横屏 2/5 */
    protected int widthNumerator = DEFAULT_WIDTH_NUMERATOR;
    protected int widthDenominator = DEFAULT_WIDTH_DENOMINATOR;
    /** 是否调用过 {@link #setWidthRatio}，为 true 时不再自动套横屏默认宽 */
    private boolean widthRatioCustomized = false;

    public BaseDialog(@NonNull Context context) {
        this(context, R.style.ActionSheetDialogStyle);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 当前 Context 对应的 Activity 是否仍可用于展示 Dialog。
     */
    public static boolean canShow(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity activity) {
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    /**
     * 横屏 inflate 前同步宿主 CustomAdapt 密度；竖屏不改密度。
     * 子类请用本方法替代直接 {@code XxxBinding.inflate(...)}。
     */
    @NonNull
    protected final <T> T inflateWithHostAdapt(
            @NonNull DialogHostAdaptHelper.InflateAction<T> inflate) {
        return DialogHostAdaptHelper.inflateWithHostAdapt(getContext(), inflate);
    }

    /** 当前是否横屏。 */
    protected final boolean isLandscape() {
        return getContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 横屏下内容区可用最大高度（屏高减去预留的标题/按钮等），竖屏返回 -1。
     *
     * @param reservedDp 预留高度（dp），通常含标题 + 按钮栏
     */
    protected final int resolveLandscapeMaxContentHeightPx(float reservedDp) {
        if (!isLandscape()) {
            return -1;
        }
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int reserved = DensityUtil.dp2px(getContext(), reservedDp);
        int minH = DensityUtil.dp2px(getContext(), 80f);
        return Math.max(minH, metrics.heightPixels - reserved);
    }

    /**
     * 横屏时限制 {@link NumberPicker} 高度，避免底部按钮被顶出屏幕。
     */
    protected final void applyLandscapeNumberPickerHeight(@Nullable NumberPicker... pickers) {
        if (pickers == null || pickers.length == 0 || !isLandscape()) {
            return;
        }
        int maxH = resolveLandscapeMaxContentHeightPx(LANDSCAPE_CONTENT_RESERVED_DP);
        if (maxH <= 0) {
            return;
        }
        // NumberPicker 默认很高，横屏再压一层上限，保证按钮可见
        int cap = Math.min(maxH, DensityUtil.dp2px(getContext(), 160f));
        for (NumberPicker picker : pickers) {
            if (picker == null || picker.getVisibility() == View.GONE) {
                continue;
            }
            ViewGroup.LayoutParams lp = picker.getLayoutParams();
            if (lp == null) {
                continue;
            }
            lp.height = cap;
            picker.setLayoutParams(lp);
        }
    }

    /**
     * 自定义居中弹窗宽度占屏比。设置后不再自动套用横屏默认 2/5。
     * 竖屏默认 4/5；未调用时横屏自动 2/5。
     */
    public BaseDialog setWidthRatio(int widthNumerator, int widthDenominator) {
        if (widthNumerator > 0 && widthDenominator > 0) {
            this.widthNumerator = widthNumerator;
            this.widthDenominator = widthDenominator;
            this.widthRatioCustomized = true;
        }
        return this;
    }

    /**
     * 应用居中弹窗 Window：未自定义时竖屏 4/5、横屏 2/5。
     */
    protected void applyCenterWindow() {
        int num = widthNumerator;
        int den = widthDenominator;
        if (!widthRatioCustomized && isLandscape()) {
            num = LANDSCAPE_WIDTH_NUMERATOR;
            den = LANDSCAPE_WIDTH_DENOMINATOR;
        }
        applyCenterWindow(num, den);
    }

    /**
     * 应用居中弹窗 Window，自定义宽度比例（numerator / denominator）。
     */
    protected void applyCenterWindow(int widthNumerator, int widthDenominator) {
        DisplayMetrics metrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (metrics != null && widthNumerator > 0 && widthDenominator > 0) {
            width = metrics.widthPixels * widthNumerator / widthDenominator;
        }
        applyWindow(Gravity.CENTER, width, ViewGroup.LayoutParams.WRAP_CONTENT, resolveBackgroundDrawable());
    }

    /**
     * 居中弹窗，同时指定宽、高占屏幕比例（高度为 WRAP_CONTENT 时传 0）。
     */
    protected void applyCenterWindow(int widthNumerator, int widthDenominator,
                                     int heightNumerator, int heightDenominator) {
        DisplayMetrics metrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (metrics != null) {
            if (widthNumerator > 0 && widthDenominator > 0) {
                width = metrics.widthPixels * widthNumerator / widthDenominator;
            }
            if (heightNumerator > 0 && heightDenominator > 0) {
                height = metrics.heightPixels * heightNumerator / heightDenominator;
            }
        }
        applyWindow(Gravity.CENTER, width, height, resolveBackgroundDrawable());
    }

    /**
     * 底部全宽弹窗，仅设置 layout 与 gravity，不修改 Window 背景（适用于透明/自定义主题）。
     */
    protected void applyBottomWindowLayout() {
        applyBottomWindowLayout(Gravity.BOTTOM);
    }

    /**
     * 底部弹窗 layout，可指定 gravity（如 BOTTOM / CENTER 等）。
     * 横屏自动收窄宽度；原 BOTTOM 改为居中，避免全宽过扁。
     */
    protected void applyBottomWindowLayout(int gravity) {
        if (isLandscape()) {
            applyWindowLayout(
                    resolveLandscapeSheetGravity(gravity),
                    resolveLandscapeSheetWidthPx(),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            return;
        }
        applyWindowLayout(gravity, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 底部 Sheet 弹窗：全宽 + 默认顶部圆角白底（可被 {@link #bgDrawable} 覆盖）。
     */
    protected void applyBottomSheetWindow() {
        applyBottomSheetWindow(Gravity.BOTTOM);
    }

    /**
     * 底部 Sheet 弹窗，可指定 gravity 与顶部圆角半径 (dp)。
     */
    protected void applyBottomSheetWindow(int gravity) {
        applyBottomSheetWindow(gravity, DEFAULT_SHEET_TOP_CORNER_DP);
    }

    /**
     * 底部 Sheet 弹窗。横屏自动约 1/2 宽：BOTTOM 改居中并四周圆角，CENTER 保持居中并收窄。
     */
    protected void applyBottomSheetWindow(int gravity, float topCornerRadiusDp) {
        if (isLandscape()) {
            int effectiveGravity = resolveLandscapeSheetGravity(gravity);
            Drawable background = (gravity == Gravity.BOTTOM || gravity == Gravity.CENTER)
                    ? resolveCenteredSheetBackgroundDrawable()
                    : resolveBottomSheetBackgroundDrawable(topCornerRadiusDp);
            applyWindow(
                    effectiveGravity,
                    resolveLandscapeSheetWidthPx(),
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    background);
            return;
        }
        applyWindow(
                gravity,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                resolveBottomSheetBackgroundDrawable(topCornerRadiusDp));
    }

    /** 横屏 Sheet / 选择类弹窗宽度（约屏宽 1/2）。 */
    protected final int resolveLandscapeSheetWidthPx() {
        DisplayMetrics metrics =
                getContext().getApplicationContext().getResources().getDisplayMetrics();
        return metrics.widthPixels
                * LANDSCAPE_SHEET_WIDTH_NUMERATOR / LANDSCAPE_SHEET_WIDTH_DENOMINATOR;
    }

    /**
     * 横屏下 Sheet 的有效 gravity：BOTTOM 改为 CENTER，其它保持原值。
     */
    protected final int resolveLandscapeSheetGravity(int gravity) {
        return gravity == Gravity.BOTTOM ? Gravity.CENTER : gravity;
    }

    /**
     * 仅设置 Window 的 layout 与 gravity，不改动背景。
     */
    protected void applyWindowLayout(int gravity, int width, int height) {
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(width, height);
        dialogWindow.setGravity(gravity);
    }

    /**
     * 设置 Window layout、gravity 与背景。
     */
    protected void applyWindow(int gravity, int width, int height, @NonNull Drawable background) {
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(width, height);
        dialogWindow.setGravity(gravity);
        dialogWindow.setBackgroundDrawable(background);
    }

    @NonNull
    protected Drawable resolveBottomSheetBackgroundDrawable(float topCornerRadiusDp) {
        if (bgDrawable != null) {
            return bgDrawable;
        }
        float top = DensityUtil.dp2px(getContext(), topCornerRadiusDp);
        return CornerShapeHelper.createBackground(
                top, top, 0f, 0f,
                true, ThemeAttrs.surfaceContainerHigh(getContext()),
                false, 0f, 0);
    }

    /** 横屏居中展示的 Sheet 背景：四周圆角（与居中弹窗一致）。 */
    @NonNull
    protected Drawable resolveCenteredSheetBackgroundDrawable() {
        if (bgDrawable != null) {
            return bgDrawable;
        }
        float r = DensityUtil.dp2px(getContext(), DEFAULT_DIALOG_CORNER_DP);
        return CornerShapeHelper.createBackground(
                r, r, r, r,
                true, ThemeAttrs.surfaceContainerHigh(getContext()),
                false, 0f, 0);
    }

    /**
     * 设置是否允许点击外部 / 返回键取消。
     */
    protected void applyCancelableOutside(boolean cancelable) {
        this.outSide = cancelable;
        setCancelable(cancelable);
        setCanceledOnTouchOutside(cancelable);
    }

    /**
     * 居中弹窗默认背景：surfaceContainerHigh + M3 大圆角。
     */
    @NonNull
    protected Drawable resolveBackgroundDrawable() {
        if (bgDrawable != null) {
            return bgDrawable;
        }
        float r = DensityUtil.dp2px(getContext(), DEFAULT_DIALOG_CORNER_DP);
        return CornerShapeHelper.createBackground(
                r, r, r, r,
                true, ThemeAttrs.surfaceContainerHigh(getContext()),
                false, 0f, 0);
    }

    public BaseDialog setBgDrawable(@Nullable Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public BaseDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    @Override
    public void show() {
        if (!canShow(getContext())) {
            return;
        }
        super.show();
    }
}
