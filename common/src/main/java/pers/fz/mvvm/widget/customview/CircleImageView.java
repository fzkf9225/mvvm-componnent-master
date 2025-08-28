package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2019/5/30.
 * describe:圆形AppCompatTextView
 */
public class CircleImageView extends AppCompatImageView {
    private final Paint circlePaint;
    private final Paint backPaint;
    private int strokeColor;
    private int circleBackColor;
    private float strokeWidth;

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        strokeWidth = 0;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleTextView);
            strokeColor = typedArray.getColor(R.styleable.CircleTextView_strokeColor, ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CircleTextView_bgColor, ContextCompat.getColor(context, R.color.white));
            strokeWidth = typedArray.getDimension(R.styleable.CircleTextView_strokeWidth, strokeWidth);
            typedArray.recycle();
        } else {
            strokeColor = ContextCompat.getColor(context, R.color.white);
            circleBackColor = ContextCompat.getColor(context, R.color.white);
        }
        if (strokeWidth != 0) {
            circlePaint.setStrokeWidth(strokeWidth);
            circlePaint.setColor(strokeColor);
        }
        backPaint.setColor(circleBackColor);
    }

    public CircleImageView(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (strokeWidth != 0) {
            canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, (float) getWidth()/2, circlePaint);
        }
        canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, (float) getWidth()/2, backPaint);
    }

    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        circlePaint.setColor(strokeColor);
        invalidate();
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        backPaint.setColor(circleBackColor);
        invalidate();
    }
}
