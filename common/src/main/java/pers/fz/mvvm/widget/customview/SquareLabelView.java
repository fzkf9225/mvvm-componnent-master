package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * created by fz on 2025/9/1 14:35
 * describe:左侧带方块的TextView，一般用于模块名文本
 */
public class SquareLabelView extends AppCompatTextView {
    private static final int DEFAULT_SQUARE_COLOR = Color.BLUE;
    private static final int DEFAULT_SQUARE_SIZE = 14; // dp
    private static final int DEFAULT_SQUARE_SPACING = 8; // dp

    private int squareColor;
    private float squareWidth;
    private float squareHeight;
    private int squareShape;
    private float squareCornerRadius;
    private int squarePosition;
    private float squareSpacing;

    private Paint squarePaint;
    private RectF squareRect;

    private boolean paddingAdjusted = false; // 标记是否已经调整过内边距

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

        // 在初始化时调整内边距，而不是在绘制时
        adjustPaddingIfNeeded();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 视图大小变化时重新调整内边距
        adjustPaddingIfNeeded();
    }

    private void adjustPaddingIfNeeded() {
        // 仅在start位置且未调整过内边距时进行调整
        if (squarePosition == 0 && !paddingAdjusted) {
            int newPaddingStart = (int) (getPaddingStart() + squareWidth + squareSpacing);
            setPaddingRelative(newPaddingStart, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            paddingAdjusted = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 先绘制方块
        drawSquare(canvas);
        // 再绘制文本（父类方法）
        super.onDraw(canvas);
    }

    private void drawSquare(Canvas canvas) {
        // 计算方块的位置
        float left, top;
        int width = getWidth();
        int height = getHeight();

        // 根据位置计算方块坐标
        top = switch (squarePosition) {
            case 1 -> {
                left = (width - squareWidth) / 2;
                yield getPaddingTop();
            }
            case 2 -> {
                left = width - getPaddingEnd() - squareWidth;
                yield (height - squareHeight) / 2;
            }
            case 3 -> {
                left = (width - squareWidth) / 2;
                yield height - getPaddingBottom() - squareHeight;
            }
            default -> {
                left = getPaddingStart();
                yield (height - squareHeight) / 2;
            }
        };

        // 对于start位置，需要考虑已经调整的内边距
        if (squarePosition == 0 && paddingAdjusted) {
            left = getPaddingStart() - squareWidth - squareSpacing;
        }

        squareRect.set(left, top, left + squareWidth, top + squareHeight);

        // 根据形状绘制方块
        switch (squareShape) {
            case 0: // rectangle
                canvas.drawRect(squareRect, squarePaint);
                break;
            case 1: // oval
                canvas.drawOval(squareRect, squarePaint);
                break;
            case 2: // round_rectangle
                canvas.drawRoundRect(squareRect, squareCornerRadius, squareCornerRadius, squarePaint);
                break;
        }
    }

    // 设置方块颜色
    public void setSquareColor(int color) {
        this.squareColor = color;
        squarePaint.setColor(color);
        invalidate();
    }

    // 设置方块大小
    public void setSquareSize(float width, float height) {
        this.squareWidth = width;
        this.squareHeight = height;
        paddingAdjusted = false; // 重置标记
        adjustPaddingIfNeeded();
        invalidate();
    }

    // 设置方块形状
    public void setSquareShape(int shape) {
        this.squareShape = shape;
        invalidate();
    }

    // 设置方块位置
    public void setSquarePosition(int position) {
        this.squarePosition = position;
        paddingAdjusted = false; // 重置标记
        adjustPaddingIfNeeded();
        invalidate();
    }

    // 设置方块与文本间距
    public void setSquareSpacing(float spacing) {
        this.squareSpacing = spacing;
        paddingAdjusted = false; // 重置标记
        adjustPaddingIfNeeded();
        invalidate();
    }

    // 获取当前方块颜色
    public int getSquareColor() {
        return squareColor;
    }

    // 获取方块宽度
    public float getSquareWidth() {
        return squareWidth;
    }

    // 获取方块高度
    public float getSquareHeight() {
        return squareHeight;
    }
}