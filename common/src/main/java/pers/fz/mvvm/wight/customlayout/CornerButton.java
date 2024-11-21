package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatButton;

import pers.fz.mvvm.R;


/**
 * Created by CherishTang on 2019/5/31.
 * describe：自定义圆角矩形
 */
public class CornerButton extends AppCompatButton {
    private Paint circlePaint;
    private Paint backPaint;
    private Paint textPaint;
    private int storkColor = Color.WHITE;
    private int circleBackColor = Color.WHITE;
    private float storkWidth, raduis;

    public CornerButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setGravity(Gravity.CENTER);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        storkWidth = 0;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            storkColor = typedArray.getColor(R.styleable.CornerTextView_corner_storkColor, storkColor);
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_corner_backColor, circleBackColor);
            storkWidth = typedArray.getDimension(R.styleable.CornerTextView_corner_storkWidth, storkWidth);
            raduis = typedArray.getDimension(R.styleable.CornerTextView_corner_radius, 0);

            typedArray.recycle();
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(circleBackColor);
            gd.setCornerRadius(raduis);
            if (storkWidth > 0) {
                gd.setStroke((int) storkWidth, storkColor);
            }

            this.setBackground(gd);

        }
        if (storkWidth != 0) {
            circlePaint.setStrokeWidth(storkWidth);
            circlePaint.setColor(storkColor);
        }
        backPaint.setColor(circleBackColor);
        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());
    }

    public CornerButton(Context context) {
        this(context, null);

    }
    public void setBackGroundColor(@ColorInt int color) {
        GradientDrawable myGrad = (GradientDrawable) getBackground();
        myGrad.setColor(color);
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
