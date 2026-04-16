package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

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
    protected Paint mPaint;
    protected final Path mPath = new Path();

    public CornerImageView(Context context) {
        this(context, null);
        init(context, null);
    }

    public CornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
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
        int defaultRadius = DensityUtil.dp2px(context, 4);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Custom_Round_Image_View);
            radius = array.getDimensionPixelOffset(R.styleable.Custom_Round_Image_View_radius, defaultRadius);
            leftTopRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftTopRadius, defaultRadius);
            bgColor = array.getColor(R.styleable.Custom_Round_Image_View_bgColor, 0xFFe4e4e4);
            rightTopRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightTopRadius, defaultRadius);
            rightBottomRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightBottomRadius, defaultRadius);
            leftBottomRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftBottomRadius, defaultRadius);
            array.recycle();
        } else {
            bgColor = 0xFFe4e4e4;
        }

        //如果四个角的值没有设置，那么就使用通用的radius的值。
        if (defaultRadius == leftTopRadius) {
            leftTopRadius = radius;
        }
        if (defaultRadius == rightTopRadius) {
            rightTopRadius = radius;
        }
        if (defaultRadius == rightBottomRadius) {
            rightBottomRadius = radius;
        }
        if (defaultRadius == leftBottomRadius) {
            leftBottomRadius = radius;
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(bgColor);
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
        invalidate();
    }

    /**
     * 设置左上圆角半径
     */
    public void setLeftTopRadius(int leftTopRadius) {
        this.leftTopRadius = leftTopRadius;
        invalidate();
    }

    /**
     * 设置左下圆角半径
     */
    public void setLeftBottomRadius(int leftBottomRadius) {
        this.leftBottomRadius = leftBottomRadius;
        invalidate();
    }

    /**
     * 设置右下圆角半径
     */
    public void setRightBottomRadius(int rightBottomRadius) {
        this.rightBottomRadius = rightBottomRadius;
        invalidate();
    }

    /**
     * 设置右上圆角半径
     */
    public void setRightTopRadius(int rightTopRadius) {
        this.rightTopRadius = rightTopRadius;
        invalidate();
    }

    /**
     * 设置所有圆角半径（分别设置）
     * @param leftTop 左上角半径
     * @param rightTop 右上角半径
     * @param rightBottom 右下角半径
     * @param leftBottom 左下角半径
     */
    public void setCornerRadii(int leftTop, int rightTop, int rightBottom, int leftBottom) {
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
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
     * @param color 颜色值，如 0xFFe4e4e4
     */
    public void setBgColor(int color) {
        this.bgColor = color;
        if (mPaint != null) {
            mPaint.setColor(color);
        }
        invalidate();
    }

    /**
     * 设置背景颜色（ARGB分量）
     * @param alpha 透明度 0-255
     * @param red 红色 0-255
     * @param green 绿色 0-255
     * @param blue 蓝色 0-255
     */
    public void setBgColor(int alpha, int red, int green, int blue) {
        int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
        setBgColor(color);
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
        //这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
        int maxRight = Math.max(rightTopRadius, rightBottomRadius);
        int minWidth = maxLeft + maxRight;
        int maxTop = Math.max(leftTopRadius, rightTopRadius);
        int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
        int minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            mPath.reset();
            //四个角：右上，右下，左下，左上
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
            canvas.drawPath(mPath, mPaint);
        }
        super.onDraw(canvas);
    }
}