package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 圆形 {@link MaterialTextView}。官方控件没有圆形背景属性，
 * 用 Material3 {@code MaterialShapeDrawable} 铺椭圆（正方形时即为圆）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/3
 */
public class CircleTextView extends MaterialTextView {
    /**
     * 边框颜色
     */
    protected int strokeColor;
    /**
     * 背景颜色
     */
    protected int circleBackColor;
    /**
     * 边框宽度
     */
    protected float strokeWidth;

    public CircleTextView(@NonNull Context context) {
        this(context, null);
    }

    public CircleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        setGravity(Gravity.CENTER);
        setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setIncludeFontPadding(false);
        setClipToOutline(true);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleTextView);
            strokeColor = typedArray.getColor(R.styleable.CircleTextView_strokeColor,
                    ThemeAttrs.surface(context));
            circleBackColor = typedArray.getColor(R.styleable.CircleTextView_bgColor,
                    ThemeAttrs.surface(context));
            strokeWidth = typedArray.getDimension(R.styleable.CircleTextView_strokeWidth, 0);
            typedArray.recycle();
        } else {
            strokeColor = ThemeAttrs.surface(context);
            circleBackColor = ThemeAttrs.surface(context);
        }
        applyBackground();
    }

    private void applyBackground() {
        setBackground(CornerShapeHelper.createOvalBackground(circleBackColor, strokeWidth, strokeColor));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.max(getMeasuredWidth(), getMeasuredHeight());
        int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(spec, spec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getBackground() != null) {
            getBackground().setBounds(0, 0, getWidth(), getHeight());
            getBackground().draw(canvas);
        }
        CharSequence text = getText();
        if (text == null || text.length() == 0) {
            return;
        }
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float x = getWidth() / 2f;
        float y = getHeight() / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text.toString(), x, y, paint);
    }

    public void setStrokeColor(@ColorInt int color) {
        this.strokeColor = color;
        applyBackground();
    }

    public void setBackColor(@ColorInt int color) {
        this.circleBackColor = color;
        applyBackground();
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        applyBackground();
    }

    /**
     * 与旧 API 对齐，内部走 {@link #setTextColor(int)}。
     */
    public void setTextPaintColor(@ColorInt int color) {
        setTextColor(color);
    }
}
