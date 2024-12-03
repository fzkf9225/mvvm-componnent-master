package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;

/**
 * Created by CherishTang on 2019/5/30.
 * describe
 */
public class CircleTextView extends AppCompatTextView {
    private final Paint circlePaint;
    private final Paint backPaint;
    private final Paint textPaint;
    private int storkColor;
    private int circleBackColor;
    private float storkWidth;

    public CircleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        storkWidth = 0;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleTextView);
            storkColor = typedArray.getColor(R.styleable.CircleTextView_storkColor, ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CircleTextView_backColor, ContextCompat.getColor(context, R.color.white));
            storkWidth = typedArray.getDimension(R.styleable.CircleTextView_storkWidth, storkWidth);
            typedArray.recycle();
        } else {
            storkColor = ContextCompat.getColor(context, R.color.white);
            circleBackColor = ContextCompat.getColor(context, R.color.white);
        }
        if (storkWidth != 0) {
            circlePaint.setStrokeWidth(storkWidth);
            circlePaint.setColor(storkColor);
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
        int storkRadius;
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
        storkRadius = (int) (radius / 2 - storkWidth);
        radius = storkRadius - 1;
        if (storkWidth != 0) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, storkRadius, circlePaint);
        }
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, backPaint);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        canvas.drawText(getText().toString(), getWidth() / 2 - textPaint.measureText(getText().toString()) / 2, getHeight() / 2 - fontMetrics.descent + (fontMetrics.bottom - fontMetrics.top) / 2, textPaint);
    }

    public void setMyStorkColor(@ColorInt int color) {
        this.storkColor = color;
        circlePaint.setColor(storkColor);
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
