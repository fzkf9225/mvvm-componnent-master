package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatEditText;

import io.coderf.arklab.common.R;


/**
 * Created by fz on 2019/5/31.
 * describe：自定义圆角矩形
 */
public class CornerEditText extends AppCompatEditText {
    /**
     * 边框颜色
     */
    protected int strokeColor;
    /**
     * 背景颜色
     */
    protected int circleBackColor;
    /**
     * 圆角半径
     */
    protected float radius;
    /**
     * 边框宽度
     */
    protected float strokeWidth;
    /**
     * 是否已指定背景色（XML 或代码）
     */
    protected boolean hasBgColor = false;
    /**
     * 是否设置了描边
     */
    protected boolean hasStroke = false;
    /**
     * 背景样式
     */
    private GradientDrawable gradientDrawable = new GradientDrawable();

    public CornerEditText(Context context) {
        this(context, null);
    }

    public CornerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);

        boolean hasStrokeColor = typedArray.hasValue(R.styleable.CornerTextView_strokeColor);
        boolean hasStrokeWidthAttr = typedArray.hasValue(R.styleable.CornerTextView_strokeWidth);
        hasBgColor = typedArray.hasValue(R.styleable.CornerTextView_bgColor);
        boolean hasRadiusAttr = typedArray.hasValue(R.styleable.CornerTextView_radius);

        if (hasStrokeColor) {
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor, 0);
        }
        if (hasBgColor) {
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor, 0);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, 0);
        }
        if (hasRadiusAttr) {
            radius = typedArray.getDimension(R.styleable.CornerTextView_radius, 0);
        }

        typedArray.recycle();

        hasStroke = strokeWidth > 0;
        if (hasBgColor || hasRadiusAttr || hasStrokeWidthAttr || hasStrokeColor) {
            applyBackground();
        }
    }

    private void applyBackground() {
        gradientDrawable = new GradientDrawable();
        if (hasBgColor) {
            gradientDrawable.setColor(circleBackColor);
        }
        gradientDrawable.setCornerRadius(radius);
        if (hasStroke) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
    }

    public void setStroke(int strokeWidth, int color) {
        this.strokeColor = color;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        applyBackground();
    }

    public void setBgColor(int color) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
        this.setBackground(this.gradientDrawable);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        applyBackground();
    }

    public void setBgColorAndRadius(int color, float radius) {
        this.radius = radius;
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
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
