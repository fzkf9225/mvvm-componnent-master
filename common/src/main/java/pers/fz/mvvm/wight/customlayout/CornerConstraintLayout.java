package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2023/8/14 10:12
 * describe :
 */
public class CornerConstraintLayout extends ConstraintLayout {
    private Paint backPaint;
    private int circleBackColor = Color.WHITE;
    private float raduis;

    public CornerConstraintLayout(@NonNull Context context) {
        super(context);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs){
        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setStyle(Paint.Style.FILL);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_corner_backColor, circleBackColor);
            raduis = typedArray.getDimension(R.styleable.CornerTextView_corner_radius, 0);

            typedArray.recycle();
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(circleBackColor);
            gd.setCornerRadius(raduis);

            this.setBackground(gd);

        }
        backPaint.setColor(circleBackColor);
    }

    public void setBackGroundColor(@ColorInt int color) {
        GradientDrawable myGrad = (GradientDrawable) getBackground();
        myGrad.setColor(color);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        backPaint.setColor(circleBackColor);
        invalidate();
    }

}
