package io.coderf.arklab.media.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

/**
 * commonmedia Dialog 基类：统一横屏 Window 尺寸，与 core-base {@code BaseDialog} 行为对齐，
 * 但不依赖其他模块。
 * <p>
 * 横屏适配：
 * <ul>
 *   <li>inflate 经 {@link #inflateWithHostAdapt} 可选同步宿主 AutoSize 密度</li>
 *   <li>居中弹窗未 {@link #setWidthRatio} 时默认宽 2/5（竖屏仍 4/5）</li>
 *   <li>底部 Sheet：横屏约 1/2 宽，垂直位置不变（BOTTOM 仍贴底），并补水平居中</li>
 *   <li>横屏左侧 DisplayCutout 会使水平居中落在安全区中心而偏右，
 *       在 {@link #applyWindowLayout} 与 {@link #show()} 中用 {@code lp.x = -(left-right)/2} 拉回全屏几何中心</li>
 * </ul>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/9/23
 * @updated 2026/9/23
 */
public abstract class MediaBaseDialog extends Dialog {

    /** 竖屏居中弹窗默认宽度占屏幕比例：4/5 */
    protected static final int DEFAULT_WIDTH_NUMERATOR = 4;
    protected static final int DEFAULT_WIDTH_DENOMINATOR = 5;

    /** 横屏居中弹窗默认宽度占屏幕比例：2/5（未 setWidthRatio 时） */
    protected static final int LANDSCAPE_WIDTH_NUMERATOR = 2;
    protected static final int LANDSCAPE_WIDTH_DENOMINATOR = 5;

    /** 横屏底部/选择类弹窗默认宽度占屏幕比例：1/2 */
    protected static final int LANDSCAPE_SHEET_WIDTH_NUMERATOR = 1;
    protected static final int LANDSCAPE_SHEET_WIDTH_DENOMINATOR = 2;

    protected final LayoutInflater layoutInflater;
    protected boolean outSide = true;

    /** 居中弹窗宽度分子/分母；未自定义时竖屏 4/5、横屏 2/5 */
    protected int widthNumerator = DEFAULT_WIDTH_NUMERATOR;
    protected int widthDenominator = DEFAULT_WIDTH_DENOMINATOR;
    /** 是否调用过 {@link #setWidthRatio}，为 true 时不再自动套横屏默认宽 */
    private boolean widthRatioCustomized = false;

    public MediaBaseDialog(@NonNull Context context, @StyleRes int themeResId) {
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
     * 横屏 inflate 前可选同步宿主密度；竖屏不改密度。
     * 子类请用本方法替代直接 {@code XxxBinding.inflate(...)}。
     */
    @NonNull
    protected final <T> T inflateWithHostAdapt(
            @NonNull MediaDialogHostAdaptHelper.InflateAction<T> inflate) {
        return MediaDialogHostAdaptHelper.inflateWithHostAdapt(getContext(), inflate);
    }

    /** 当前是否横屏。 */
    protected final boolean isLandscape() {
        return getContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
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
        Activity activity = MediaDialogHostAdaptHelper.activityFrom(getContext());
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
        Activity activity = MediaDialogHostAdaptHelper.activityFrom(getContext());
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return activity.getWindowManager().getCurrentWindowMetrics().getBounds().width();
        }
        return getContext().getApplicationContext().getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 自定义居中弹窗宽度占屏比。设置后不再自动套用横屏默认 2/5。
     * 竖屏默认 4/5；未调用时横屏自动 2/5。
     */
    public MediaBaseDialog setWidthRatio(int widthNumerator, int widthDenominator) {
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
     * 宽度按全屏宽；水平 cutout 补偿由 {@link #applyWindowLayout} 统一处理。
     */
    protected void applyCenterWindow(int widthNumerator, int widthDenominator) {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (widthNumerator > 0 && widthDenominator > 0) {
            width = resolveScreenWidthPx() * widthNumerator / widthDenominator;
        }
        applyWindowLayout(Gravity.CENTER, width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 底部全宽弹窗 layout。横屏自动收窄宽度，仍从底部弹出。
     */
    protected void applyBottomWindowLayout() {
        applyBottomWindowLayout(Gravity.BOTTOM);
    }

    /**
     * 底部弹窗 layout，可指定 gravity。
     * 横屏自动收窄宽度（约 1/2），垂直位置保持原 gravity（BOTTOM 仍贴底）。
     * 未指定水平方向时补 {@link Gravity#CENTER_HORIZONTAL}，避免半宽贴左；
     * 水平居中由 {@link #applyWindowLayout} 做 DisplayCutout 补偿。
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

    /** 横屏 Sheet / 选择类弹窗宽度（约屏宽 1/2）。 */
    protected final int resolveLandscapeSheetWidthPx() {
        return resolveScreenWidthPx()
                * LANDSCAPE_SHEET_WIDTH_NUMERATOR / LANDSCAPE_SHEET_WIDTH_DENOMINATOR;
    }

    /**
     * 横屏收窄宽度后的有效 gravity：若未指定水平方向（如仅 BOTTOM），补上
     * {@link Gravity#CENTER_HORIZONTAL}，避免半宽弹窗贴左；已指定 LEFT/RIGHT/CENTER_HORIZONTAL 则保持。
     * 不改变垂直方向。
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
     * 设置是否允许点击外部 / 返回键取消。
     */
    protected void applyCancelableOutside(boolean cancelable) {
        this.outSide = cancelable;
        setCancelable(cancelable);
        setCanceledOnTouchOutside(cancelable);
    }

    public MediaBaseDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    @Override
    public void show() {
        if (!canShow(getContext())) {
            return;
        }
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            applyHorizontalCutoutOffset(lp);
            dialogWindow.setAttributes(lp);
        }
        super.show();
    }
}
