package com.casic.otitan.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.casic.otitan.common.R;

/**
 * Created by fz on 2023/11/10 8:57
 * describe :自带内边距的ImageView，有选中样式、取消样式等
 */
public class CirclePaddingImageView extends AppCompatImageView {
    /**
     * 边框颜色
     */
    protected int borderColor;
    /**
     * 默认背景色
     */
    protected int defaultBackgroundColor;
    /**
     * 选中或按下时的北背景颜色
     */
    protected int focusBackgroundColor;
    /**
     * 是否启用选中样式
     */
    protected boolean enableSelected;
    /**
     * 是否启用按下样式
     */
    protected boolean enablePressed;
    /**
     * 边框粗细
     */
    protected int borderWidth;
    /**
     * 按下时的背景样式资源
     */
    protected GradientDrawable bgFocusedDrawable;
    /**
     * 默认的背景样式资源
     */
    protected GradientDrawable bgDefaultDrawable;

    public CirclePaddingImageView(Context context) {
        super(context);
        init(null);
    }

    public CirclePaddingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CirclePaddingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        int borderFocusColor;
        if (attrs != null) {
            // 从XML中获取自定义属性
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleImageView);
            borderColor = a.getColor(R.styleable.CircleImageView_borderColor, 0xFFB4B4B4);
            borderFocusColor = a.getColor(R.styleable.CircleImageView_borderFocusColor, 0xFFFFFFFF);
            defaultBackgroundColor = a.getColor(R.styleable.CircleImageView_defaultBackgroundColor, 0xFF1F1F1F);
            focusBackgroundColor = a.getColor(R.styleable.CircleImageView_focusBackgroundColor, 0xFF0F0F0F);
            borderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_borderWidth, 0);
            enableSelected = a.getBoolean(R.styleable.CircleImageView_enableSelected, false);
            enablePressed = a.getBoolean(R.styleable.CircleImageView_enablePressed, false);
            a.recycle();
        } else {
            borderColor = 0xFFB4B4B4;
            borderFocusColor = 0xFFFFFFFF;
            defaultBackgroundColor = 0xFF1F1F1F;
            focusBackgroundColor = 0xFF0F0F0F;
        }

        bgDefaultDrawable = new GradientDrawable();
        // 设置背景为圆形
        bgDefaultDrawable.setColor(defaultBackgroundColor);
        bgDefaultDrawable.setShape(GradientDrawable.OVAL);
        bgDefaultDrawable.setStroke(borderWidth, borderColor);

        bgFocusedDrawable = new GradientDrawable();
        bgFocusedDrawable.setColor(focusBackgroundColor);
        bgFocusedDrawable.setShape(GradientDrawable.OVAL);
        bgFocusedDrawable.setStroke(borderWidth, borderFocusColor);
        setBackground(bgDefaultDrawable);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (!enableSelected) {
            return;
        }
        if (selected) {
            setBackground(bgFocusedDrawable);
        } else {
            setBackground(bgDefaultDrawable);
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (!enableSelected && !enablePressed) {
            return;
        }
        if (enablePressed) {
            if (pressed) {
                setBackground(bgFocusedDrawable);
            } else {
                setBackground(bgDefaultDrawable);
            }
            return;
        }
        if (pressed) {
            setBackground(bgFocusedDrawable);
        }
    }

    public void setBorderColor(int color) {
        borderColor = color;
        invalidate();
    }

    public void setBorderWidth(int width) {
        borderWidth = width;
        invalidate();
    }

}