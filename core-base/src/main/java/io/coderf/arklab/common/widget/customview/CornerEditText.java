package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.google.android.material.textfield.TextInputEditText;

import io.coderf.arklab.common.R;


/**
 * 圆角输入框：XML / 代码 API 不变，背景走 Material3 {@code ShapeAppearance}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class CornerEditText extends TextInputEditText {
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
    /** 仅兼容旧 {@link #setGradientDrawable} 入参，不再作为实际 background。 */
    private GradientDrawable gradientDrawable;

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
        setBackground(CornerShapeHelper.createBackground(
                radius, radius, radius, radius,
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
        radius = radii[0];
        if (hasFill[0]) {
            hasBgColor = true;
            circleBackColor = fill[0];
        }
        applyBackground();
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

    /**
     * 一次性设置描边、背景色和圆角，仅调用一次 applyBackground，避免分别设置带来的重复创建。
     *
     * @param strokeColor 描边颜色
     * @param strokeWidth 描边宽度（像素）
     * @param bgColor     背景颜色
     * @param radius      圆角半径
     */
    public void setStrokeBgColorAndRadius(@ColorInt int strokeColor, float strokeWidth,
                                          @ColorInt int bgColor, float radius) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        this.circleBackColor = bgColor;
        this.hasBgColor = true;
        this.radius = radius;
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
