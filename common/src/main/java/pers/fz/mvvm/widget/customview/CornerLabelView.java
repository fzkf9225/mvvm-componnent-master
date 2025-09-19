package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import pers.fz.mvvm.R;
import pers.fz.mvvm.utils.common.DensityUtil;

/**
 * Create by CherishTang on 2019/11/5
 * describe:自定义三角形角标view，可以自定义在四个角落和角标的宽度背景色
 */
public class CornerLabelView extends View {
    private float mHalfWidth;//View宽度的一半，取宽高的最小值，即：短的一边，然后取正方形
    private Paint mPaint;//角标画笔
    private TextPaint mTextPaint;//文字画笔
    private Path mPath;//角标路径

    private Position position = Position.RIGHT_TOP;//角标位置，0：右上角、1：右下角、2：左下角、3：左上角

    public enum Position {
        RIGHT_TOP(0),
        RIGHT_BOTTOM(1),
        LEFT_BOTTOM(2),
        LEFT_TOP(3);
        private final int value;

        public int getValue() {
            return value;
        }

        Position(int value) {
            this.value = value;
        }

        public static Position valueOf(int value) {
            for (Position p : Position.values()) {
                if (p.getValue() == value) {
                    return p;
                }
            }
            return null;
        }
    }

    //角标的显示边长
    private float sideLength;
    //字体大小
    private int textSize;
    //字体颜色
    private int textColor;
    private String text;
    //角标背景
    private int bgColor;
    //文字到斜边的距离
    private int marginLeanSide;

    public CornerLabelView(Context context) {
        this(context, null);
    }

    public CornerLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context.obtainStyledAttributes(attrs, R.styleable.CornerLabelView, 0, 0));
        init();
    }

    private void initAttrs(TypedArray typedArray) {
        if (typedArray == null) {
            position = Position.RIGHT_TOP;
            bgColor = ContextCompat.getColor(getContext(), R.color.themeColor);
            textColor = ContextCompat.getColor(getContext(), R.color.white);
            textSize = DensityUtil.sp2px(getContext(), 12f);
            sideLength = DensityUtil.dp2px(getContext(), 40);
            return;
        }
        position = Position.valueOf(typedArray.getInt(R.styleable.CornerLabelView_position, Position.RIGHT_TOP.value));
        sideLength = typedArray.getDimension(R.styleable.CornerLabelView_sideLength, DensityUtil.dp2px(getContext(), 40f));
        textSize = typedArray.getDimensionPixelSize(R.styleable.CornerLabelView_textSize, DensityUtil.sp2px(getContext(), 12f));
        textColor = typedArray.getColor(R.styleable.CornerLabelView_textColor, ContextCompat.getColor(getContext(), R.color.themeColor));
        text = typedArray.getString(R.styleable.CornerLabelView_text);
        bgColor = typedArray.getColor(R.styleable.CornerLabelView_bgColor, ContextCompat.getColor(getContext(), R.color.themeColor));
        marginLeanSide = typedArray.getDimensionPixelSize(R.styleable.CornerLabelView_marginLeanSide, 0);
        typedArray.recycle();
    }

    private void init() {
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(bgColor);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) (sideLength * 2), (int) (sideLength * 2));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize);
        } else if (widthSpecSize != heightSpecSize) {
            int size = Math.min(widthSpecSize, heightSpecSize);
            setMeasuredDimension(size, size);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHalfWidth = (float) Math.min(w, h) / 2;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //将原点移动到画布中心
        canvas.translate(mHalfWidth, mHalfWidth);
        //根据角标位置旋转画布
        canvas.rotate(position.value * 90);

        if (sideLength > mHalfWidth * 2) {
            sideLength = mHalfWidth * 2;
        }

        //绘制角标背景
        mPath.moveTo(-mHalfWidth, -mHalfWidth);
        mPath.lineTo(sideLength - mHalfWidth, -mHalfWidth);
        mPath.lineTo(mHalfWidth, mHalfWidth - sideLength);
        mPath.lineTo(mHalfWidth, mHalfWidth);
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        //绘制文字前画布旋转45度
        canvas.rotate(45);
        //角标实际高度
        int h1 = (int) (Math.sqrt(2) / 2.0 * sideLength);
        int h2 = (int) -(mTextPaint.ascent() + mTextPaint.descent());
        //文字绘制坐标
        int x = (int) -mTextPaint.measureText(text) / 2;
        int y;
        if (marginLeanSide >= 0) { //使用clv:margin_lean_side属性时
            if (position == Position.RIGHT_BOTTOM || position == Position.LEFT_BOTTOM) {
                if (h1 - (marginLeanSide - mTextPaint.ascent()) < (float) (h1 - h2) / 2) {
                    y = -(h1 - h2) / 2;
                } else {
                    y = (int) -(h1 - (marginLeanSide - mTextPaint.ascent()));
                }
            } else {
                if (marginLeanSide < mTextPaint.descent()) {
                    marginLeanSide = (int) mTextPaint.descent();
                }

                if (marginLeanSide > (h1 - h2) / 2) {
                    marginLeanSide = (h1 - h2) / 2;
                }
                y = -marginLeanSide;
            }
        } else { //默认情况下
            if (sideLength > mHalfWidth) {
                sideLength = mHalfWidth;
            }
            y = (int) (-Math.sqrt(2) / 2.0 * sideLength + h2) / 2;
        }

        //如果角标在右下、左下则进行画布平移、翻转，已解决绘制的文字显示问题
        if (position == Position.RIGHT_BOTTOM || position == Position.LEFT_BOTTOM) {
            canvas.translate(0, (float) (-Math.sqrt(2) / 2.0 * sideLength));
            canvas.scale(-1, -1);
        }
        //绘制文字
        canvas.drawText(text, x, y, mTextPaint);
    }

    @BindingAdapter({"bindText"})
    public static void bindText(CornerLabelView cornerLabelView, String text) {
        cornerLabelView.setText(text);
    }

    @BindingAdapter({"bindTextColor"})
    public static void bindTextColor(CornerLabelView cornerLabelView, int textColor) {
        cornerLabelView.setTextColor(textColor);
    }

    @BindingAdapter({"bindBgColor"})
    public static void bindBgColor(CornerLabelView cornerLabelView, int color) {
        cornerLabelView.setBgColor(color);
    }

    /**
     * 设置角标背景色
     * @param bgColorId 颜色资源id
     * @return this
     */
    public CornerLabelView setBgColorId(@ColorRes int bgColorId) {
        this.bgColor = ContextCompat.getColor(getContext(), bgColorId);
        mPaint.setColor(bgColor);
        invalidate();
        return this;
    }

    /**
     * 设置角标背景色
     *
     * @param bgColor 颜色值
     * @return this
     */
    public CornerLabelView setBgColor(int bgColor) {
        mPaint.setColor(bgColor);
        invalidate();
        return this;
    }

    /**
     * 设置文字颜色
     *
     * @param colorId 颜色资源id
     * @return this
     */
    public CornerLabelView setTextColorId(int colorId) {
        this.textColor = ContextCompat.getColor(getContext(), colorId);
        mTextPaint.setColor(textColor);
        invalidate();
        return this;
    }

    /**
     * 设置文字颜色
     *
     * @param color 颜色值
     * @return this
     */
    public CornerLabelView setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
        return this;
    }

    /**
     * 设置文字
     *
     * @param textId 文字资源id
     * @return this
     */
    public CornerLabelView setText(int textId) {
        this.text = getResources().getString(textId);
        invalidate();
        return this;
    }

    /**
     * 设置文字
     *
     * @param text 角标内容字符串类型
     * @return this
     */
    public CornerLabelView setText(String text) {
        this.text = text;
        invalidate();
        return this;
    }
}
