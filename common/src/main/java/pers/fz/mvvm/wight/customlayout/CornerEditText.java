package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;


/**
 * Created by CherishTang on 2019/5/31.
 * describe：自定义圆角矩形
 */
public class CornerEditText extends AppCompatEditText {
    private int strokeColor;
    private int circleBackColor;
    private float radius;
    private float strokeWidth;
    private GradientDrawable gradientDrawable = new GradientDrawable();

    public CornerEditText(Context context) {
        this(context, null);
    }

    public CornerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor, ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor, ContextCompat.getColor(context, R.color.white));
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, 0);
            radius = typedArray.getDimension(R.styleable.CornerTextView_radius, 0);
            typedArray.recycle();
        } else {
            strokeColor = ContextCompat.getColor(context, R.color.white);
            circleBackColor = ContextCompat.getColor(context, R.color.white);
        }

        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        this.setBackground(gradientDrawable);
    }

    public void setStroke(int strokeWidth,int color) {
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
        this.circleBackColor = color;
        gradientDrawable.setColor(this.circleBackColor);
        gradientDrawable.setCornerRadius(this.radius);
        this.setBackground(gradientDrawable);
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
        this.setBackground(this.gradientDrawable);
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
