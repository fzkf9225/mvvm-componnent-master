package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 圆角背景 + 清除按钮的输入框，不包含搜索相关能力。
 * <p>
 * 会保留 XML 中通过 {@code android:drawableStart} / {@code android:drawableLeft}
 * 设置的左侧图标，不会被清除按钮逻辑覆盖。
 * <p>
 * 可通过自定义属性控制：
 * <ul>
 *   <li>{@code app:startIconSize} — 左侧图标宽高（0 表示 intrinsic）</li>
 *   <li>{@code app:clearIconSize} — 清除图标宽高</li>
 *   <li>{@code app:iconPadding} — 图标与文字间距（compoundDrawablePadding）</li>
 *   <li>{@code app:bgColor / radius / stroke*} — 圆角背景</li>
 * </ul>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/7/13 9:25
 */
public class ClearableEditText extends TextInputEditText implements TextWatcher {

    private final String TAG = getClass().getSimpleName();

    /** 右侧清除图标 */
    private Drawable drawableClear;
    /** 左侧起始图标（来自 XML drawableStart 或代码设置） */
    private Drawable drawableStart;
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private boolean enableBgStyle = true;

    /** 左侧图标尺寸（px），0 表示使用 intrinsic */
    private int startIconSizePx;
    /** 清除图标尺寸（px），0 表示使用 intrinsic */
    private int clearIconSizePx;
    /** 图标与文字间距（px） */
    private int iconPaddingPx;

    private OnClearListener onClearListener;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText, defStyleAttr, 0);
        if (ta != null) {
            // app:startIcon 优先
            drawableStart = ta.getDrawable(R.styleable.ClearableEditText_startIcon);
            drawableClear = ta.getDrawable(R.styleable.ClearableEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.ClearableEditText_enableBgStyle, true);
            strokeColor = ta.getColor(R.styleable.ClearableEditText_strokeColor, ThemeAttrs.surface(context));
            circleBackColor = ta.getColor(R.styleable.ClearableEditText_bgColor, ThemeAttrs.surface(context));
            strokeWidth = ta.getDimension(R.styleable.ClearableEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.ClearableEditText_radius, 0);
            startIconSizePx = (int) ta.getDimension(R.styleable.ClearableEditText_startIconSize, 0);
            clearIconSizePx = (int) ta.getDimension(R.styleable.ClearableEditText_clearIconSize, 0);
            iconPaddingPx = (int) ta.getDimension(R.styleable.ClearableEditText_iconPadding, -1);
            ta.recycle();
        } else {
            strokeColor = ThemeAttrs.surface(context);
            circleBackColor = ThemeAttrs.surface(context);
            iconPaddingPx = -1;
        }

        // 未设置 app:startIcon 时，从 android:drawableStart / drawableLeft 读取
        if (drawableStart == null && attrs != null) {
            drawableStart = resolveAndroidStartDrawable(context, attrs);
        }
        // 再兜底：super 已解析的 compound drawable
        if (drawableStart == null) {
            Drawable[] existing = getCompoundDrawablesRelative();
            if (existing[0] != null) {
                drawableStart = existing[0];
            } else {
                Drawable[] absolute = getCompoundDrawables();
                if (absolute[0] != null) {
                    drawableStart = absolute[0];
                }
            }
        }
        if (drawableStart != null) {
            drawableStart = drawableStart.mutate();
        }

        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(context, R.mipmap.icon_clear);
        }
        if (drawableClear != null) {
            drawableClear = drawableClear.mutate();
        }

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);

        // 仅当 XML 未指定水平 padding 时才用默认 8dp
        if (getPaddingLeft() == 0 && getPaddingRight() == 0
                && getPaddingStart() == 0 && getPaddingEnd() == 0) {
            int defaultPad = DensityUtil.dp2px(context, 8);
            setPadding(defaultPad, getPaddingTop(), defaultPad, getPaddingBottom());
        }

        if (iconPaddingPx < 0) {
            iconPaddingPx = DensityUtil.dp2px(context, 8);
        }
        setCompoundDrawablePadding(iconPaddingPx);

        addTextChangedListener(this);
        updateCompoundDrawables(getText());
        applyBackgroundIfNeeded();
    }

    /**
     * 从 AttributeSet 显式读取 android:drawableStart / android:drawableLeft。
     * Material/AppCompat 有时不会在 super 构造完成后立刻把 compound drawable 设好，
     * 直接读 attr 更可靠。
     */
    @Nullable
    private static Drawable resolveAndroidStartDrawable(Context context, AttributeSet attrs) {
        final int[] attrsIds = new int[]{
                android.R.attr.drawableStart,
                android.R.attr.drawableLeft
        };
        TypedArray a = context.obtainStyledAttributes(attrs, attrsIds);
        Drawable d = a.getDrawable(0);
        if (d == null) {
            d = a.getDrawable(1);
        }
        a.recycle();
        return d;
    }

    /**
     * 按当前尺寸约束缩放 Drawable，返回可用于 setCompoundDrawables 的实例。
     * sizePx == 0 时保持 intrinsic 尺寸。
     */
    @Nullable
    private Drawable sizeDrawable(@Nullable Drawable src, int sizePx) {
        if (src == null) {
            return null;
        }
        Drawable d = src.mutate();
        if (sizePx > 0) {
            d.setBounds(0, 0, sizePx, sizePx);
        } else {
            int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : DensityUtil.dp2px(getContext(), 24);
            int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : DensityUtil.dp2px(getContext(), 24);
            d.setBounds(0, 0, w, h);
        }
        return d;
    }

    private void updateCompoundDrawables(@Nullable CharSequence text) {
        boolean showClear = text != null && !StringUtil.isEmpty(text.toString());
        Drawable start = sizeDrawable(drawableStart, startIconSizePx);
        Drawable end = showClear ? sizeDrawable(drawableClear, clearIconSizePx) : null;
        // 使用 Relative 版本，RTL 友好；不再用 WithIntrinsicBounds 以免忽略我们设置的 bounds
        setCompoundDrawablesRelative(start, null, end, null);
    }

    private void applyBackgroundIfNeeded() {
        if (!enableBgStyle) {
            return;
        }
        applyBackground();
    }

    private void applyBackground() {
        setBackground(CornerShapeHelper.createBackground(
                radius, radius, radius, radius,
                true, circleBackColor, strokeWidth > 0, strokeWidth, strokeColor));
    }

    private boolean isTouchOnClearButton(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        CharSequence text = getText();
        if (text == null || text.toString().isEmpty()) {
            return false;
        }
        // 与修改前一致：用 compoundPaddingRight 判断右侧清除区域
        int xDown = (int) event.getX();
        return xDown >= (getWidth() - getCompoundPaddingRight() * 2) && xDown < getWidth();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            updateCompoundDrawables(s);
        } catch (Exception e) {
            LogUtil.logger(TAG, "onTextChanged:" + e);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (isTouchOnClearButton(event)) {
                setText("");
                if (onClearListener != null) {
                    onClearListener.onClear();
                }
                performClick();
                return false;
            }
        } catch (Exception e) {
            LogUtil.logger(TAG, "onTouchEvent:" + e);
        }
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnClearListener(OnClearListener onClearListener) {
        this.onClearListener = onClearListener;
    }

    public interface OnClearListener {
        void onClear();
    }

    /**
     * 设置左侧起始图标（会覆盖 XML drawableStart）。
     */
    public void setStartDrawable(@Nullable Drawable drawable) {
        this.drawableStart = drawable != null ? drawable.mutate() : null;
        updateCompoundDrawables(getText());
    }

    /**
     * 设置左侧图标尺寸（px）。传 0 恢复 intrinsic。
     */
    public void setStartIconSize(int sizePx) {
        this.startIconSizePx = Math.max(0, sizePx);
        updateCompoundDrawables(getText());
    }

    /**
     * 设置清除图标尺寸（px）。传 0 恢复 intrinsic。
     */
    public void setClearIconSize(int sizePx) {
        this.clearIconSizePx = Math.max(0, sizePx);
        updateCompoundDrawables(getText());
    }

    /**
     * 设置图标与文字间距（px）。
     */
    public void setIconPadding(int paddingPx) {
        this.iconPaddingPx = Math.max(0, paddingPx);
        setCompoundDrawablePadding(this.iconPaddingPx);
    }

    public void setEnableBgStyle(boolean enableBgStyle) {
        this.enableBgStyle = enableBgStyle;
        applyBackgroundIfNeeded();
    }

    public void setBackColor(@ColorInt int color) {
        if (!enableBgStyle) {
            return;
        }
        this.circleBackColor = color;
        applyBackground();
    }

    public void setStroke(int strokeWidth, int color) {
        if (!enableBgStyle) {
            return;
        }
        this.strokeColor = color;
        this.strokeWidth = strokeWidth;
        applyBackground();
    }

    public void setBgColor(int color) {
        if (!enableBgStyle) {
            return;
        }
        this.circleBackColor = color;
        applyBackground();
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        if (!enableBgStyle) {
            return;
        }
        /** 仅兼容旧 {@link #setGradientDrawable} 入参，不再作为实际 background。 */
        if (gradientDrawable == null) {
            return;
        }
        float[] radii = new float[4];
        int[] fill = new int[1];
        boolean[] hasFill = new boolean[1];
        CornerShapeHelper.copyFromGradient(gradientDrawable, radii, fill, hasFill);
        radius = radii[0];
        if (hasFill[0]) {
            circleBackColor = fill[0];
        }
        applyBackground();
    }

    public void setRadius(float radius) {
        if (!enableBgStyle) {
            return;
        }
        this.radius = radius;
        applyBackground();
    }

    public void setBgColorAndRadius(int color, float radius) {
        if (!enableBgStyle) {
            return;
        }
        this.radius = radius;
        this.circleBackColor = color;
        applyBackground();
    }

    public boolean isEnableBgStyle() {
        return enableBgStyle;
    }

    public float getRadius() {
        return radius;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public int getCircleBackColor() {
        return circleBackColor;
    }
}
