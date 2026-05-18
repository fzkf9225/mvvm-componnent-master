package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * created by fz on 2025/9/1 14:35
 * describe:左侧带方块的TextView，一般用于模块名文本
 */
public class SquareLabelView extends AppCompatTextView {
    /**
     * 默认方块颜色
     */
    protected static final int DEFAULT_SQUARE_COLOR = Color.BLUE;
    /**
     * 默认方块大小
     */
    protected static final int DEFAULT_SQUARE_SIZE = 14; // dp
    /**
     * 默认方块间距
     */
    protected static final int DEFAULT_SQUARE_SPACING = 8; // dp
    /**
     * 方块颜色
     */
    protected int squareColor;
    /**
     * 方块宽度
     */
    protected float squareWidth;
    /**
     * 方块高度
     */
    protected float squareHeight;
    /**
     * 方块形状，rectangle-0，oval-1，round_rectangle-2
     */
    protected int squareShape;
    /**
     * 方块圆角半径
     */
    protected float squareCornerRadius;
    /**
     * 方块位置,0-左，1-上，2-右，3-下
     */
    protected int squarePosition;
    /**
     * 方块间距
     */
    protected float squareSpacing;

    private Paint squarePaint;
    private RectF squareRect;

    private int basePaddingStart;
    private int basePaddingTop;
    private int basePaddingEnd;
    private int basePaddingBottom;
    private boolean basePaddingCaptured;

    public SquareLabelView(Context context) {
        this(context, null);
    }

    public SquareLabelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareLabelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.SquareLabelView, defStyleAttr, 0));
    }

    private void init(TypedArray typedArray) {
        // 初始化默认值
        squareColor = DEFAULT_SQUARE_COLOR;
        squareWidth = DensityUtil.dp2px(getContext(), DEFAULT_SQUARE_SIZE);
        squareHeight = DensityUtil.dp2px(getContext(), DEFAULT_SQUARE_SIZE);
        squareShape = 0; // rectangle
        squareCornerRadius = 0;
        squarePosition = 0; // start
        squareSpacing = DensityUtil.dp2px(getContext(), DEFAULT_SQUARE_SPACING);

        // 获取自定义属性
        if (typedArray != null) {
            squareColor = typedArray.getColor(R.styleable.SquareLabelView_squareColor, squareColor);
            squareWidth = typedArray.getDimension(R.styleable.SquareLabelView_squareWidth, squareWidth);
            squareHeight = typedArray.getDimension(R.styleable.SquareLabelView_squareHeight, squareHeight);
            squareShape = typedArray.getInt(R.styleable.SquareLabelView_squareShape, squareShape);
            squareCornerRadius = typedArray.getDimension(R.styleable.SquareLabelView_squareCornerRadius, squareCornerRadius);
            squarePosition = typedArray.getInt(R.styleable.SquareLabelView_squarePosition, squarePosition);
            squareSpacing = typedArray.getDimension(R.styleable.SquareLabelView_squareSpacing, squareSpacing);
            typedArray.recycle();
        }

        // 初始化Paint
        squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        squarePaint.setColor(squareColor);
        squarePaint.setStyle(Paint.Style.FILL);

        squareRect = new RectF();

        applySquarePadding();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        applySquarePadding();
    }

    private void captureBasePaddingIfNeeded() {
        if (!basePaddingCaptured) {
            basePaddingStart = getPaddingStart();
            basePaddingTop = getPaddingTop();
            basePaddingEnd = getPaddingEnd();
            basePaddingBottom = getPaddingBottom();
            basePaddingCaptured = true;
        }
    }

    private void applySquarePadding() {
        captureBasePaddingIfNeeded();
        int extraStart = 0;
        int extraTop = 0;
        int extraEnd = 0;
        int extraBottom = 0;
        if (hasVisibleSquare()) {
            switch (squarePosition) {
                case 1:
                    extraTop = (int) (squareHeight + squareSpacing);
                    break;
                case 2:
                    extraEnd = (int) (squareWidth + squareSpacing);
                    break;
                case 3:
                    extraBottom = (int) (squareHeight + squareSpacing);
                    break;
                default:
                    extraStart = (int) (squareWidth + squareSpacing);
                    break;
            }
        }
        setPaddingRelative(
                basePaddingStart + extraStart,
                basePaddingTop + extraTop,
                basePaddingEnd + extraEnd,
                basePaddingBottom + extraBottom
        );
    }

    private boolean hasVisibleSquare() {
        return squareWidth > 0 || squareHeight > 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSquare(canvas);
        super.onDraw(canvas);
    }

    private void drawSquare(Canvas canvas) {
        if (!hasVisibleSquare()) {
            return;
        }

        float left;
        float top;
        int width = getWidth();
        int height = getHeight();

        switch (squarePosition) {
            case 1:
                left = (width - squareWidth) / 2f;
                top = basePaddingTop;
                break;
            case 2:
                left = width - basePaddingEnd - squareWidth;
                top = (height - squareHeight) / 2f;
                break;
            case 3:
                left = (width - squareWidth) / 2f;
                top = height - basePaddingBottom - squareHeight;
                break;
            default:
                left = basePaddingStart;
                top = (height - squareHeight) / 2f;
                break;
        }

        squareRect.set(left, top, left + squareWidth, top + squareHeight);

        switch (squareShape) {
            case 0:
                canvas.drawRect(squareRect, squarePaint);
                break;
            case 1:
                canvas.drawOval(squareRect, squarePaint);
                break;
            case 2:
                canvas.drawRoundRect(squareRect, squareCornerRadius, squareCornerRadius, squarePaint);
                break;
            default:
                break;
        }
    }

    public void setSquareColor(int color) {
        this.squareColor = color;
        squarePaint.setColor(color);
        invalidate();
    }

    public void setSquareSize(float width, float height) {
        this.squareWidth = width;
        this.squareHeight = height;
        applySquarePadding();
        invalidate();
    }

    public void setSquareShape(int shape) {
        this.squareShape = shape;
        invalidate();
    }

    public void setSquarePosition(int position) {
        this.squarePosition = position;
        applySquarePadding();
        invalidate();
    }

    public void setSquareSpacing(float spacing) {
        this.squareSpacing = spacing;
        applySquarePadding();
        invalidate();
    }

    public int getSquareColor() {
        return squareColor;
    }

    public float getSquareWidth() {
        return squareWidth;
    }

    public float getSquareHeight() {
        return squareHeight;
    }
}
