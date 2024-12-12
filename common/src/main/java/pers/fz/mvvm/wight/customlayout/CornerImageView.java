package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Create by CherishTang on 2019/9/3 0003
 * describe:圆角ImageView
 */
public class CornerImageView extends AppCompatImageView {
    float width, height;
    private int radius;
    private int leftTopRadius;
    private int rightTopRadius;
    private int rightBottomRadius;
    private int leftBottomRadius;
    private Paint mPaint;
    private final Path mPath = new Path();

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
        int defaultRadius = DensityUtil.dp2px(context, 3);
        int imageBg;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Custom_Round_Image_View);
            radius = array.getDimensionPixelOffset(R.styleable.Custom_Round_Image_View_radius, defaultRadius);
            leftTopRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftTopRadius, defaultRadius);
            imageBg = array.getColor(R.styleable.Custom_Round_Image_View_bgColor, 0xFFe4e4e4);
            rightTopRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightTopRadius, defaultRadius);
            rightBottomRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_rightBottomRadius, defaultRadius);
            leftBottomRadius = array.getDimensionPixelSize(R.styleable.Custom_Round_Image_View_leftBottomRadius, defaultRadius);
            array.recycle();
        } else {
            imageBg = 0xFFe4e4e4;
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
        mPaint.setColor(imageBg);
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setLeftTopRadius(int leftTopRadius) {
        this.leftTopRadius = leftTopRadius;
    }

    public void setLeftBottomRadius(int leftBottomRadius) {
        this.leftBottomRadius = leftBottomRadius;
    }

    public void setRightBottomRadius(int rightBottomRadius) {
        this.rightBottomRadius = rightBottomRadius;
    }

    public void setRightTopRadius(int rightTopRadius) {
        this.rightTopRadius = rightTopRadius;
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
