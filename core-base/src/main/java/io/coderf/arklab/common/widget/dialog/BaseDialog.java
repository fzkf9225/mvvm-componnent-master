package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

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
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/7/13 10:00
 * @updated 2026/9/21
 */
public abstract class BaseDialog extends Dialog {

    /** 默认宽度占屏幕比例：4/5 */
    protected static final int DEFAULT_WIDTH_NUMERATOR = 4;
    protected static final int DEFAULT_WIDTH_DENOMINATOR = 5;

    /** 居中弹窗默认圆角（dp）。M3 规范为 28dp；本工程用 16dp，避免过圆。 */
    protected static final float DEFAULT_DIALOG_CORNER_DP = 16f;

    /** 底部 Sheet 顶部圆角（dp），与居中弹窗一致。 */
    protected static final float DEFAULT_SHEET_TOP_CORNER_DP = 16f;

    protected final LayoutInflater layoutInflater;
    protected boolean outSide = true;
    @Nullable
    protected Drawable bgDrawable;

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
     * 应用居中弹窗 Window：默认宽度为屏幕 4/5。
     */
    protected void applyCenterWindow() {
        applyCenterWindow(DEFAULT_WIDTH_NUMERATOR, DEFAULT_WIDTH_DENOMINATOR);
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
     */
    protected void applyBottomWindowLayout(int gravity) {
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

    protected void applyBottomSheetWindow(int gravity, float topCornerRadiusDp) {
        applyWindow(
                gravity,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                resolveBottomSheetBackgroundDrawable(topCornerRadiusDp));
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
