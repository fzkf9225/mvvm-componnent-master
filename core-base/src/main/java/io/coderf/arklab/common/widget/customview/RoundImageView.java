package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.imageview.ShapeableImageView;

import io.coderf.arklab.common.R;

/**
 * 圆形头像：XML / 代码 API 不变（borderWidth / borderColor），
 * 裁剪与描边走 Material3 {@code ShapeableImageView}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class RoundImageView extends ShapeableImageView {

    protected static final ScaleType SCALE_TYPE = ScaleType.FIT_XY;
    protected static final int DEFAULT_BORDER_WIDTH = 0;
    protected static final int DEFAULT_BORDER_COLOR = Color.WHITE;

    protected int mBorderColor = DEFAULT_BORDER_COLOR;
    protected int mBorderWidth = DEFAULT_BORDER_WIDTH;

    public RoundImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        super.setScaleType(SCALE_TYPE);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);
            mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_borderWidth, DEFAULT_BORDER_WIDTH);
            mBorderColor = a.getColor(R.styleable.CircleImageView_borderColor, DEFAULT_BORDER_COLOR);
            a.recycle();
        }
        applyMaterialShape();
    }

    private void applyMaterialShape() {
        setShapeAppearanceModel(CornerShapeHelper.ovalModel());
        setStrokeWidth(mBorderWidth);
        setStrokeColor(ColorStateList.valueOf(mBorderColor));
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
        super.setScaleType(scaleType);
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }
        mBorderColor = borderColor;
        setStrokeColor(ColorStateList.valueOf(mBorderColor));
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }
        mBorderWidth = borderWidth;
        setStrokeWidth(mBorderWidth);
    }
}
