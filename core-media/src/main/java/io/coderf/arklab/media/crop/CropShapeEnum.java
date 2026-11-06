package io.coderf.arklab.media.crop;

/**
 * 图片裁剪框形状枚举。
 * CIRCLE-圆形，SQUARE-正方形，RECTANGLE-长方形。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public enum CropShapeEnum {
    /**
     * 圆形裁剪框（输出圆形图片，透明背景 PNG）
     */
    CIRCLE,
    /**
     * 正方形裁剪框（宽高比 1:1）
     */
    SQUARE,
    /**
     * 长方形裁剪框（宽高比由 {@link ImageCropBuilder#setAspectRatio(int, int)} 配置，默认 4:3）
     */
    RECTANGLE
}
