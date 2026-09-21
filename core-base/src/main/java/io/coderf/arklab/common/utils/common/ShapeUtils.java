package io.coderf.arklab.common.utils.common;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.widget.customview.round.ShapeBuilder;

/**
 * 圆角 / 描边 / 填充 / 渐变 统一入口。
 * <p>
 * <b>纯色 + 圆角 + 描边</b>：走 Material3 {@link MaterialShapeDrawable}（{@link CornerShapeHelper}）。<br>
 * <b>渐变背景</b>：走 {@link ShapeBuilder}（{@link GradientDrawable}）。<br>
 * 业务侧优先本类；也可直接使用 {@code app:shapeAppearanceOverlay} / 主题默认值。
 * <p>
 * 历史 API：
 * <ul>
 *   <li>{@link CornerShapeHelper} — 仍可用，本类对其做转发</li>
 *   <li>{@link ShapeBuilder} — 仅建议用于渐变；纯色场景请改用本类</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
public final class ShapeUtils {

    private ShapeUtils() {
    }

    // ---------- ShapeAppearanceModel ----------

    @NonNull
    public static ShapeAppearanceModel shapeModel(float radius) {
        return CornerShapeHelper.shapeModel(radius);
    }

    @NonNull
    public static ShapeAppearanceModel shapeModel(float leftTop, float rightTop,
                                                   float rightBottom, float leftBottom) {
        return CornerShapeHelper.shapeModel(leftTop, rightTop, rightBottom, leftBottom);
    }

    @NonNull
    public static ShapeAppearanceModel ovalModel() {
        return CornerShapeHelper.ovalModel();
    }

    // ---------- MaterialShapeDrawable 工厂 ----------

    @NonNull
    public static MaterialShapeDrawable createBackground(
            float leftTop, float rightTop, float rightBottom, float leftBottom,
            boolean hasBgColor, @ColorInt int bgColor,
            boolean hasStroke, float strokeWidth, @ColorInt int strokeColor) {
        return CornerShapeHelper.createBackground(
                leftTop, rightTop, rightBottom, leftBottom,
                hasBgColor, bgColor, hasStroke, strokeWidth, strokeColor);
    }

    @NonNull
    public static MaterialShapeDrawable createBackground(float radius, @ColorInt int bgColor) {
        return createBackground(radius, radius, radius, radius, true, bgColor, false, 0f, Color.TRANSPARENT);
    }

    @NonNull
    public static MaterialShapeDrawable createBackground(
            float radius, @ColorInt int bgColor, float strokeWidth, @ColorInt int strokeColor) {
        return createBackground(radius, radius, radius, radius, true, bgColor,
                strokeWidth > 0, strokeWidth, strokeColor);
    }

    @NonNull
    public static MaterialShapeDrawable createOvalBackground(
            @ColorInt int bgColor, float strokeWidth, @ColorInt int strokeColor) {
        return CornerShapeHelper.createOvalBackground(bgColor, strokeWidth, strokeColor);
    }

    // ---------- 应用到 View ----------

    public static void apply(@NonNull View view, float radius, @ColorInt int bgColor) {
        CornerShapeHelper.apply(view, radius, bgColor);
    }

    public static void apply(@NonNull View view, float radius, @ColorInt int bgColor,
                             float strokeWidth, @ColorInt int strokeColor) {
        CornerShapeHelper.apply(view, radius, bgColor, strokeWidth, strokeColor);
    }

    public static void apply(@NonNull View view,
                             float leftTop, float rightTop, float rightBottom, float leftBottom,
                             boolean hasBgColor, @ColorInt int bgColor,
                             boolean hasStroke, float strokeWidth, @ColorInt int strokeColor) {
        CornerShapeHelper.apply(view, leftTop, rightTop, rightBottom, leftBottom,
                hasBgColor, bgColor, hasStroke, strokeWidth, strokeColor);
    }

    public static void apply(@NonNull ShapeableImageView imageView, float radius) {
        CornerShapeHelper.apply(imageView, radius);
    }

    public static void apply(@NonNull ShapeableImageView imageView, float radius, @ColorInt int bgColor) {
        CornerShapeHelper.apply(imageView, radius, bgColor);
    }

    public static void apply(@NonNull MaterialButton button, float radius) {
        CornerShapeHelper.apply(button, radius);
    }

    public static void apply(@NonNull MaterialButton button, float radius, @ColorInt int bgColor) {
        CornerShapeHelper.apply(button, radius, bgColor);
    }

    public static void apply(@NonNull MaterialButton button, float radius, @ColorInt int bgColor,
                             float strokeWidth, @ColorInt int strokeColor) {
        CornerShapeHelper.apply(button, radius, bgColor, strokeWidth, strokeColor);
    }

    public static void applyTint(@NonNull MaterialButton button, @ColorInt int bgColor) {
        CornerShapeHelper.applyTint(button, bgColor);
    }

    /**
     * 读取 XML {@code ShapeView} 属性并铺背景（与 {@link CornerShapeHelper#applyFromAttributes} 相同）。
     */
    public static void applyFromAttributes(@NonNull View view, @Nullable AttributeSet attrs) {
        CornerShapeHelper.applyFromAttributes(view, attrs);
    }

    public static void copyFromGradient(@Nullable GradientDrawable src,
                                        @NonNull float[] outRadii,
                                        @NonNull int[] outFill,
                                        @NonNull boolean[] outHasFill) {
        CornerShapeHelper.copyFromGradient(src, outRadii, outFill, outHasFill);
    }

    // ---------- 渐变（委托 ShapeBuilder） ----------

    /**
     * 线性渐变背景（TOP_BOTTOM）。
     */
    public static void applyGradient(@NonNull View view, int startColor, int centerColor, int endColor) {
        ShapeBuilder.create()
                .Gradient(startColor, centerColor, endColor)
                .build(view);
    }

    /**
     * 按角度线性渐变（角度须为 45 的整数倍）。
     */
    public static void applyGradient(@NonNull View view, int angle,
                                     int startColor, int centerColor, int endColor) {
        ShapeBuilder.create()
                .Gradient(angle, startColor, centerColor, endColor)
                .build(view);
    }

    /**
     * 带圆角的线性渐变。
     */
    public static void applyGradient(@NonNull View view, float radius,
                                     int startColor, int centerColor, int endColor) {
        ShapeBuilder.create()
                .Radius(radius)
                .Gradient(startColor, centerColor, endColor)
                .build(view);
    }

    /**
     * 需要完整链式配置（虚线描边、径向渐变等）时直接使用 {@link ShapeBuilder}。
     */
    @NonNull
    public static ShapeBuilder gradientBuilder() {
        return ShapeBuilder.create();
    }

    /**
     * 仅设置纯色圆角背景的便捷构建（内部仍走 MaterialShapeDrawable）。
     */
    public static void applySolid(@NonNull View view, float radius, @ColorInt int color) {
        apply(view, radius, color);
    }

    /**
     * 清除背景。
     */
    public static void clear(@NonNull View view) {
        view.setBackground(null);
    }
}
