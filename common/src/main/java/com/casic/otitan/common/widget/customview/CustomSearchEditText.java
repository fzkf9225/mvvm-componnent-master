package com.casic.otitan.common.widget.customview;

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

import com.casic.otitan.common.R;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.KeyBoardUtil;
import com.casic.otitan.common.utils.common.StringUtil;
import com.casic.otitan.common.utils.log.LogUtil;


/**
 * Create by CherishTang on 2019/12/25 0025
 * describe:自定义输入框
 */
public class CustomSearchEditText extends AppCompatEditText implements AppCompatEditText.OnEditorActionListener, TextWatcher {
    private final String TAG = this.getClass().getSimpleName();

    private Drawable drawableSearch = null;
    private Drawable drawableClear = null;
    /**
     * 1-左，2-上，3-右，4-下
     */
    private int drawablePosition = 1;
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private GradientDrawable gradientDrawable = new GradientDrawable();
    /**
     * 是否启用背景图、描边等样式
     */
    private boolean enableBgStyle = false;

    public CustomSearchEditText(Context context) {
        super(context);
        init(null);
    }

    public CustomSearchEditText(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context.obtainStyledAttributes(attrs, R.styleable.CustomEditText));
    }

    public CustomSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, defStyleAttr, 0));
    }

    private void init(TypedArray ta) {
        if (ta != null) {
            drawablePosition = ta.getInt(R.styleable.CustomEditText_drawablePosition, 3);
            drawableSearch = ta.getDrawable(R.styleable.CustomEditText_searchIcon);
            drawableClear = ta.getDrawable(R.styleable.CustomEditText_clearIcon);
            enableBgStyle = ta.getBoolean(R.styleable.CustomEditText_enableBgStyle, false);
            strokeColor = ta.getColor(R.styleable.CustomEditText_strokeColor, ContextCompat.getColor(getContext(), R.color.white));
            circleBackColor = ta.getColor(R.styleable.CustomEditText_bgColor, ContextCompat.getColor(getContext(), R.color.white));
            strokeWidth = ta.getDimension(R.styleable.CustomEditText_strokeWidth, 0);
            radius = ta.getDimension(R.styleable.CustomEditText_radius, 0);
            ta.recycle();
        } else {
            enableBgStyle = false;
            strokeColor = ContextCompat.getColor(getContext(), R.color.white);
            circleBackColor = ContextCompat.getColor(getContext(), R.color.white);
        }

        setSingleLine();
        setLines(1);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(DensityUtil.dp2px(getContext(), 8), 0, DensityUtil.dp2px(getContext(), 8), 0);
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        setTextSize(TypedValue.COMPLEX_UNIT_PX,DensityUtil.sp2px(getContext(),12f));
        setTextColor(ContextCompat.getColor(getContext(), R.color.search_view_background));
        if (drawableSearch == null) {
            drawableSearch = ContextCompat.getDrawable(getContext(), R.mipmap.ic_search_app_left);
        }
        if (drawableClear == null) {
            drawableClear = ContextCompat.getDrawable(getContext(), R.mipmap.icon_clear);
        }

        if (drawablePosition == 1) {
            setCompoundDrawablesWithIntrinsicBounds(drawableSearch, null, null, null);
        } else if (drawablePosition == 2) {
            setCompoundDrawablesWithIntrinsicBounds(null, drawableSearch, null, null);
        } else if (drawablePosition == 4) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawableSearch);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawableSearch, null);
        }
        setCompoundDrawablePadding(DensityUtil.dp2px(getContext(), 8));
        setOnEditorActionListener(this);
        addTextChangedListener(this);
        if (enableBgStyle) {
            gradientDrawable.setColor(circleBackColor);
            gradientDrawable.setCornerRadius(radius);
            if (strokeWidth > 0) {
                gradientDrawable.setStroke((int) strokeWidth, strokeColor);
            }
            this.setBackground(gradientDrawable);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            if (drawablePosition == 1) {
                setCompoundDrawablesWithIntrinsicBounds(drawableSearch, null, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, null);

            } else if (drawablePosition == 2) {
                setCompoundDrawablesWithIntrinsicBounds(null, drawableSearch, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, null);

            } else if (drawablePosition == 4) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, (s == null || StringUtil.isEmpty(s.toString())) ? null : drawableClear, drawableSearch);
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, (s == null || StringUtil.isEmpty(s.toString())) ? drawableSearch : drawableClear, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "onTextChanged:" + e);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (!(getText() == null || getText().toString().isEmpty())) {
                int xDown = (int) event.getX();
                if (event.getAction() == MotionEvent.ACTION_DOWN && xDown >= (getWidth() - getCompoundPaddingRight() * 2) && xDown < getWidth()) {
                    // 清除按钮的点击范围 按钮自身大小 +-padding
                    setText("");
                    if (onInputSubmitListener != null) {
                        onInputSubmitListener.onInputClear();
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "onTouchEvent:" + e);
        }
        super.onTouchEvent(event);
        return true;
    }

    private OnInputSubmitListener onInputSubmitListener;

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
            e.printStackTrace();
            LogUtil.show(TAG, "onEditorAction:" + e);
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
        if(enableBgStyle){
            return;
        }
        this.circleBackColor = color;
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        this.setBackground(gradientDrawable);
    }

    public void setStroke(int strokeWidth, int color) {
        if(enableBgStyle){
            return;
        }
        this.strokeColor = color;
        this.strokeWidth = strokeWidth;
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            gradientDrawable.setStroke(strokeWidth, strokeColor);
        }
        this.setBackground(gradientDrawable);
    }

    public void setBgColor(int color) {
        if(enableBgStyle){
            return;
        }
        this.circleBackColor = color;
        gradientDrawable.setColor(this.circleBackColor);
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        if(enableBgStyle){
            return;
        }
        this.gradientDrawable = gradientDrawable;
        this.setBackground(this.gradientDrawable);
    }

    public void setRadius(float radius) {
        if(enableBgStyle){
            return;
        }
        this.radius = radius;
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
    }

    public void setBgColorAndRadius(int color, float radius) {
        if(enableBgStyle){
            return;
        }
        this.radius = radius;
        this.circleBackColor = color;
        gradientDrawable.setColor(this.circleBackColor);
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
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
