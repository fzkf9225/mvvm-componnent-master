package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.ColorStateList;
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
    private int storkColor = Color.WHITE;
    private int circleBackColor = Color.WHITE;
    private final GradientDrawable gradientDrawable;
    private float radius,storkWidth;
    public CornerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTextView);
            storkColor = typedArray.getColor(R.styleable.CornerTextView_corner_storkColor, storkColor);
            circleBackColor = typedArray.getColor(R.styleable.CornerTextView_corner_backColor, circleBackColor);
            storkWidth = typedArray.getDimension(R.styleable.CornerTextView_corner_storkWidth, storkWidth);
            radius = typedArray.getDimension(R.styleable.CornerTextView_corner_radius, 0);
            typedArray.recycle();
        }
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(circleBackColor);
        gradientDrawable.setCornerRadius(radius);
        if (storkWidth > 0) {
            gradientDrawable.setStroke((int) storkWidth, storkColor);
        }
        this.setBackground(gradientDrawable);
    }

    public CornerButton(Context context) {
        this(context, null);
    }

    public void setStorkColor(@ColorInt int color) {
        this.storkColor = color;
        gradientDrawable.setStroke((int) storkWidth, this.storkColor);
        this.setBackground(gradientDrawable);
    }

    public void setStorkWidth(int width) {
        this.storkWidth = width;
        gradientDrawable.setStroke(this.storkColor, storkColor);
        this.setBackground(gradientDrawable);
    }

    public void setStork(@ColorInt int color,int width) {
        this.storkColor = color;
        this.storkWidth = width;
        gradientDrawable.setStroke(this.storkColor, this.storkColor);
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        gradientDrawable.setColor(circleBackColor);
        this.setBackground(gradientDrawable);
    }

    public void setBackColor(ColorStateList color) {
        gradientDrawable.setColor(color);
        this.setBackground(gradientDrawable);
    }

}
