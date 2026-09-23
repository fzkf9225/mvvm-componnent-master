package io.coderf.arklab.common.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
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
 * 横屏适配（只调宽度/密度，不改弹出位置）：
 * <ul>
 *   <li>inflate 经 {@link #inflateWithHostAdapt} 跟随宿主 CustomAdapt 密度</li>
 *   <li>居中弹窗未 {@link #setWidthRatio} 时默认宽 2/5（竖屏仍 4/5）</li>
 *   <li>底部 Sheet 横屏约 1/2 宽，gravity 与竖屏一致（BOTTOM 仍贴底）</li>
 *   <li>横屏左侧 DisplayCutout 会使水平居中（含仅 BOTTOM、未指定水平方向）落在安全区中心而偏右，
 *       在 {@link #applyWindow} / {@link #applyWindowLayout} 与 {@link #show()} 中用
 *       {@code lp.x = -(left-right)/2} 拉回全屏几何中心</li>
 * </ul>
 *
 * @author fz
 * @version 1.4
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
     * 读取当前 Activity 窗口的左右 systemBars / displayCutout inset（px）。
     * API &lt; 30 或拿不到 Activity 时返回 {0, 0}。
     *
     * @return int[2]：{leftInset, rightInset}
     */
    @NonNull
    protected final int[] resolveHorizontalSystemInsets() {
        int left = 0;
        int right = 0;
        Activity activity = DialogHostAdaptHelper.activityFrom(getContext());
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics wm = activity.getWindowManager().getCurrentWindowMetrics();
            android.graphics.Insets sys = wm.getWindowInsets().getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            left = sys.left;
            right = sys.right;
        }
        return new int[]{left, right};
    }

    /**
     * 当前用于算弹窗宽度的全屏宽（优先 WindowMetrics.bounds，否则 DisplayMetrics.widthPixels）。
     */
    protected final int resolveScreenWidthPx() {
        Activity activity = DialogHostAdaptHelper.activityFrom(getContext());
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.getWindowManager().getCurrentWindowMetrics().getBounds().width();
        }
        return getContext().getApplicationContext().getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 当前用于算弹窗高度的全屏高。
     */
    protected final int resolveScreenHeightPx() {
        Activity activity = DialogHostAdaptHelper.activityFrom(getContext());
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.getWindowManager().getCurrentWindowMetrics().getBounds().height();
        }
        return getContext().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
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
     * 宽度按全屏宽；水平 cutout 补偿由 {@link #applyWindow} 统一处理。
     */
    protected void applyCenterWindow(int widthNumerator, int widthDenominator) {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (widthNumerator > 0 && widthDenominator > 0) {
            width = resolveScreenWidthPx() * widthNumerator / widthDenominator;
        }
        applyWindow(Gravity.CENTER, width, ViewGroup.LayoutParams.WRAP_CONTENT, resolveBackgroundDrawable());
    }

    /**
     * 居中弹窗，同时指定宽、高占屏幕比例（高度为 WRAP_CONTENT 时传 0）。
     */
    protected void applyCenterWindow(int widthNumerator, int widthDenominator,
                                     int heightNumerator, int heightDenominator) {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (widthNumerator > 0 && widthDenominator > 0) {
            width = resolveScreenWidthPx() * widthNumerator / widthDenominator;
        }
        if (heightNumerator > 0 && heightDenominator > 0) {
            height = resolveScreenHeightPx() * heightNumerator / heightDenominator;
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
     * 横屏自动收窄宽度；未指定水平方向时补 {@link Gravity#CENTER_HORIZONTAL}，避免半宽贴左。
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
     * 底部 Sheet 弹窗。横屏自动约 1/2 宽。
     * CENTER 用四周圆角；BOTTOM 等仍用顶部圆角 Sheet 背景。
     * 横屏未指定水平方向时补 {@link Gravity#CENTER_HORIZONTAL}，并由 {@link #applyWindow} 做 cutout 补偿。
     */
    protected void applyBottomSheetWindow(int gravity, float topCornerRadiusDp) {
        if (isLandscape()) {
            int effectiveGravity = resolveLandscapeSheetGravity(gravity);
            Drawable background = gravity == Gravity.CENTER
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
        return resolveScreenWidthPx()
                * LANDSCAPE_SHEET_WIDTH_NUMERATOR / LANDSCAPE_SHEET_WIDTH_DENOMINATOR;
    }

    /**
     * 横屏收窄宽度后的有效 gravity：若未指定水平方向（如仅 BOTTOM），补上
     * {@link Gravity#CENTER_HORIZONTAL}，避免半宽弹窗贴左；已指定 LEFT/RIGHT/CENTER_HORIZONTAL 则保持。
     */
    protected final int resolveLandscapeSheetGravity(int gravity) {
        int horizontal = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        if (horizontal == 0 || horizontal == Gravity.FILL_HORIZONTAL) {
            return gravity | Gravity.CENTER_HORIZONTAL;
        }
        return gravity;
    }

    /**
     * 系统会把「未指定水平方向」（如仅 {@link Gravity#BOTTOM}）和
     * {@link Gravity#CENTER_HORIZONTAL} 都放在扣掉 DisplayCutout 后的安全区里水平居中。
     * 这两种都需要 cutout 补偿；显式 LEFT/RIGHT/START/END 不补偿。
     */
    private static boolean needsHorizontalCutoutCompensation(int gravity) {
        int horizontal = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        return horizontal == 0 || horizontal == Gravity.CENTER_HORIZONTAL;
    }

    /**
     * 横屏左侧 cutout 会让水平居中相对全屏几何中心右偏 {@code (left-right)/2}。
     * 用 {@code lp.x} 拉回，不改变垂直弹出位置。
     */
    protected final void applyHorizontalCutoutOffset(@NonNull WindowManager.LayoutParams lp) {
        if (!needsHorizontalCutoutCompensation(lp.gravity)) {
            return;
        }
        int[] insets = resolveHorizontalSystemInsets();
        lp.x = -(insets[0] - insets[1]) / 2;
    }

    /**
     * 仅设置 Window 的 layout 与 gravity，不改动背景。
     * 水平居中（含未指定水平方向的 BOTTOM）时补偿 DisplayCutout 偏移。
     */
    protected void applyWindowLayout(int gravity, int width, int height) {
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = gravity;
        lp.x = 0;
        lp.y = 0;
        lp.width = width;
        lp.height = height;
        applyHorizontalCutoutOffset(lp);
        dialogWindow.setAttributes(lp);
    }

    /**
     * 设置 Window layout、gravity 与背景。
     * 水平居中（含未指定水平方向的 BOTTOM）时补偿 DisplayCutout 偏移。
     */
    protected void applyWindow(int gravity, int width, int height, @NonNull Drawable background) {
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = gravity;
        lp.x = 0;
        lp.y = 0;
        lp.width = width;
        lp.height = height;
        applyHorizontalCutoutOffset(lp);
        dialogWindow.setAttributes(lp);
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
        // builder 之后子类可能改过 gravity/width，弹出前再补一次，避免 cutout 补偿被盖掉
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            applyHorizontalCutoutOffset(lp);
            dialogWindow.setAttributes(lp);
        }
        super.show();
    }
}
