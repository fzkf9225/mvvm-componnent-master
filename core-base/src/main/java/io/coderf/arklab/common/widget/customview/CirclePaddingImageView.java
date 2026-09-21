package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.shape.MaterialShapeDrawable;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.helper.CornerShapeHelper;

/**
 * 带内边距的圆形 ImageView：圆形只作为背景，src 仍按 {@code android:padding} 内缩。
 * 选中 / 按下背景走 Material3 oval {@code MaterialShapeDrawable}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:54
 */
public class CirclePaddingImageView extends AppCompatImageView {
    /**
     * 边框颜色
     */
    protected int borderColor;
    /**
     * 按下/选中边框颜色
     */
    protected int borderFocusColor;
    /**
     * 默认背景色
     */
    protected int defaultBackgroundColor;
    /**
     * 选中或按下时的背景颜色
     */
    protected int focusBackgroundColor;
    /**
     * 是否启用选中样式
     */
    protected boolean enableSelected;
    /**
     * 是否启用按下样式
     */
    protected boolean enablePressed;
    /**
     * 边框粗细
     */
    protected int borderWidth;
    /**
     * 按下时的背景
     */
    protected MaterialShapeDrawable bgFocusedDrawable;
    /**
     * 默认的背景
     */
    protected MaterialShapeDrawable bgDefaultDrawable;

    public CirclePaddingImageView(Context context) {
        super(context);
        init(null);
    }

    public CirclePaddingImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CirclePaddingImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleImageView);
            borderColor = a.getColor(R.styleable.CircleImageView_borderColor, 0xFFB4B4B4);
            borderFocusColor = a.getColor(R.styleable.CircleImageView_borderFocusColor, 0xFFFFFFFF);
            defaultBackgroundColor = a.getColor(R.styleable.CircleImageView_defaultBackgroundColor, 0xFF1F1F1F);
            focusBackgroundColor = a.getColor(R.styleable.CircleImageView_focusBackgroundColor, 0xFF0F0F0F);
            borderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_borderWidth, 0);
            enableSelected = a.getBoolean(R.styleable.CircleImageView_enableSelected, false);
            enablePressed = a.getBoolean(R.styleable.CircleImageView_enablePressed, false);
            a.recycle();
        } else {
            borderColor = 0xFFB4B4B4;
            borderFocusColor = 0xFFFFFFFF;
            defaultBackgroundColor = 0xFF1F1F1F;
            focusBackgroundColor = 0xFF0F0F0F;
        }

        rebuildBackgrounds();
        setBackground(bgDefaultDrawable);
    }

    private void rebuildBackgrounds() {
        bgDefaultDrawable = CornerShapeHelper.createOvalBackground(
                defaultBackgroundColor, borderWidth, borderColor);
        bgFocusedDrawable = CornerShapeHelper.createOvalBackground(
                focusBackgroundColor, borderWidth, borderFocusColor);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (!enableSelected) {
            return;
        }
        setBackground(selected ? bgFocusedDrawable : bgDefaultDrawable);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (!enableSelected && !enablePressed) {
            return;
        }
        if (enablePressed) {
            setBackground(pressed ? bgFocusedDrawable : bgDefaultDrawable);
            return;
        }
        if (pressed) {
            setBackground(bgFocusedDrawable);
        }
    }

    public void setBorderColor(int color) {
        borderColor = color;
        rebuildBackgrounds();
        setBackground(isSelected() ? bgFocusedDrawable : bgDefaultDrawable);
    }

    public void setBorderWidth(int width) {
        borderWidth = width;
        rebuildBackgrounds();
        setBackground(isSelected() ? bgFocusedDrawable : bgDefaultDrawable);
    }
}
