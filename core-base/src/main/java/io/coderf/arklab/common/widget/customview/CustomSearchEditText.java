package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.KeyBoardUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * Create by fz on 2019/12/25 0025
 * describe:自定义搜索输入框
 */
public class CustomSearchEditText extends AppCompatEditText implements AppCompatEditText.OnEditorActionListener, TextWatcher {

    private static final int DRAWABLE_LEFT = 1;
    private static final int DRAWABLE_TOP = 2;
    private static final int DRAWABLE_RIGHT = 3;
    private static final int DRAWABLE_BOTTOM = 4;

    private final String TAG = getClass().getSimpleName();

    protected Drawable drawableSearch;
    protected Drawable drawableClear;
    protected int drawablePosition = DRAWABLE_RIGHT;
    protected int strokeColor;
    protected int circleBackColor;
    protected float radius;
    protected float strokeWidth;
    protected GradientDrawable gradientDrawable = new GradientDrawable();
    protected boolean enableBgStyle = false;

    private OnInputSubmitListener onInputSubmitListener;

    public CustomSearchEditText(Context context) {
        super(context);
        init(null);
    }

    public CustomSearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.CustomEditText));
    }

    public CustomSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, defStyleAttr, 0));
    }

    private void init(TypedArray ta) {
        if (ta != null) {
            drawablePosition = ta.getInt(R.styleable.CustomEditText_drawablePosition, DRAWABLE_RIGHT);
            drawableSearch = ta.getDrawable(R.styleable.CustomEditText_searchIcon);
            drawableClear = ta.getDrawable(R.styleable.CustomEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.CustomEditText_enableBgStyle, false);
            strokeColor = ta.getColor(R.styleable.CustomEditText_strokeColor, ContextCompat.getColor(getContext(), R.color.white));
            circleBackColor = ta.getColor(R.styleable.CustomEditText_bgColor, ContextCompat.getColor(getContext(), R.color.white));
            strokeWidth = ta.getDimension(R.styleable.CustomEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.CustomEditText_radius, 0);
            ta.recycle();
        } else {
            strokeColor = ContextCompat.getColor(getContext(), R.color.white);
            circleBackColor = ContextCompat.getColor(getContext(), R.color.white);
        }

        if (drawableSearch == null) {
            drawableSearch = ContextCompat.getDrawable(getContext(), R.mipmap.ic_search_app_left);
        }
        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(getContext(), R.mipmap.icon_clear);
        }
        mutateDrawables();

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(DensityUtil.dp2px(getContext(), 8), 0, DensityUtil.dp2px(getContext(), 8), 0);
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(getContext(), 12f));
        setTextColor(ContextCompat.getColor(getContext(), R.color.search_view_background));
        setCompoundDrawablePadding(DensityUtil.dp2px(getContext(), 8));
        setOnEditorActionListener(this);
        addTextChangedListener(this);
        updateCompoundDrawables(getText());
        applyBackgroundIfNeeded();
    }

    private void mutateDrawables() {
        if (drawableSearch != null) {
            drawableSearch = drawableSearch.mutate();
        }
        if (drawableClear != null) {
            drawableClear = drawableClear.mutate();
        }
    }

    private void updateCompoundDrawables(CharSequence text) {
        boolean showClear = text != null && !StringUtil.isEmpty(text.toString());
        Drawable left = null;
        Drawable top = null;
        Drawable right = null;
        Drawable bottom = null;

        right = switch (drawablePosition) {
            case DRAWABLE_LEFT -> {
                left = drawableSearch;
                yield showClear ? drawableClear : null;
            }
            case DRAWABLE_TOP -> {
                top = drawableSearch;
                yield showClear ? drawableClear : null;
            }
            case DRAWABLE_BOTTOM -> {
                bottom = drawableSearch;
                yield showClear ? drawableClear : null;
            }
            default -> showClear ? drawableClear : drawableSearch;
        };
        setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
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
                if (onInputSubmitListener != null) {
                    onInputSubmitListener.onInputClear();
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

    public void setOnInputSubmitListener(OnInputSubmitListener onInputSubmitListener) {
        this.onInputSubmitListener = onInputSubmitListener;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        try {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = v.getText().toString().trim();
                KeyBoardUtil.closeKeyboard(this, getContext());
                if (onInputSubmitListener != null) {
                    onInputSubmitListener.onInputSubmit(keyword);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.logger(TAG, "onEditorAction:" + e);
        }
        return false;
    }

    public interface OnInputSubmitListener {
        void onInputSubmit(String query);

        void onInputClear();
    }

    public void setEnableBgStyle(boolean enableBgStyle) {
        this.enableBgStyle = enableBgStyle;
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
