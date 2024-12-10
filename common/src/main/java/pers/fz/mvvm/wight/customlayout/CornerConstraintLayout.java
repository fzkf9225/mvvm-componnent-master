package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/8/14 10:12
 * describe :
 */
public class CornerConstraintLayout extends ConstraintLayout {
    private int circleBackColor;
    private float radius;

    public CornerConstraintLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_corner_backColor, ContextCompat.getColor(context, R.color.white));
            radius = typedArray.getDimension(R.styleable.CornerTextView_corner_radius, DensityUtil.dp2px(getContext(),8));
            typedArray.recycle();
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(circleBackColor);
            gd.setCornerRadius(radius);
            this.setBackground(gd);
        } else {
            circleBackColor = ContextCompat.getColor(context, R.color.white);
            radius = DensityUtil.dp2px(context, 8f);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(circleBackColor);
            gd.setCornerRadius(radius);
            this.setBackground(gd);
        }
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(circleBackColor);
        gd.setCornerRadius(radius);
        this.setBackground(gd);
    }

    public void setBgColor(int color) {
        this.circleBackColor = color;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(this.circleBackColor);
        gd.setCornerRadius(this.radius);
        this.setBackground(gd);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(this.circleBackColor);
        gd.setCornerRadius(this.radius);
        this.setBackground(gd);
    }

    public void setBgColorAndRadius(int color, float radius) {
        this.radius = radius;
        this.circleBackColor = color;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(this.circleBackColor);
        gd.setCornerRadius(this.radius);
        this.setBackground(gd);
    }
}
