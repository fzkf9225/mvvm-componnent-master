package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2023/11/10 8:57
 * describe :
 */
public class CirclePaddingImageView extends AppCompatImageView {

    private int borderColor;
    private boolean enableSelected;
    private boolean enablePressed;
    private int borderWidth;
    private GradientDrawable bgFocusedDrawable;
    private GradientDrawable bgDefaultDrawable;

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
            borderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_borderWidth, 0);
            enableSelected = a.getBoolean(R.styleable.CircleImageView_enableSelected, false);
            enablePressed = a.getBoolean(R.styleable.CircleImageView_enablePressed, false);
            a.recycle();
        } else {
            borderColor = 0xFFB4B4B4;
            borderFocusColor = 0xFFFFFFFF;
        }

        bgDefaultDrawable = new GradientDrawable();
        // 设置背景为圆形
        bgDefaultDrawable.setShape(GradientDrawable.OVAL);
        bgDefaultDrawable.setStroke(borderWidth, borderColor);

        bgFocusedDrawable = new GradientDrawable();
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