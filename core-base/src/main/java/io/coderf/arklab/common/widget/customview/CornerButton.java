package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import io.coderf.arklab.common.R;


/**
 * MaterialButton 圆角封装：用 {@link ShapeAppearanceModel} + backgroundTint / stroke，
 * 不再自绘 {@link GradientDrawable}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class CornerButton extends MaterialButton {
    protected @ColorInt int strokeColor;
    protected @ColorInt int circleBackColor;
    protected float radius;
    protected float strokeWidth;
    protected float leftTopRadius;
    protected float rightTopRadius;
    protected float rightBottomRadius;
    protected float leftBottomRadius;
    protected boolean hasStroke = false;
    protected boolean hasBgColor = false;
    protected boolean customBackgroundEnabled = false;

    public CornerButton(Context context) {
        this(context, null);
    }

    public CornerButton(Context context, AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialButtonStyle);
    }

    public CornerButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
            applyMaterialShape();
        }
    }

    /**
     * 用 Material3 ShapeAppearance + backgroundTint / stroke 铺满控件，
     * 去掉 Filled Button 默认 inset 和 elevation，视觉上对齐旧 GradientDrawable。
     */
    private void applyMaterialShape() {
        customBackgroundEnabled = true;
        setInsetTop(0);
        setInsetBottom(0);
        setElevation(0f);
        setStateListAnimator(null);
        setShapeAppearanceModel(ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, leftTopRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, rightTopRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, rightBottomRadius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, leftBottomRadius)
                .build());
        if (hasBgColor) {
            setBackgroundTintList(ColorStateList.valueOf(circleBackColor));
        }
        if (hasStroke) {
            super.setStrokeWidth((int) strokeWidth);
            super.setStrokeColor(ColorStateList.valueOf(strokeColor));
        } else {
            super.setStrokeWidth(0);
            super.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
        }
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        this.hasStroke = strokeWidth > 0;
        applyMaterialShape();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        applyMaterialShape();
    }

    public void setStroke(@ColorInt int strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        applyMaterialShape();
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyMaterialShape();
    }

    public void setBackColor(ColorStateList bgColor) {
        if (bgColor == null) {
            return;
        }
        this.hasBgColor = true;
        this.circleBackColor = bgColor.getDefaultColor();
        setBackgroundTintList(bgColor);
        if (customBackgroundEnabled) {
            applyMaterialShape();
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        applyMaterialShape();
    }

    public void setCornerRadii(float leftTop, float rightTop, float rightBottom, float leftBottom) {
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyMaterialShape();
    }

    public void setBgColorAndRadius(@ColorInt int bgColor, float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        applyMaterialShape();
    }

    public void setBgColorAndCornerRadii(@ColorInt int bgColor, float leftTop, float rightTop,
                                         float rightBottom, float leftBottom) {
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyMaterialShape();
    }

    public void setStrokeBgColorAndRadius(@ColorInt int strokeColor, float strokeWidth,
                                          @ColorInt int bgColor, float radius) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        applyMaterialShape();
    }

    public void setStrokeBgColorAndCornerRadii(@ColorInt int strokeColor, float strokeWidth,
                                               @ColorInt int bgColor,
                                               float leftTop, float rightTop,
                                               float rightBottom, float leftBottom) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyMaterialShape();
    }

    public void setButtonStyle(@ColorInt int strokeColor, float strokeWidth,
                               @ColorInt int bgColor, float radius) {
        setStrokeBgColorAndRadius(strokeColor, strokeWidth, bgColor, radius);
    }

    /**
     * 兼容旧 API：把 GradientDrawable 的色/角/描边映射到 ShapeAppearance，不再 setBackground。
     */
    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        if (gradientDrawable == null) {
            return;
        }
        ColorStateList fill = gradientDrawable.getColor();
        if (fill != null) {
            hasBgColor = true;
            circleBackColor = fill.getDefaultColor();
        }
        float[] radii = gradientDrawable.getCornerRadii();
        if (radii != null && radii.length >= 8) {
            leftTopRadius = radii[0];
            rightTopRadius = radii[2];
            rightBottomRadius = radii[4];
            leftBottomRadius = radii[6];
            radius = leftTopRadius;
        } else {
            radius = gradientDrawable.getCornerRadius();
            leftTopRadius = radius;
            rightTopRadius = radius;
            rightBottomRadius = radius;
            leftBottomRadius = radius;
        }
        applyMaterialShape();
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
