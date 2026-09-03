package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;

import io.coderf.arklab.common.R;

/**
 * 把圆角 / 填充 / 描边映射到 Material3 {@link ShapeAppearanceModel}。
 * 新代码不要再包一层自定义 View，直接对官方控件调用这里的 apply。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/3 11:38
 */
public final class CornerShapeHelper {

    private CornerShapeHelper() {
    }

    @NonNull
    public static ShapeAppearanceModel shapeModel(float radius) {
        return shapeModel(radius, radius, radius, radius);
    }

    @NonNull
    public static ShapeAppearanceModel shapeModel(float leftTop, float rightTop,
                                                   float rightBottom, float leftBottom) {
        return ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, leftTop)
                .setTopRightCorner(CornerFamily.ROUNDED, rightTop)
                .setBottomRightCorner(CornerFamily.ROUNDED, rightBottom)
                .setBottomLeftCorner(CornerFamily.ROUNDED, leftBottom)
                .build();
    }

    @NonNull
    public static ShapeAppearanceModel ovalModel() {
        return ShapeAppearanceModel.builder()
                .setAllCornerSizes(new RelativeCornerSize(0.5f))
                .build();
    }

    @NonNull
    public static MaterialShapeDrawable createBackground(
            float leftTop, float rightTop, float rightBottom, float leftBottom,
            boolean hasBgColor, @ColorInt int bgColor,
            boolean hasStroke, float strokeWidth, @ColorInt int strokeColor) {
        MaterialShapeDrawable drawable = new MaterialShapeDrawable(
                shapeModel(leftTop, rightTop, rightBottom, leftBottom));
        drawable.setFillColor(ColorStateList.valueOf(hasBgColor ? bgColor : Color.TRANSPARENT));
        if (hasStroke && strokeWidth > 0) {
            drawable.setStroke(strokeWidth, ColorStateList.valueOf(strokeColor));
        } else {
            drawable.setStroke(0f, ColorStateList.valueOf(Color.TRANSPARENT));
        }
        drawable.setElevation(0f);
        return drawable;
    }

    @NonNull
    public static MaterialShapeDrawable createOvalBackground(
            @ColorInt int bgColor, float strokeWidth, @ColorInt int strokeColor) {
        MaterialShapeDrawable drawable = new MaterialShapeDrawable(ovalModel());
        drawable.setFillColor(ColorStateList.valueOf(bgColor));
        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, ColorStateList.valueOf(strokeColor));
        } else {
            drawable.setStroke(0f, ColorStateList.valueOf(Color.TRANSPARENT));
        }
        drawable.setElevation(0f);
        return drawable;
    }

    public static void apply(@NonNull View view, float radius, @ColorInt int bgColor) {
        apply(view, radius, radius, radius, radius, true, bgColor, false, 0f, Color.TRANSPARENT);
    }

    public static void apply(@NonNull View view, float radius, @ColorInt int bgColor,
                             float strokeWidth, @ColorInt int strokeColor) {
        apply(view, radius, radius, radius, radius, true, bgColor, strokeWidth > 0, strokeWidth, strokeColor);
    }

    public static void apply(@NonNull View view,
                             float leftTop, float rightTop, float rightBottom, float leftBottom,
                             boolean hasBgColor, @ColorInt int bgColor,
                             boolean hasStroke, float strokeWidth, @ColorInt int strokeColor) {
        view.setBackground(createBackground(
                leftTop, rightTop, rightBottom, leftBottom,
                hasBgColor, bgColor, hasStroke, strokeWidth, strokeColor));
    }

    public static void apply(@NonNull ShapeableImageView imageView, float radius) {
        imageView.setShapeAppearanceModel(shapeModel(radius));
    }

    public static void apply(@NonNull ShapeableImageView imageView, float radius, @ColorInt int bgColor) {
        apply(imageView, radius);
        imageView.setBackground(createBackground(
                radius, radius, radius, radius, true, bgColor, false, 0f, Color.TRANSPARENT));
    }

    public static void apply(@NonNull MaterialButton button, float radius) {
        flushButton(button);
        button.setCornerRadius(Math.round(radius));
    }

    public static void apply(@NonNull MaterialButton button, float radius, @ColorInt int bgColor) {
        apply(button, radius, bgColor, 0f, Color.TRANSPARENT);
    }

    public static void apply(@NonNull MaterialButton button, float radius, @ColorInt int bgColor,
                             float strokeWidth, @ColorInt int strokeColor) {
        apply(button, radius);
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        if (strokeWidth > 0) {
            button.setStrokeWidth(Math.round(strokeWidth));
            button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        } else {
            button.setStrokeWidth(0);
        }
    }

    public static void applyTint(@NonNull MaterialButton button, @ColorInt int bgColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
    }

    /**
     * 读取 XML 中的 radius / bgColor / stroke*，给 ConstraintLayout 等官方容器铺 MaterialShapeDrawable。
     */
    public static void applyFromAttributes(@NonNull View view, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        Context context = view.getContext();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeView);
        boolean hasStrokeColor = typedArray.hasValue(R.styleable.ShapeView_strokeColor);
        boolean hasStrokeWidthAttr = typedArray.hasValue(R.styleable.ShapeView_strokeWidth);
        boolean hasBgColor = typedArray.hasValue(R.styleable.ShapeView_bgColor);
        boolean hasRadiusAttr = typedArray.hasValue(R.styleable.ShapeView_radius);
        boolean hasLeftTop = typedArray.hasValue(R.styleable.ShapeView_leftTopRadius);
        boolean hasRightTop = typedArray.hasValue(R.styleable.ShapeView_rightTopRadius);
        boolean hasRightBottom = typedArray.hasValue(R.styleable.ShapeView_rightBottomRadius);
        boolean hasLeftBottom = typedArray.hasValue(R.styleable.ShapeView_leftBottomRadius);

        int strokeColor = hasStrokeColor ? typedArray.getColor(R.styleable.ShapeView_strokeColor, 0) : 0;
        int bgColor = hasBgColor ? typedArray.getColor(R.styleable.ShapeView_bgColor, 0) : Color.TRANSPARENT;
        float strokeWidth = hasStrokeWidthAttr ? typedArray.getDimension(R.styleable.ShapeView_strokeWidth, 0) : 0;
        float radius = hasRadiusAttr ? typedArray.getDimension(R.styleable.ShapeView_radius, 0) : 0;
        float leftTop = hasLeftTop ? typedArray.getDimension(R.styleable.ShapeView_leftTopRadius, 0) : radius;
        float rightTop = hasRightTop ? typedArray.getDimension(R.styleable.ShapeView_rightTopRadius, 0) : radius;
        float rightBottom = hasRightBottom
                ? typedArray.getDimension(R.styleable.ShapeView_rightBottomRadius, 0) : radius;
        float leftBottom = hasLeftBottom
                ? typedArray.getDimension(R.styleable.ShapeView_leftBottomRadius, 0) : radius;
        typedArray.recycle();

        boolean hasStroke = strokeWidth > 0 && hasStrokeColor;
        if (hasBgColor || hasRadiusAttr || hasLeftTop || hasRightTop || hasRightBottom || hasLeftBottom
                || hasStrokeWidthAttr || hasStrokeColor) {
            apply(view, leftTop, rightTop, rightBottom, leftBottom,
                    hasBgColor, bgColor, hasStroke, strokeWidth, strokeColor);
        }
    }

    public static void copyFromGradient(@Nullable GradientDrawable src,
                                        @NonNull float[] outRadii,
                                        @NonNull int[] outFill,
                                        @NonNull boolean[] outHasFill) {
        if (src == null || outRadii.length < 4) {
            return;
        }
        ColorStateList fill = src.getColor();
        if (fill != null) {
            outHasFill[0] = true;
            outFill[0] = fill.getDefaultColor();
        }
        float[] radii = src.getCornerRadii();
        if (radii != null && radii.length >= 8) {
            outRadii[0] = radii[0];
            outRadii[1] = radii[2];
            outRadii[2] = radii[4];
            outRadii[3] = radii[6];
        } else {
            float r = src.getCornerRadius();
            outRadii[0] = r;
            outRadii[1] = r;
            outRadii[2] = r;
            outRadii[3] = r;
        }
    }

    private static void flushButton(@NonNull MaterialButton button) {
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
    }
}
