package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import io.coderf.arklab.common.R;

/**
 * created by fz on 2019/9/3 0003
 * describe:圆角ImageView
 */
public class CornerImageView extends AppCompatImageView {
    /**
     * 图片宽高
     */
    protected float width, height;
    /**
     * 圆角半径
     */
    protected int radius;
    /**
     * 左上圆角半径
     */
    protected int leftTopRadius;
    /**
     * 右上圆角半径
     */
    protected int rightTopRadius;
    /**
     * 右下圆角半径
     */
    protected int rightBottomRadius;
    /**
     * 左下圆角半径
     */
    protected int leftBottomRadius;
    /**
     * 背景颜色
     */
    protected int bgColor;
    /**
     * 是否已指定背景色（XML 或代码）
     */
    protected boolean hasBgColor = false;
    /**
     * 是否启用圆角裁剪
     */
    protected boolean cornerClipEnabled = false;

    /**
     * 边框颜色
     */
    protected int strokeColor;
    /**
     * 边框宽度
     */
    protected float strokeWidth;
    /**
     * 是否设置了描边
     */
    protected boolean hasStroke = false;

    protected Paint mPaint;
    protected final Path mPath = new Path();

    public CornerImageView(Context context) {
        this(context, null);
    }

    public CornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        if (attrs == null) {
            return;
        }

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Custom_Round_Image_View);
        boolean hasRadiusAttr = array.hasValue(R.styleable.Custom_Round_Image_View_radius);
        boolean hasLeftTop = array.hasValue(R.styleable.Custom_Round_Image_View_leftTopRadius);
        boolean hasRightTop = array.hasValue(R.styleable.Custom_Round_Image_View_rightTopRadius);
        boolean hasRightBottom = array.hasValue(R.styleable.Custom_Round_Image_View_rightBottomRadius);
        boolean hasLeftBottom = array.hasValue(R.styleable.Custom_Round_Image_View_leftBottomRadius);
        hasBgColor = array.hasValue(R.styleable.Custom_Round_Image_View_bgColor);

        boolean hasStrokeColor = array.hasValue(R.styleable.Custom_Round_Image_View_strokeColor);
        boolean hasStrokeWidthAttr = array.hasValue(R.styleable.Custom_Round_Image_View_strokeWidth);

        radius = hasRadiusAttr ? array.getDimensionPixelOffset(R.styleable.Custom_Round_Image_View_radius, 0) : 0;
        leftTopRadius = hasLeftTop
                ? array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftTopRadius, 0)
                : radius;
        rightTopRadius = hasRightTop
                ? array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightTopRadius, 0)
                : radius;
        rightBottomRadius = hasRightBottom
                ? array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightBottomRadius, 0)
                : radius;
        leftBottomRadius = hasLeftBottom
                ? array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftBottomRadius, 0)
                : radius;

        if (hasBgColor) {
            bgColor = array.getColor(R.styleable.Custom_Round_Image_View_bgColor, Color.TRANSPARENT);
        }

        if (hasStrokeColor) {
            strokeColor = array.getColor(R.styleable.Custom_Round_Image_View_strokeColor, 0);
        }
        if (hasStrokeWidthAttr) {
            strokeWidth = array.getDimension(R.styleable.Custom_Round_Image_View_strokeWidth, 0);
        }

        array.recycle();

        hasStroke = strokeWidth > 0;
        cornerClipEnabled = hasRadiusAttr || hasLeftTop || hasRightTop || hasRightBottom || hasLeftBottom;
        if (cornerClipEnabled) {
            updateCornerClipEnabled();
        }
    }

    private void updateCornerClipEnabled() {
        cornerClipEnabled = leftTopRadius > 0 || rightTopRadius > 0
                || rightBottomRadius > 0 || leftBottomRadius > 0;
    }

    /**
     * 设置统一圆角半径
     */
    public void setRadius(int radius) {
        this.radius = radius;
        leftTopRadius = radius;
        rightTopRadius = radius;
        rightBottomRadius = radius;
        leftBottomRadius = radius;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 设置左上圆角半径
     */
    public void setLeftTopRadius(int leftTopRadius) {
        this.leftTopRadius = leftTopRadius;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 设置左下圆角半径
     */
    public void setLeftBottomRadius(int leftBottomRadius) {
        this.leftBottomRadius = leftBottomRadius;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 设置右下圆角半径
     */
    public void setRightBottomRadius(int rightBottomRadius) {
        this.rightBottomRadius = rightBottomRadius;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 设置右上圆角半径
     */
    public void setRightTopRadius(int rightTopRadius) {
        this.rightTopRadius = rightTopRadius;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 设置所有圆角半径（分别设置）
     */
    public void setCornerRadii(int leftTop, int rightTop, int rightBottom, int leftBottom) {
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        updateCornerClipEnabled();
        invalidate();
    }

    /**
     * 获取背景颜色
     */
    public int getBgColor() {
        return bgColor;
    }

    /**
     * 设置背景颜色
     */
    public void setBgColor(int color) {
        this.bgColor = color;
        this.hasBgColor = true;
        invalidate();
    }

    /**
     * 设置背景颜色（ARGB分量）
     */
    public void setBgColor(int alpha, int red, int green, int blue) {
        int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
        setBgColor(color);
    }

    /**
     * 设置描边
     */
    public void setStroke(int strokeWidth, int color) {
        this.strokeColor = color;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        invalidate();
    }

    /**
     * 获取边框颜色
     */
    public int getStrokeColor() {
        return strokeColor;
    }

    /**
     * 获取边框宽度
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * 获取Paint对象，用于更多自定义绘制
     */
    public Paint getPaint() {
        return mPaint;
    }

    /**
     * 设置画笔样式
     */
    public void setPaint(Paint paint) {
        if (paint != null) {
            this.mPaint = paint;
            invalidate();
        }
    }

    /**
     * 刷新圆角裁剪（当宽高变化时调用）
     */
    public void refreshCornerClip() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!cornerClipEnabled) {
            super.onDraw(canvas);
            return;
        }

        int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
        int maxRight = Math.max(rightTopRadius, rightBottomRadius);
        int minWidth = maxLeft + maxRight;
        int maxTop = Math.max(leftTopRadius, rightTopRadius);
        int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
        int minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            mPath.reset();
            mPath.moveTo(leftTopRadius, 0);
            mPath.lineTo(width - rightTopRadius, 0);
            mPath.quadTo(width, 0, width, rightTopRadius);

            mPath.lineTo(width, height - rightBottomRadius);
            mPath.quadTo(width, height, width - rightBottomRadius, height);

            mPath.lineTo(leftBottomRadius, height);
            mPath.quadTo(0, height, 0, height - leftBottomRadius);

            mPath.lineTo(0, leftTopRadius);
            mPath.quadTo(0, 0, leftTopRadius, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                canvas.clipPath(mPath);
            } else {
                canvas.clipPath(mPath, Region.Op.INTERSECT);
            }

            // 绘制背景色
            if (hasBgColor && Color.alpha(bgColor) > 0) {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(bgColor);
                canvas.drawPath(mPath, mPaint);
            }

            // 绘制描边
            if (hasStroke) {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(strokeWidth);
                mPaint.setColor(strokeColor);
                canvas.drawPath(mPath, mPaint);
            }
        }
        super.onDraw(canvas);
    }
}