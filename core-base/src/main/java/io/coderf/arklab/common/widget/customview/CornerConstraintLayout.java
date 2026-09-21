package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.helper.CornerShapeHelper;

/**
 * 带 XML 圆角 / 填充 / 描边的 {@link ConstraintLayout}。
 * 官方容器没有这些属性，背景走 Material3 {@code MaterialShapeDrawable}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/3
 */
public class CornerConstraintLayout extends ConstraintLayout {
    /**
     * 背景颜色
     */
    protected int circleBackColor;
    /**
     * 圆角半径（四个角统一使用）
     */
    protected float radius;
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
     * 描边颜色
     */
    protected int strokeColor;
    /**
     * 描边宽度
     */
    protected float strokeWidth;

    /**
     * 是否设置了描边（用于判断是否应用描边）
     */
    protected boolean hasStroke = false;
    /**
     * 是否已指定背景色（XML 或代码）
     */
    protected boolean hasBgColor = false;

    public CornerConstraintLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerConstraintLayout);

        boolean hasStrokeColor = typedArray.hasValue(R.styleable.CornerConstraintLayout_strokeColor);
        boolean hasStrokeWidthAttr = typedArray.hasValue(R.styleable.CornerConstraintLayout_strokeWidth);
        hasBgColor = typedArray.hasValue(R.styleable.CornerConstraintLayout_bgColor);
        boolean hasRadiusAttr = typedArray.hasValue(R.styleable.CornerConstraintLayout_radius);
        boolean hasLeftTop = typedArray.hasValue(R.styleable.CornerConstraintLayout_leftTopRadius);
        boolean hasRightTop = typedArray.hasValue(R.styleable.CornerConstraintLayout_rightTopRadius);
        boolean hasRightBottom = typedArray.hasValue(R.styleable.CornerConstraintLayout_rightBottomRadius);
        boolean hasLeftBottom = typedArray.hasValue(R.styleable.CornerConstraintLayout_leftBottomRadius);

        if (hasBgColor) {
            circleBackColor = typedArray.getColor(R.styleable.CornerConstraintLayout_bgColor, 0);
        }
        if (hasStrokeColor) {
            strokeColor = typedArray.getColor(R.styleable.CornerConstraintLayout_strokeColor, 0);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = typedArray.getDimension(R.styleable.CornerConstraintLayout_strokeWidth, 0f);
        }

        radius = hasRadiusAttr ? typedArray.getDimension(R.styleable.CornerConstraintLayout_radius, 0) : 0;
        leftTopRadius = hasLeftTop
                ? typedArray.getDimension(R.styleable.CornerConstraintLayout_leftTopRadius, 0)
                : radius;
        rightTopRadius = hasRightTop
                ? typedArray.getDimension(R.styleable.CornerConstraintLayout_rightTopRadius, 0)
                : radius;
        rightBottomRadius = hasRightBottom
                ? typedArray.getDimension(R.styleable.CornerConstraintLayout_rightBottomRadius, 0)
                : radius;
        leftBottomRadius = hasLeftBottom
                ? typedArray.getDimension(R.styleable.CornerConstraintLayout_leftBottomRadius, 0)
                : radius;

        typedArray.recycle();

        hasStroke = strokeWidth > 0 && hasStrokeColor;
        if (hasBgColor || hasRadiusAttr || hasLeftTop || hasRightTop || hasRightBottom || hasLeftBottom
                || hasStrokeWidthAttr || hasStrokeColor) {
            applyBackground();
        }
    }

    private void applyBackground() {
        setClipToOutline(true);
        setBackground(CornerShapeHelper.createBackground(
                leftTopRadius, rightTopRadius, rightBottomRadius, leftBottomRadius,
                hasBgColor, circleBackColor, hasStroke, strokeWidth, strokeColor));
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
    }

    public void setBgColor(@ColorInt int color) {
        setBackColor(color);
    }

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

    public void setBgColorAndRadius(@ColorInt int color, float radius) {
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        this.circleBackColor = color;
        this.hasBgColor = true;
        applyBackground();
    }

    /**
     * 设置背景颜色和四个角的圆角半径
     */
    public void setBgColorAndCornerRadii(@ColorInt int color, float leftTop, float rightTop,
                                         float rightBottom, float leftBottom) {
        this.circleBackColor = color;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyBackground();
    }

    /**
     * 一次性设置描边、背景色和统一圆角，仅调用一次 applyBackground。
     */
    public void setStrokeBgColorAndRadius(@ColorInt int strokeColor, float strokeWidth,
                                          @ColorInt int bgColor, float radius) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0
                && strokeColor != ContextCompat.getColor(getContext(), android.R.color.transparent);
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        applyBackground();
    }

    /**
     * 一次性设置描边、背景色和四个角圆角，仅调用一次 applyBackground。
     */
    public void setStrokeBgColorAndCornerRadii(@ColorInt int strokeColor, float strokeWidth,
                                               @ColorInt int bgColor,
                                               float leftTop, float rightTop,
                                               float rightBottom, float leftBottom) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0
                && strokeColor != ContextCompat.getColor(getContext(), android.R.color.transparent);
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        applyBackground();
    }

    /**
     * 设置描边（边框）
     *
     * @param width 描边宽度（像素）
     * @param color 描边颜色
     */
    public void setStroke(float width, @ColorInt int color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = width > 0 && color != ContextCompat.getColor(getContext(), android.R.color.transparent);
        applyBackground();
    }

    /**
     * 设置描边宽度
     *
     * @param width 描边宽度（像素）
     */
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        this.hasStroke = width > 0 && strokeColor != ContextCompat.getColor(getContext(), android.R.color.transparent);
        applyBackground();
    }

    /**
     * 设置描边颜色
     *
     * @param color 描边颜色
     */
    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        this.hasStroke = strokeWidth > 0 && color != ContextCompat.getColor(getContext(), android.R.color.transparent);
        applyBackground();
    }

    /**
     * 移除描边
     */
    public void removeStroke() {
        this.strokeWidth = 0f;
        this.strokeColor = ContextCompat.getColor(getContext(), android.R.color.transparent);
        this.hasStroke = false;
        applyBackground();
    }

    /**
     * 获取当前描边宽度
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 获取当前描边颜色
     */
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * 是否有描边
     */
    public boolean hasStroke() {
        return hasStroke;
    }
}
