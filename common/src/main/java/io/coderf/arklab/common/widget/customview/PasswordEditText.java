package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * 密码输入框：圆角背景、明文/密文切换，可选清除按钮（可与切换按钮同时显示）。
 * 样式属性与 {@link ClearableEditText} 保持一致，便于 XML 复用。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/7/13 10:10
 */
public class PasswordEditText extends AppCompatEditText implements TextWatcher {

    private static final int INDEX_DRAWABLE_END = 2;
    private static final int ZONE_NONE = 0;
    private static final int ZONE_CLEAR = 1;
    private static final int ZONE_TOGGLE = 2;

    private final String TAG = getClass().getSimpleName();

    private Drawable drawableVisible;
    private Drawable drawableInvisible;
    private Drawable drawableClear;
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private final GradientDrawable gradientDrawable = new GradientDrawable();
    private boolean enableBgStyle = true;
    private boolean passwordVisible = false;
    private boolean enableToggle = true;
    private boolean enableClear = false;
    private boolean showingClear = false;
    private boolean showingToggle = false;
    private int endIconGapPx;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText, defStyleAttr, 0));
    }

    private void init(TypedArray ta) {
        endIconGapPx = DensityUtil.dp2px(getContext(), 8);
        if (ta != null) {
            drawableVisible = ta.getDrawable(R.styleable.PasswordEditText_passwordVisibleIcon);
            drawableInvisible = ta.getDrawable(R.styleable.PasswordEditText_passwordInvisibleIcon);
            drawableClear = ta.getDrawable(R.styleable.PasswordEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.PasswordEditText_enableBgStyle, true);
            enableToggle = ta.getBoolean(R.styleable.PasswordEditText_enablePasswordToggle, true);
            enableClear = ta.getBoolean(R.styleable.PasswordEditText_enableClear, false);
            strokeColor = ta.getColor(R.styleable.PasswordEditText_strokeColor,
                    ContextCompat.getColor(getContext(), R.color.white));
            circleBackColor = ta.getColor(R.styleable.PasswordEditText_bgColor,
                    ContextCompat.getColor(getContext(), R.color.white));
            strokeWidth = ta.getDimension(R.styleable.PasswordEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.PasswordEditText_radius, 0);
            ta.recycle();
        } else {
            strokeColor = ContextCompat.getColor(getContext(), R.color.white);
            circleBackColor = ContextCompat.getColor(getContext(), R.color.white);
        }

        if (drawableVisible == null) {
            drawableVisible = ContextCompat.getDrawable(getContext(), R.drawable.ic_password_visible);
        }
        if (drawableInvisible == null) {
            drawableInvisible = ContextCompat.getDrawable(getContext(), R.drawable.ic_password_invisible);
        }
        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(getContext(), R.mipmap.icon_clear);
        }
        mutateDrawables();

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(DensityUtil.dp2px(getContext(), 8), 0, DensityUtil.dp2px(getContext(), 8), 0);
        setCompoundDrawablePadding(DensityUtil.dp2px(getContext(), 4));
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        applyPasswordVisibility(false);
        addTextChangedListener(this);
        updateRightDrawables(getText());
        applyBackgroundIfNeeded();
    }

    private void mutateDrawables() {
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
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, endDrawable, null);
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
        int drawableWidth = drawables[INDEX_DRAWABLE_END].getBounds().width();
        if (drawableWidth <= 0) {
            drawableWidth = drawables[INDEX_DRAWABLE_END].getIntrinsicWidth();
        }
        int drawableHeight = drawables[INDEX_DRAWABLE_END].getBounds().height();
        if (drawableHeight <= 0) {
            drawableHeight = drawables[INDEX_DRAWABLE_END].getIntrinsicHeight();
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
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }
        setBackground(gradientDrawable);
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
     * 将两个 Drawable 横向拼接为一个 end compound drawable。
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
