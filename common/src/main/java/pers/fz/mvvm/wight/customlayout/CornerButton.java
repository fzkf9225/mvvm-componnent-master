package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;


/**
 * Created by CherishTang on 2019/5/31.
 * describe：自定义圆角矩形
 */
public class CornerButton extends AppCompatButton {
    private int strokeColor;
    private int circleBackColor;
    private final GradientDrawable gradientDrawable;
    private float radius, strokeWidth;

    public CornerButton(Context context) {
        this(context, null);
    }

    public CornerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor, ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor, ContextCompat.getColor(context, R.color.white));
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, strokeWidth);
            radius = typedArray.getDimension(R.styleable.CornerTextView_radius, 0);
            typedArray.recycle();
        } else {
            strokeColor = ContextCompat.getColor(context, R.color.white);
            circleBackColor = ContextCompat.getColor(context, R.color.white);
        }
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }
        this.setBackground(gradientDrawable);
    }

    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        gradientDrawable.setStroke((int) strokeWidth, this.strokeColor);
        this.setBackground(gradientDrawable);
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
        gradientDrawable.setStroke(this.strokeColor, strokeColor);
        this.setBackground(gradientDrawable);
    }

    public void setStroke(@ColorInt int color, int width) {
        this.strokeColor = color;
        this.strokeWidth = width;
        gradientDrawable.setStroke(this.strokeColor, this.strokeColor);
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        gradientDrawable.setColor(circleBackColor);
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(ColorStateList color) {
        gradientDrawable.setColor(color);
        this.setBackground(gradientDrawable);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
    }

    public void setBgColorAndRadius(int color, float radius) {
        this.radius = radius;
        this.circleBackColor = color;
        gradientDrawable.setColor(this.circleBackColor);
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
    }
}
