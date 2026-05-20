package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatButton;

import io.coderf.arklab.common.R;


/**
 * Created by fz on 2019/5/31.
 * describe：自定义圆角矩形（支持分别设置四个圆角）
 */
public class CornerButton extends AppCompatButton {
    /**
     * 边框颜色
     */
    protected @ColorInt int strokeColor;
    /**
     * 背景颜色
     */
    protected @ColorInt int circleBackColor;
    /**
     * 圆角矩形样式
     */
    protected GradientDrawable gradientDrawable;
    /**
     * 圆角半径（四个角统一使用）
     */
    protected float radius;
    /**
     * 边框宽度
     */
    protected float strokeWidth;
    /**
     * 左上角圆角半径
     */
    protected float leftTopRadius;
    /**
     * 右上角圆角半径
     */
    protected float rightTopRadius;
    /**
     * 右下角圆角半径
     */
    protected float rightBottomRadius;
    /**
     * 左下角圆角半径
     */
    protected float leftBottomRadius;
    /**
     * 是否设置了描边
     */
    protected boolean hasStroke = false;
    /**
     * 是否已指定背景色（XML 或代码）
     */
    protected boolean hasBgColor = false;
    /**
     * 是否应用自定义圆角背景
     */
    protected boolean customBackgroundEnabled = false;

    public CornerButton(Context context) {
        this(context, null);
    }

    public CornerButton(Context context, AttributeSet attrs) {
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
        boolean hasLeftTop = typedArray.hasValue(R.styleable.CornerTextView_leftTopRadius);
        boolean hasRightTop = typedArray.hasValue(R.styleable.CornerTextView_rightTopRadius);
        boolean hasRightBottom = typedArray.hasValue(R.styleable.CornerTextView_rightBottomRadius);
        boolean hasLeftBottom = typedArray.hasValue(R.styleable.CornerTextView_leftBottomRadius);

        if (hasStrokeColor) {
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor, 0);
        }
        if (hasBgColor) {
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor, 0);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, 0);
        }

        radius = hasRadiusAttr ? typedArray.getDimension(R.styleable.CornerTextView_radius, 0) : 0;
        leftTopRadius = hasLeftTop
                ? typedArray.getDimension(R.styleable.CornerTextView_leftTopRadius, 0)
                : radius;
        rightTopRadius = hasRightTop
                ? typedArray.getDimension(R.styleable.CornerTextView_rightTopRadius, 0)
                : radius;
        rightBottomRadius = hasRightBottom
                ? typedArray.getDimension(R.styleable.CornerTextView_rightBottomRadius, 0)
                : radius;
        leftBottomRadius = hasLeftBottom
                ? typedArray.getDimension(R.styleable.CornerTextView_leftBottomRadius, 0)
                : radius;

        typedArray.recycle();

        hasStroke = strokeWidth > 0;
        customBackgroundEnabled = hasBgColor || hasRadiusAttr || hasLeftTop || hasRightTop
                || hasRightBottom || hasLeftBottom || hasStrokeWidthAttr || hasStrokeColor;
        if (customBackgroundEnabled) {
            applyBackground();
        }
    }

    /**
     * 应用背景（圆角+描边）
     */
    private void applyBackground() {
        customBackgroundEnabled = true;
        gradientDrawable = new GradientDrawable();
        if (hasBgColor) {
            gradientDrawable.setColor(circleBackColor);
        }

        // 设置圆角
        if (leftTopRadius == radius && rightTopRadius == radius &&
                rightBottomRadius == radius && leftBottomRadius == radius) {
            gradientDrawable.setCornerRadius(radius);
        } else {
            float[] radii = new float[]{
                    leftTopRadius, leftTopRadius,
                    rightTopRadius, rightTopRadius,
                    rightBottomRadius, rightBottomRadius,
                    leftBottomRadius, leftBottomRadius
            };
            gradientDrawable.setCornerRadii(radii);
        }

        // 设置描边
        if (hasStroke) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }

        this.setBackground(gradientDrawable);
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        this.hasStroke = strokeWidth > 0;
        applyBackground();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        applyBackground();
    }

    public void setStroke(@ColorInt int strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        applyBackground();
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
    }

    public void setBackColor(ColorStateList bgColor) {
        // GradientDrawable 不支持 ColorStateList，这里做转换处理
        if (bgColor != null && !bgColor.isStateful()) {
            gradientDrawable.setColor(bgColor.getDefaultColor());
        } else if (bgColor != null) {
            gradientDrawable.setColor(bgColor.getDefaultColor());
        }
        this.setBackground(gradientDrawable);
    }

    /**
     * 统一设置圆角半径
     */
    public void setRadius(float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        applyBackground();
    }

    /**
     * 分别设置四个角的圆角半径
     */
    public void setCornerRadii(float leftTop, float rightTop, float rightBottom, float leftBottom) {
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyBackground();
    }

    public void setBgColorAndRadius(@ColorInt int bgColor, float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        applyBackground();
    }

    /**
     * 设置背景颜色和四个角的圆角半径
     */
    public void setBgColorAndCornerRadii(@ColorInt int bgColor, float leftTop, float rightTop,
                                         float rightBottom, float leftBottom) {
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyBackground();
    }

    public void setButtonStyle(@ColorInt int strokeColor, float strokeWidth,
                               @ColorInt int bgColor, float radius) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.hasStroke = strokeWidth > 0;
        applyBackground();
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
        this.setBackground(gradientDrawable);
    }

    public float getLeftTopRadius() {
        return leftTopRadius;
    }

    public float getRightTopRadius() {
        return rightTopRadius;
    }

    public float getRightBottomRadius() {
        return rightBottomRadius;
    }

    public float getLeftBottomRadius() {
        return leftBottomRadius;
    }
}