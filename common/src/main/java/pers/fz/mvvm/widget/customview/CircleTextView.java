package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2019/5/30.
 * describe:圆形AppCompatTextView
 */
public class CircleTextView extends AppCompatTextView {
    private final Paint circlePaint;
    private final Paint backPaint;
    private final Paint textPaint;
    private int strokeColor;
    private int circleBackColor;
    private float strokeWidth;

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());
    }

    public CircleTextView(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int radius;
        int strokeRadius;
        int textWidth = (int) textPaint.measureText(getText().toString());
        if (width > height) {
            if (height > textWidth) {
                radius = height;
            } else {
                setHeight(textWidth + getPaddingTop() + getPaddingBottom());
                radius = textWidth;
            }
        } else {
            if (width > textWidth) {
                radius = width;
            } else {
                setWidth(textWidth + getPaddingRight() + getPaddingLeft());
                radius = textWidth;
            }
        }
        strokeRadius = (int) ((float) radius / 2 - strokeWidth);
        radius = strokeRadius - 1;
        if (strokeWidth != 0) {
            canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, strokeRadius, circlePaint);
        }
        canvas.drawCircle((float) getWidth() / 2, (float) getHeight() / 2, radius, backPaint);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        canvas.drawText(getText().toString(), (float) getWidth() / 2 - textPaint.measureText(getText().toString()) / 2, (float) getHeight() / 2 - fontMetrics.descent + (fontMetrics.bottom - fontMetrics.top) / 2, textPaint);
    }

    public void setMystrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        circlePaint.setColor(strokeColor);
        invalidate();
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        backPaint.setColor(circleBackColor);
        invalidate();
    }

    public void setMyTextColor(@ColorInt int color) {
        textPaint.setColor(color);
        invalidate();
    }
}
