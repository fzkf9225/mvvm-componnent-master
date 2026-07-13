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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * 圆角背景 + 清除按钮的输入框，不包含搜索相关能力。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 9:25
 */
public class ClearableEditText extends AppCompatEditText implements TextWatcher {

    private final String TAG = getClass().getSimpleName();

    private Drawable drawableClear;
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private GradientDrawable gradientDrawable = new GradientDrawable();
    private boolean enableBgStyle = true;

    private OnClearListener onClearListener;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText, defStyleAttr, 0));
    }

    private void init(TypedArray ta) {
        if (ta != null) {
            drawableClear = ta.getDrawable(R.styleable.ClearableEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.ClearableEditText_enableBgStyle, true);
            strokeColor = ta.getColor(R.styleable.ClearableEditText_strokeColor, ContextCompat.getColor(getContext(), R.color.white));
            circleBackColor = ta.getColor(R.styleable.ClearableEditText_bgColor, ContextCompat.getColor(getContext(), R.color.white));
            strokeWidth = ta.getDimension(R.styleable.ClearableEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.ClearableEditText_radius, 0);
            ta.recycle();
        } else {
            strokeColor = ContextCompat.getColor(getContext(), R.color.white);
            circleBackColor = ContextCompat.getColor(getContext(), R.color.white);
        }

        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(getContext(), R.mipmap.icon_clear);
        }
        if (drawableClear != null) {
            drawableClear = drawableClear.mutate();
        }

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(DensityUtil.dp2px(getContext(), 8), 0, DensityUtil.dp2px(getContext(), 8), 0);
        setCompoundDrawablePadding(DensityUtil.dp2px(getContext(), 8));
        addTextChangedListener(this);
        updateClearDrawable(getText());
        applyBackgroundIfNeeded();
    }

    private void updateClearDrawable(CharSequence text) {
        boolean showClear = text != null && !StringUtil.isEmpty(text.toString());
        setCompoundDrawablesWithIntrinsicBounds(null, null, showClear ? drawableClear : null, null);
    }

    private void applyBackgroundIfNeeded() {
        if (!enableBgStyle) {
            return;
        }
        applyBackground();
    }

    private void applyBackground() {
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }
        setBackground(gradientDrawable);
    }

    private boolean isTouchOnClearButton(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        CharSequence text = getText();
        if (text == null || text.toString().isEmpty()) {
            return false;
        }
        int xDown = (int) event.getX();
        return xDown >= (getWidth() - getCompoundPaddingRight() * 2) && xDown < getWidth();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            updateClearDrawable(s);
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

    public void setEnableBgStyle(boolean enableBgStyle) {
        this.enableBgStyle = enableBgStyle;
        applyBackgroundIfNeeded();
    }

    public void setBackColor(@ColorInt int color) {
        if (enableBgStyle) {
            return;
        }
        this.circleBackColor = color;
        applyBackground();
    }

    public void setStroke(int strokeWidth, int color) {
        if (enableBgStyle) {
            return;
        }
        this.strokeColor = color;
        this.strokeWidth = strokeWidth;
        applyBackground();
    }

    public void setBgColor(int color) {
        if (enableBgStyle) {
            return;
        }
        this.circleBackColor = color;
        applyBackground();
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        if (enableBgStyle) {
            return;
        }
        this.gradientDrawable = gradientDrawable;
        setBackground(this.gradientDrawable);
    }

    public void setRadius(float radius) {
        if (enableBgStyle) {
            return;
        }
        this.radius = radius;
        applyBackground();
    }

    public void setBgColorAndRadius(int color, float radius) {
        if (enableBgStyle) {
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
