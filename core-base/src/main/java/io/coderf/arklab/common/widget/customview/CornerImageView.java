package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.google.android.material.imageview.ShapeableImageView;

import io.coderf.arklab.common.R;

/**
 * 圆角 ImageView：XML / 代码 API 不变，裁剪与描边走 Material3 {@code ShapeableImageView}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class CornerImageView extends ShapeableImageView {
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
        updateCornerClipEnabled();
        if (cornerClipEnabled || hasBgColor || hasStroke) {
            applyMaterialShape();
        }
    }

    private void updateCornerClipEnabled() {
        cornerClipEnabled = leftTopRadius > 0 || rightTopRadius > 0
                || rightBottomRadius > 0 || leftBottomRadius > 0;
    }

    private void applyMaterialShape() {
        setShapeAppearanceModel(CornerShapeHelper.shapeModel(
                leftTopRadius, rightTopRadius, rightBottomRadius, leftBottomRadius));
        if (hasBgColor) {
            setBackground(CornerShapeHelper.createBackground(
                    leftTopRadius, rightTopRadius, rightBottomRadius, leftBottomRadius,
                    true, bgColor, false, 0, Color.TRANSPARENT));
        }
        if (hasStroke) {
            setStrokeWidth(strokeWidth);
            setStrokeColor(ColorStateList.valueOf(strokeColor));
        } else {
            setStrokeWidth(0f);
            setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
        }
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
        applyMaterialShape();
    }

    /**
     * 设置左上圆角半径
     */
    public void setLeftTopRadius(int leftTopRadius) {
        this.leftTopRadius = leftTopRadius;
        updateCornerClipEnabled();
        applyMaterialShape();
    }

    /**
     * 设置左下圆角半径
     */
    public void setLeftBottomRadius(int leftBottomRadius) {
        this.leftBottomRadius = leftBottomRadius;
        updateCornerClipEnabled();
        applyMaterialShape();
    }

    /**
     * 设置右下圆角半径
     */
    public void setRightBottomRadius(int rightBottomRadius) {
        this.rightBottomRadius = rightBottomRadius;
        updateCornerClipEnabled();
        applyMaterialShape();
    }

    /**
     * 设置右上圆角半径
     */
    public void setRightTopRadius(int rightTopRadius) {
        this.rightTopRadius = rightTopRadius;
        updateCornerClipEnabled();
        applyMaterialShape();
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
        applyMaterialShape();
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
        applyMaterialShape();
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
        applyMaterialShape();
    }

    /**
     * 一次性设置描边、背景色和统一圆角，仅触发一次 apply。
     *
     * @param strokeColor 描边颜色
     * @param strokeWidth 描边宽度（像素）
     * @param bgColor     背景颜色
     * @param radius      四个角统一圆角半径
     */
    public void setStrokeBgColorAndRadius(int strokeColor, float strokeWidth, int bgColor, int radius) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        this.bgColor = bgColor;
        this.hasBgColor = true;
        this.radius = radius;
        this.leftTopRadius = radius;
        this.rightTopRadius = radius;
        this.rightBottomRadius = radius;
        this.leftBottomRadius = radius;
        updateCornerClipEnabled();
        applyMaterialShape();
    }

    /**
     * 一次性设置描边、背景色和四个角圆角，仅触发一次 apply。
     *
     * @param strokeColor  描边颜色
     * @param strokeWidth  描边宽度（像素）
     * @param bgColor      背景颜色
     * @param leftTop      左上角圆角半径
     * @param rightTop     右上角圆角半径
     * @param rightBottom  右下角圆角半径
     * @param leftBottom   左下角圆角半径
     */
    public void setStrokeBgColorAndCornerRadii(int strokeColor, float strokeWidth, int bgColor,
                                               int leftTop, int rightTop,
                                               int rightBottom, int leftBottom) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.hasStroke = strokeWidth > 0;
        this.bgColor = bgColor;
        this.hasBgColor = true;
        this.leftTopRadius = leftTop;
        this.rightTopRadius = rightTop;
        this.rightBottomRadius = rightBottom;
        this.leftBottomRadius = leftBottom;
        updateCornerClipEnabled();
        applyMaterialShape();
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
        }
    }

    /**
     * 刷新圆角裁剪（当宽高变化时调用）
     */
    public void refreshCornerClip() {
        applyMaterialShape();
    }
}
