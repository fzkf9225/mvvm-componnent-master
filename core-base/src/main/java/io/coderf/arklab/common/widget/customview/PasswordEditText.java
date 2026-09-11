package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 密码输入框：圆角背景、明文/密文切换，可选清除按钮（可与切换按钮同时显示）。
 * 样式属性与 {@link ClearableEditText} 保持一致，便于 XML 复用。
 * <p>
 * 会保留 XML 中通过 {@code android:drawableStart} / {@code android:drawableLeft}
 * 设置的左侧图标，不会被右侧按钮逻辑覆盖。
 * <p>
 * 可通过自定义属性控制：
 * <ul>
 *   <li>{@code app:startIconSize} — 左侧图标宽高（0 表示 intrinsic）</li>
 *   <li>{@code app:clearIconSize} — 清除图标宽高</li>
 *   <li>{@code app:toggleIconSize} — 眼睛切换图标宽高</li>
 *   <li>{@code app:iconPadding} — 图标与文字间距</li>
 *   <li>{@code app:endIconGap} — 清除与切换按钮之间的间距</li>
 *   <li>{@code app:enableClear} — 是否同时显示清除按钮</li>
 * </ul>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/7/13 10:10
 */
public class PasswordEditText extends TextInputEditText implements TextWatcher {

    private static final int INDEX_DRAWABLE_END = 2;
    private static final int ZONE_NONE = 0;
    private static final int ZONE_CLEAR = 1;
    private static final int ZONE_TOGGLE = 2;

    private final String TAG = getClass().getSimpleName();

    private Drawable drawableVisible;
    private Drawable drawableInvisible;
    private Drawable drawableClear;
    /** 左侧起始图标（来自 XML drawableStart 或代码设置） */
    private Drawable drawableStart;
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private boolean enableBgStyle = true;
    private boolean passwordVisible = false;
    private boolean enableToggle = true;
    private boolean enableClear = false;
    private boolean showingClear = false;
    private boolean showingToggle = false;

    /** 左侧图标尺寸（px），0 = intrinsic */
    private int startIconSizePx;
    /** 清除图标尺寸（px），0 = intrinsic */
    private int clearIconSizePx;
    /** 切换图标尺寸（px），0 = intrinsic */
    private int toggleIconSizePx;
    /** 图标与文字间距 */
    private int iconPaddingPx;
    /** 清除与切换按钮间距 */
    private int endIconGapPx;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        endIconGapPx = DensityUtil.dp2px(context, 8);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText, defStyleAttr, 0);
        if (ta != null) {
            // app:startIcon 优先
            drawableStart = ta.getDrawable(R.styleable.PasswordEditText_startIcon);
            drawableVisible = ta.getDrawable(R.styleable.PasswordEditText_passwordVisibleIcon);
            drawableInvisible = ta.getDrawable(R.styleable.PasswordEditText_passwordInvisibleIcon);
            drawableClear = ta.getDrawable(R.styleable.PasswordEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.PasswordEditText_enableBgStyle, true);
            enableToggle = ta.getBoolean(R.styleable.PasswordEditText_enablePasswordToggle, true);
            enableClear = ta.getBoolean(R.styleable.PasswordEditText_enableClear, false);
            strokeColor = ta.getColor(R.styleable.PasswordEditText_strokeColor,
                    ThemeAttrs.surface(context));
            circleBackColor = ta.getColor(R.styleable.PasswordEditText_bgColor,
                    ThemeAttrs.surface(context));
            strokeWidth = ta.getDimension(R.styleable.PasswordEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.PasswordEditText_radius, 0);
            startIconSizePx = (int) ta.getDimension(R.styleable.PasswordEditText_startIconSize, 0);
            clearIconSizePx = (int) ta.getDimension(R.styleable.PasswordEditText_clearIconSize, 0);
            toggleIconSizePx = (int) ta.getDimension(R.styleable.PasswordEditText_toggleIconSize, 0);
            iconPaddingPx = (int) ta.getDimension(R.styleable.PasswordEditText_iconPadding, -1);
            endIconGapPx = (int) ta.getDimension(R.styleable.PasswordEditText_endIconGap, endIconGapPx);
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

        if (drawableVisible == null) {
            drawableVisible = ContextCompat.getDrawable(context, R.drawable.ic_password_visible);
        }
        if (drawableInvisible == null) {
            drawableInvisible = ContextCompat.getDrawable(context, R.drawable.ic_password_invisible);
        }
        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(context, R.mipmap.icon_clear);
        }
        mutateDrawables();

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);

        if (getPaddingLeft() == 0 && getPaddingRight() == 0
                && getPaddingStart() == 0 && getPaddingEnd() == 0) {
            int defaultPad = DensityUtil.dp2px(context, 8);
            setPadding(defaultPad, getPaddingTop(), defaultPad, getPaddingBottom());
        }

        if (iconPaddingPx < 0) {
            iconPaddingPx = DensityUtil.dp2px(context, 4);
        }
        setCompoundDrawablePadding(iconPaddingPx);

        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        applyPasswordVisibility(false);
        addTextChangedListener(this);
        updateRightDrawables(getText());
        applyBackgroundIfNeeded();
    }

    /**
     * 从 AttributeSet 显式读取 android:drawableStart / android:drawableLeft。
     * Material/AppCompat 有时不会在 super 构造完成后立刻把 compound drawable 设好。
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

    private void mutateDrawables() {
        if (drawableStart != null) {
            drawableStart = drawableStart.mutate();
        }
        if (drawableVisible != null) {
            drawableVisible = drawableVisible.mutate();
        }
        if (drawableInvisible != null) {
            drawableInvisible = drawableInvisible.mutate();
        }
        if (drawableClear != null) {
            drawableClear = drawableClear.mutate();
        }
    }

    /**
     * 仅用于左侧 start 图标：按 sizePx 设置 bounds；sizePx==0 时用 intrinsic。
     * 右侧清除/切换图标走原来的 WithIntrinsicBounds 逻辑，避免点击区域错乱。
     */
    @Nullable
    private Drawable prepareStartDrawable() {
        if (drawableStart == null) {
            return null;
        }
        Drawable d = drawableStart.mutate();
        if (startIconSizePx > 0) {
            d.setBounds(0, 0, startIconSizePx, startIconSizePx);
        } else {
            int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : DensityUtil.dp2px(getContext(), 24);
            int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : DensityUtil.dp2px(getContext(), 24);
            d.setBounds(0, 0, w, h);
        }
        return d;
    }

    private void applyPasswordVisibility(boolean visible) {
        passwordVisible = visible;
        int selection = getSelectionEnd();
        if (visible) {
            setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (selection >= 0) {
            int length = getText() == null ? 0 : getText().length();
            setSelection(Math.min(selection, length));
        }
        updateRightDrawables(getText());
    }

    /**
     * 右侧清除/眼睛逻辑与修改前保持一致（WithIntrinsicBounds），
     * 仅额外把左侧 start 图标一起设上。
     */
    private void updateRightDrawables(@Nullable CharSequence text) {
        Drawable toggle = null;
        Drawable clear = null;
        showingToggle = enableToggle;
        showingClear = enableClear && text != null && !StringUtil.isEmpty(text.toString());
        if (showingToggle) {
            toggle = passwordVisible ? drawableVisible : drawableInvisible;
        }
        if (showingClear) {
            clear = drawableClear;
        }
        Drawable endDrawable = buildEndDrawable(clear, toggle);
        Drawable start = prepareStartDrawable();

        // 右侧与原来一致：按 intrinsic 设 bounds，保证占位和点击区域正确
        if (endDrawable != null) {
            int ew = endDrawable.getIntrinsicWidth() > 0
                    ? endDrawable.getIntrinsicWidth()
                    : DensityUtil.dp2px(getContext(), 24);
            int eh = endDrawable.getIntrinsicHeight() > 0
                    ? endDrawable.getIntrinsicHeight()
                    : DensityUtil.dp2px(getContext(), 24);
            endDrawable.setBounds(0, 0, ew, eh);
        }
        setCompoundDrawablesRelative(start, null, endDrawable, null);
    }

    @Nullable
    private Drawable buildEndDrawable(@Nullable Drawable clear, @Nullable Drawable toggle) {
        if (clear != null && toggle != null) {
            return new HorizontalCompoundDrawable(clear, toggle, endIconGapPx);
        }
        if (toggle != null) {
            return toggle;
        }
        return clear;
    }

    private int getDrawableWidth(@NonNull Drawable drawable) {
        return drawable.getIntrinsicWidth() > 0
                ? drawable.getIntrinsicWidth()
                : DensityUtil.dp2px(getContext(), 24);
    }

    private int getDrawableHeight(@NonNull Drawable drawable) {
        return drawable.getIntrinsicHeight() > 0
                ? drawable.getIntrinsicHeight()
                : DensityUtil.dp2px(getContext(), 24);
    }

    /** 与修改前相同的右侧触摸判断 */
    private boolean isTouchOnEndDrawables(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return false;
        }
        Drawable[] drawables = getCompoundDrawablesRelative();
        if (drawables.length <= INDEX_DRAWABLE_END || drawables[INDEX_DRAWABLE_END] == null) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        Drawable end = drawables[INDEX_DRAWABLE_END];
        int drawableWidth = end.getBounds().width();
        if (drawableWidth <= 0) {
            drawableWidth = end.getIntrinsicWidth();
        }
        int drawableHeight = end.getBounds().height();
        if (drawableHeight <= 0) {
            drawableHeight = end.getIntrinsicHeight();
        }
        int drawableTop = (getHeight() - drawableHeight) / 2;
        int drawableBottom = drawableTop + drawableHeight;
        int drawableLeft = getWidth() - getPaddingEnd() - drawableWidth;
        return x >= drawableLeft && x <= getWidth() - getPaddingEnd()
                && y >= drawableTop && y <= drawableBottom;
    }

    private int hitEndDrawableZone(int touchX) {
        if (!showingToggle && !showingClear) {
            return ZONE_NONE;
        }
        Drawable endDrawable = getCompoundDrawablesRelative()[INDEX_DRAWABLE_END];
        if (endDrawable == null) {
            return ZONE_NONE;
        }
        int drawableWidth = endDrawable.getBounds().width();
        if (drawableWidth <= 0) {
            drawableWidth = endDrawable.getIntrinsicWidth();
        }
        int drawableLeft = getWidth() - getPaddingEnd() - drawableWidth;
        if (showingToggle && showingClear) {
            int clearWidth = getDrawableWidth(drawableClear);
            int toggleWidth = getDrawableWidth(passwordVisible ? drawableVisible : drawableInvisible);
            int toggleLeft = drawableLeft + clearWidth + endIconGapPx;
            if (touchX >= toggleLeft && touchX <= toggleLeft + toggleWidth) {
                return ZONE_TOGGLE;
            }
            if (touchX >= drawableLeft && touchX < toggleLeft) {
                return ZONE_CLEAR;
            }
            return ZONE_NONE;
        }
        if (showingToggle) {
            return ZONE_TOGGLE;
        }
        return ZONE_CLEAR;
    }

    private void applyBackgroundIfNeeded() {
        if (!enableBgStyle) {
            return;
        }
        setBackground(CornerShapeHelper.createBackground(
                radius, radius, radius, radius,
                true, circleBackColor, strokeWidth > 0, strokeWidth, strokeColor));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateRightDrawables(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (isTouchOnEndDrawables(event)) {
                int zone = hitEndDrawableZone((int) event.getX());
                if (zone == ZONE_TOGGLE) {
                    applyPasswordVisibility(!passwordVisible);
                    performClick();
                    return true;
                }
                if (zone == ZONE_CLEAR) {
                    setText("");
                    performClick();
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.logger(TAG, "onTouchEvent:" + e);
        }
        return super.onTouchEvent(event);
    }

    /** 设置左侧起始图标（覆盖 XML drawableStart）。 */
    public void setStartDrawable(@Nullable Drawable drawable) {
        this.drawableStart = drawable != null ? drawable.mutate() : null;
        updateRightDrawables(getText());
    }

    /** 左侧图标尺寸（px），0 恢复 intrinsic。 */
    public void setStartIconSize(int sizePx) {
        this.startIconSizePx = Math.max(0, sizePx);
        updateRightDrawables(getText());
    }

    /** 清除图标尺寸（px），0 恢复 intrinsic。 */
    public void setClearIconSize(int sizePx) {
        this.clearIconSizePx = Math.max(0, sizePx);
        updateRightDrawables(getText());
    }

    /** 眼睛切换图标尺寸（px），0 恢复 intrinsic。 */
    public void setToggleIconSize(int sizePx) {
        this.toggleIconSizePx = Math.max(0, sizePx);
        updateRightDrawables(getText());
    }

    /** 图标与文字间距（px）。 */
    public void setIconPadding(int paddingPx) {
        this.iconPaddingPx = Math.max(0, paddingPx);
        setCompoundDrawablePadding(this.iconPaddingPx);
    }

    /** 清除与切换按钮之间的间距（px）。 */
    public void setEndIconGap(int gapPx) {
        this.endIconGapPx = Math.max(0, gapPx);
        updateRightDrawables(getText());
    }

    public void setEnablePasswordToggle(boolean enableToggle) {
        this.enableToggle = enableToggle;
        updateRightDrawables(getText());
    }

    public void setEnableClear(boolean enableClear) {
        this.enableClear = enableClear;
        updateRightDrawables(getText());
    }

    public void setPasswordVisibleIcon(@DrawableRes int resId) {
        drawableVisible = ContextCompat.getDrawable(getContext(), resId);
        if (drawableVisible != null) {
            drawableVisible = drawableVisible.mutate();
        }
        updateRightDrawables(getText());
    }

    public void setPasswordInvisibleIcon(@DrawableRes int resId) {
        drawableInvisible = ContextCompat.getDrawable(getContext(), resId);
        if (drawableInvisible != null) {
            drawableInvisible = drawableInvisible.mutate();
        }
        updateRightDrawables(getText());
    }

    public void setEnableBgStyle(boolean enableBgStyle) {
        this.enableBgStyle = enableBgStyle;
        applyBackgroundIfNeeded();
    }

    public void setBgColor(@ColorInt int color) {
        this.circleBackColor = color;
        applyBackgroundIfNeeded();
    }

    public void setRadius(float radius) {
        this.radius = radius;
        applyBackgroundIfNeeded();
    }

    public boolean isPasswordVisible() {
        return passwordVisible;
    }

    /**
     * 将两个 Drawable 横向拼接为一个 end compound drawable（与修改前一致，按 intrinsic 尺寸）。
     */
    private static final class HorizontalCompoundDrawable extends Drawable {

        private final Drawable startDrawable;
        private final Drawable endDrawable;
        private final int gapPx;
        private final int intrinsicWidth;
        private final int intrinsicHeight;

        HorizontalCompoundDrawable(@NonNull Drawable startDrawable, @NonNull Drawable endDrawable, int gapPx) {
            this.startDrawable = startDrawable.mutate();
            this.endDrawable = endDrawable.mutate();
            this.gapPx = gapPx;
            int startW = startDrawable.getIntrinsicWidth() > 0 ? startDrawable.getIntrinsicWidth() : 0;
            int endW = endDrawable.getIntrinsicWidth() > 0 ? endDrawable.getIntrinsicWidth() : 0;
            int startH = startDrawable.getIntrinsicHeight() > 0 ? startDrawable.getIntrinsicHeight() : 0;
            int endH = endDrawable.getIntrinsicHeight() > 0 ? endDrawable.getIntrinsicHeight() : 0;
            intrinsicWidth = startW + gapPx + endW;
            intrinsicHeight = Math.max(startH, endH);
        }

        @Override
        public int getIntrinsicWidth() {
            return intrinsicWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return intrinsicHeight;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int left = getBounds().left;
            int top = getBounds().top;
            int height = getBounds().height();

            int startW = startDrawable.getIntrinsicWidth();
            int startH = startDrawable.getIntrinsicHeight();
            int startTop = top + (height - startH) / 2;
            startDrawable.setBounds(left, startTop, left + startW, startTop + startH);
            startDrawable.draw(canvas);

            int endW = endDrawable.getIntrinsicWidth();
            int endH = endDrawable.getIntrinsicHeight();
            int endLeft = left + startW + gapPx;
            int endTop = top + (height - endH) / 2;
            endDrawable.setBounds(endLeft, endTop, endLeft + endW, endTop + endH);
            endDrawable.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            startDrawable.setAlpha(alpha);
            endDrawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
            startDrawable.setColorFilter(colorFilter);
            endDrawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
