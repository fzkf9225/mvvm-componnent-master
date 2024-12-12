package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;


/**
 * Created by CherishTang on 2019/5/31.
 * describe：自定义圆角矩形
 */
public class CornerTextView extends AppCompatTextView {
    private final Paint circlePaint;
    private final Paint backPaint;
    private final Paint textPaint;
    private int strokeColor;
    private int circleBackColor;


    public CornerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setGravity(Gravity.CENTER);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float strokeWidth = 0;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            strokeColor = typedArray.getColor(R.styleable.CornerTextView_strokeColor, ContextCompat.getColor(context, R.color.white));
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_bgColor, ContextCompat.getColor(context, R.color.white));
            strokeWidth = typedArray.getDimension(R.styleable.CornerTextView_strokeWidth, strokeWidth);
            float radius = typedArray.getDimension(R.styleable.CornerTextView_radius, 0);
            typedArray.recycle();
            GradientDrawable gd = new GradientDrawable();//创建drawable
            gd.setColor(circleBackColor);
            gd.setCornerRadius(radius);
            if (strokeWidth > 0) {
                gd.setStroke((int) strokeWidth, strokeColor);
            }
            this.setBackground(gd);
        }else{
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

    public CornerTextView(Context context) {
        this(context, null);

    }
    public void setBackGroundColor(@ColorInt int color) {
        GradientDrawable myGrad = (GradientDrawable) getBackground();
        myGrad.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        int height = getHeight();
//        int width = getWidth();
//        int radius;
//        int strokeRadius;
//        int textWidth = (int) textPaint.measureText(getText().toString());
//        if (height > textWidth) {
//            radius = height;
//        } else {
//            setHeight(textWidth + getPaddingTop() + getPaddingBottom());
//            radius = textWidth;
//        }
//        strokeRadius = (int) (radius / 2 - strokeWidth);
//        radius = strokeRadius - 1;
//        if (strokeWidth != 0)
//            canvas.drawRect(getWidth() / 2, getHeight() / 2, strokeRadius, circlePaint);
//        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, backPaint);
//        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//
//        canvas.drawText(getText().toString(), getWidth() / 2 - textPaint.measureText(getText().toString()) / 2, getHeight() / 2 - fontMetrics.descent + (fontMetrics.bottom - fontMetrics.top) / 2, textPaint);

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
