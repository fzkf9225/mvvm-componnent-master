package io.coderf.arklab.googlegps.ui;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

/**
 * 模块内 UI 绘制辅助：dp 换算、代码构建圆角矩形背景等
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/6/15
 */
public final class UiUtil {

    private UiUtil() {
    }

    /**
     * 创建支持自定义四个角的圆角矩形 Drawable（无尺寸限制）
     * @param color 填充颜色
     * @param topLeftRadius 左上角半径
     * @param topRightRadius 右上角半径
     * @param bottomRightRadius 右下角半径
     * @param bottomLeftRadius 左下角半径
     * @return GradientDrawable
     */
    public static GradientDrawable createRectDrawable(
            int color,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadii(new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        });
        return drawable;
    }

    /**
     * 创建一个圆角矩形Drawable
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     * @param cornerRadius 圆角半径
     * @return 圆角矩形ShapeDrawable
     */
    public static ShapeDrawable createRectDrawable(
            int color,
            int width,
            int height,
            float cornerRadius) {
        float[] radii = new float[]{
                cornerRadius, cornerRadius, // 左上角
                cornerRadius, cornerRadius, // 右上角
                cornerRadius, cornerRadius, // 右下角
                cornerRadius, cornerRadius  // 左下角
        };
        RoundRectShape shape = new RoundRectShape(radii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setAntiAlias(true); // 启用抗锯齿
        shapeDrawable.setIntrinsicWidth(width);
        shapeDrawable.setIntrinsicHeight(height);
        return shapeDrawable;
    }

    /**
     * 创建矩形Drawable（无圆角）
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     */
    public static ShapeDrawable createRectDrawable(
            int color,
            int width,
            int height) {
        return createRectDrawable(color, width, height, 0f);
    }

    /**
     * convert dp to its equivalent px
     * <p>
     * 将dp转换为与之相等的px
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
