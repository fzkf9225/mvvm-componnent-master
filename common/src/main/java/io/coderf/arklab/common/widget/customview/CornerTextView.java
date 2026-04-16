package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;


/**
 * Created by fz on 2019/5/31.
 * describe：自定义圆角矩形（支持分别设置四个圆角）
 */
public class CornerTextView extends AppCompatTextView {
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
     * 圆角矩形
     */
    protected GradientDrawable gradientDrawable = new GradientDrawable();

    public CornerTextView(Context context) {
        this(context, null);
    }

    public CornerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);

            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor,
                    ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor,
                    ContextCompat.getColor(context, R.color.white));
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, 0);

            // 读取统一的圆角半径
            radius = typedArray.getDimension(R.styleable.CornerTextView_radius, 0);

            // 读取四个角的单独设置
            leftTopRadius = typedArray.getDimension(R.styleable.CornerTextView_leftTopRadius, radius);
            rightTopRadius = typedArray.getDimension(R.styleable.CornerTextView_rightTopRadius, radius);
            rightBottomRadius = typedArray.getDimension(R.styleable.CornerTextView_rightBottomRadius, radius);
            leftBottomRadius = typedArray.getDimension(R.styleable.CornerTextView_leftBottomRadius, radius);

            typedArray.recycle();
        } else {
            strokeColor = ContextCompat.getColor(context, R.color.white);
            circleBackColor = ContextCompat.getColor(context, R.color.white);
            radius = 0;
            leftTopRadius = radius;
            rightTopRadius = radius;
            rightBottomRadius = radius;
            leftBottomRadius = radius;
        }

        hasStroke = strokeWidth > 0;
        applyBackground();
    }

    /**
     * 应用背景（圆角+描边）
     */
    private void applyBackground() {
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(circleBackColor);

        // 设置圆角
        if (leftTopRadius == radius && rightTopRadius == radius &&
                rightBottomRadius == radius && leftBottomRadius == radius) {
            // 所有圆角相同，使用统一的圆角半径
            gradientDrawable.setCornerRadius(radius);
        } else {
            // 分别设置四个角的圆角半径
            float[] radii = new float[]{
                    leftTopRadius, leftTopRadius,      // 左上角 x, y
                    rightTopRadius, rightTopRadius,    // 右上角 x, y
                    rightBottomRadius, rightBottomRadius, // 右下角 x, y
                    leftBottomRadius, leftBottomRadius    // 左下角 x, y
            };
            gradientDrawable.setCornerRadii(radii);
        }

        // 设置描边
        if (hasStroke) {
            gradientDrawable.setStroke((int) strokeWidth, strokeColor);
        }

        this.setBackground(gradientDrawable);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
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
        applyBackground();
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
        this.setBackground(this.gradientDrawable);
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
        applyBackground();
    }

    /**
     * 设置背景颜色和四个角的圆角半径
     */
    public void setBgColorAndCornerRadii(int color, float leftTop, float rightTop,
                                         float rightBottom, float leftBottom) {
        this.circleBackColor = color;
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