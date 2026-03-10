package com.casic.otitan.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.R;
import com.casic.otitan.common.utils.common.DensityUtil;

/**
 * Created by fz on 2023/8/14 10:12
 * describe : 支持分别设置四个圆角和描边的ConstraintLayout
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
        float defaultRadius = DensityUtil.dp2px(getContext(), 8);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);

            // 读取背景颜色
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor,
                    ContextCompat.getColor(context, R.color.white));

            // 读取统一的圆角半径
            radius = typedArray.getDimension(R.styleable.CornerTextView_radius, defaultRadius);

            // 读取四个角的单独设置
            leftTopRadius = typedArray.getDimension(R.styleable.CornerTextView_leftTopRadius, radius);
            rightTopRadius = typedArray.getDimension(R.styleable.CornerTextView_rightTopRadius, radius);
            rightBottomRadius = typedArray.getDimension(R.styleable.CornerTextView_rightBottomRadius, radius);
            leftBottomRadius = typedArray.getDimension(R.styleable.CornerTextView_leftBottomRadius, radius);

            // 读取描边相关属性
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor,
                    ContextCompat.getColor(context, android.R.color.transparent));
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, 0f);

            // 判断是否设置了描边（宽度大于0且颜色不是透明）
            hasStroke = strokeWidth > 0 && strokeColor != ContextCompat.getColor(context, android.R.color.transparent);

            typedArray.recycle();

            // 应用圆角和描边设置
            applyBackground();
        } else {
            circleBackColor = ContextCompat.getColor(context, R.color.white);
            radius = defaultRadius;
            leftTopRadius = radius;
            rightTopRadius = radius;
            rightBottomRadius = radius;
            leftBottomRadius = radius;

            // 默认没有描边
            strokeColor = ContextCompat.getColor(context, android.R.color.transparent);
            strokeWidth = 0f;
            hasStroke = false;

            applyBackground();
        }
    }

    /**
     * 应用背景（圆角+描边）
     */
    private void applyBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(circleBackColor);

        // 设置圆角
        if (leftTopRadius == radius && rightTopRadius == radius &&
                rightBottomRadius == radius && leftBottomRadius == radius) {
            // 所有圆角相同，使用统一的圆角半径
            gd.setCornerRadius(radius);
        } else {
            // 分别设置四个角的圆角半径
            float[] radii = new float[]{
                    leftTopRadius, leftTopRadius,      // 左上角 x, y
                    rightTopRadius, rightTopRadius,    // 右上角 x, y
                    rightBottomRadius, rightBottomRadius, // 右下角 x, y
                    leftBottomRadius, leftBottomRadius    // 左下角 x, y
            };
            gd.setCornerRadii(radii);
        }

        // 设置描边（边框）
        if (hasStroke) {
            gd.setStroke((int) strokeWidth, strokeColor);
        }

        this.setBackground(gd);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        applyBackground();
    }

    public void setBgColor(int color) {
        this.circleBackColor = color;
        applyBackground();
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
     * @param leftTop 左上角圆角半径
     * @param rightTop 右上角圆角半径
     * @param rightBottom 右下角圆角半径
     * @param leftBottom 左下角圆角半径
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

    /**
     * 设置描边（边框）
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
     * @param width 描边宽度（像素）
     */
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        this.hasStroke = width > 0 && strokeColor != ContextCompat.getColor(getContext(), android.R.color.transparent);
        applyBackground();
    }

    /**
     * 设置描边颜色
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