package io.coderf.arklab.common.widget.customview;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * 把历史 {@code radius / 四角 / bgColor / stroke} 映射到 Material3
 * {@link ShapeAppearanceModel} + {@link MaterialShapeDrawable}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
final class CornerShapeHelper {

    private CornerShapeHelper() {
    }

    @NonNull
    static ShapeAppearanceModel shapeModel(float leftTop, float rightTop,
                                           float rightBottom, float leftBottom) {
        return ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, leftTop)
                .setTopRightCorner(CornerFamily.ROUNDED, rightTop)
                .setBottomRightCorner(CornerFamily.ROUNDED, rightBottom)
                .setBottomLeftCorner(CornerFamily.ROUNDED, leftBottom)
                .build();
    }

    @NonNull
    static MaterialShapeDrawable createBackground(
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
    static ShapeAppearanceModel ovalModel() {
        return ShapeAppearanceModel.builder()
                .setAllCornerSizes(new RelativeCornerSize(0.5f))
                .build();
    }

    @NonNull
    static MaterialShapeDrawable createOvalBackground(
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

    /**
     * 兼容旧 {@code setGradientDrawable}：读出色值和圆角，不把 GradientDrawable 设为 background。
     */
    static void copyFromGradient(@Nullable GradientDrawable src,
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
}
