package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.R;


/**
 * 圆角 TextView：XML / 代码 API 不变，背景走 Material3 {@code ShapeAppearance}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class CornerTextView extends MaterialTextView {
    /**
     * 边框颜色
     */
    protected int strokeColor;
    /**
     * 背景颜色
     */
    protected int circleBackColor;
    /**
     * 边框宽度
     */
    protected float strokeWidth;
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
    /** 仅兼容旧 {@link #setGradientDrawable} 入参，不再作为实际 background。 */
    protected GradientDrawable gradientDrawable;

    public CornerTextView(Context context) {
        this(context, null);
    }

    public CornerTextView(Context context, AttributeSet attrs) {
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

    private void applyBackground() {
        customBackgroundEnabled = true;
        setBackground(CornerShapeHelper.createBackground(
                leftTopRadius, rightTopRadius, rightBottomRadius, leftBottomRadius,
                hasBgColor, circleBackColor, hasStroke, strokeWidth, strokeColor));
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
        if (gradientDrawable == null) {
            return;
        }
        float[] radii = new float[4];
        int[] fill = new int[1];
        boolean[] hasFill = new boolean[1];
        CornerShapeHelper.copyFromGradient(gradientDrawable, radii, fill, hasFill);
        leftTopRadius = radii[0];
        rightTopRadius = radii[1];
        rightBottomRadius = radii[2];
        leftBottomRadius = radii[3];
        radius = leftTopRadius;
        if (hasFill[0]) {
            hasBgColor = true;
            circleBackColor = fill[0];
        }
        applyBackground();
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

    public void setBgColorAndRadius(int color, float radius) {
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
    public void setBgColorAndCornerRadii(int color, float leftTop, float rightTop,
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
     * 一次性设置描边、背景色和统一圆角，仅调用一次 applyBackground，避免分别设置带来的重复创建。
     *
     * @param strokeColor 描边颜色
     * @param strokeWidth 描边宽度（像素）
     * @param bgColor     背景颜色
     * @param radius      四个角统一圆角半径
     */
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
        applyBackground();
    }

    /**
     * 一次性设置描边、背景色和四个角圆角，仅调用一次 applyBackground。
     *
     * @param strokeColor  描边颜色
     * @param strokeWidth  描边宽度（像素）
     * @param bgColor      背景颜色
     * @param leftTop      左上角圆角半径
     * @param rightTop     右上角圆角半径
     * @param rightBottom  右下角圆角半径
     * @param leftBottom   左下角圆角半径
     */
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
