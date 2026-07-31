package io.coderf.arklab.media.crop;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 图片裁剪配置构建器。
 * 支持输入 File / Uri，可配置输出目录、文件名、裁剪框形状与长宽比。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public class ImageCropBuilder {

    /**
     * 默认输出子目录名（位于 Android/data/包名/files/ 下）
     */
    public static final String DEFAULT_OUTPUT_DIR_NAME = "cutting";

    /**
     * 默认输出文件扩展名（圆形为 png，其余为 jpg）
     */
    public static final String DEFAULT_OUTPUT_EXTENSION_JPG = ".jpg";

    /**
     * 圆形裁剪默认输出扩展名
     */
    public static final String DEFAULT_OUTPUT_EXTENSION_PNG = ".png";

    /**
     * 默认长方形宽高比分子（宽）
     */
    public static final int DEFAULT_ASPECT_X = 4;

    /**
     * 默认长方形宽高比分母（高）
     */
    public static final int DEFAULT_ASPECT_Y = 3;

    /**
     * 默认输出边长上限（像素），0 表示不限制、按裁剪区域原尺寸输出
     */
    public static final int DEFAULT_MAX_OUTPUT_SIZE = 1080;

    /**
     * 默认裁剪框描边颜色
     */
    public static final int DEFAULT_BORDER_COLOR = 0xFFFFFFFF;

    /**
     * 默认蒙版颜色
     */
    public static final int DEFAULT_DIM_COLOR = 0x99000000;

    /**
     * 默认确定按钮背景色
     */
    public static final int DEFAULT_CONFIRM_BG_COLOR = 0xFF2D8CF0;

    /**
     * 默认取消按钮背景色
     */
    public static final int DEFAULT_CANCEL_BG_COLOR = 0x33FFFFFF;

    /**
     * 默认按钮文字颜色
     */
    public static final int DEFAULT_BUTTON_TEXT_COLOR = 0xFFFFFFFF;

    /**
     * 默认按钮文字大小（sp）
     */
    public static final float DEFAULT_BUTTON_TEXT_SIZE_SP = 16f;

    /**
     * 默认按钮高度（dp），0 表示沿用布局默认
     */
    public static final float DEFAULT_BUTTON_HEIGHT_DP = 48f;

    /**
     * 默认按钮宽度（dp），0 表示均分占满（layout_weight）
     */
    public static final float DEFAULT_BUTTON_WIDTH_DP = 0f;

    /**
     * 裁剪框距屏幕边缘的默认最小间距（dp），避免与系统返回手势冲突
     */
    public static final float DEFAULT_CROP_EDGE_INSET_DP = 16f;

    /**
     * 初始裁剪框相对可用区域的默认比例（0~1），小于 1 表示不铺满
     */
    public static final float DEFAULT_INITIAL_CROP_SCALE = 0.7f;

    /**
     * 上下文
     */
    private final Context mContext;

    /**
     * 输入图片 Uri
     */
    @Nullable
    private Uri sourceUri;

    /**
     * 输入图片 File
     */
    @Nullable
    private File sourceFile;

    /**
     * 裁剪框形状，默认正方形
     */
    private CropShapeEnum cropShape = CropShapeEnum.SQUARE;

    /**
     * 输出目录绝对路径，为空时使用默认 cutting 目录
     */
    @Nullable
    private String outputDir;

    /**
     * 输出文件名（可含扩展名），为空时自动生成
     */
    @Nullable
    private String outputFileName;

    /**
     * 长方形裁剪宽高比-宽
     */
    private int aspectX = DEFAULT_ASPECT_X;

    /**
     * 长方形裁剪宽高比-高
     */
    private int aspectY = DEFAULT_ASPECT_Y;

    /**
     * 长方形是否自由裁剪（不锁定宽高比），仅 {@link CropShapeEnum#RECTANGLE} 生效
     */
    private boolean freeCrop = false;

    /**
     * 裁剪框距屏幕边缘的最小间距（dp）
     */
    private float cropEdgeInsetDp = DEFAULT_CROP_EDGE_INSET_DP;

    /**
     * 初始裁剪框相对可用区域比例（0~1）
     */
    private float initialCropScale = DEFAULT_INITIAL_CROP_SCALE;

    /**
     * 输出图片最长边限制，单位像素；小于等于 0 表示不缩放
     */
    private int maxOutputSize = DEFAULT_MAX_OUTPUT_SIZE;

    /**
     * 是否允许用户缩放/拖动裁剪框，默认 true
     */
    private boolean cropFrameScalable = true;

    /**
     * 裁剪框描边颜色
     */
    private int borderColor = DEFAULT_BORDER_COLOR;

    /**
     * 蒙版颜色
     */
    private int dimColor = DEFAULT_DIM_COLOR;

    /**
     * 九宫格辅助线颜色，0 表示跟随描边色自动推导
     */
    private int gridColor = 0;

    /**
     * 确定按钮文字，空则使用默认「确定」
     */
    @Nullable
    private String confirmText;

    /**
     * 确定按钮文字颜色
     */
    private int confirmTextColor = DEFAULT_BUTTON_TEXT_COLOR;

    /**
     * 确定按钮文字大小，单位 sp
     */
    private float confirmTextSizeSp = DEFAULT_BUTTON_TEXT_SIZE_SP;

    /**
     * 确定按钮背景色
     */
    private int confirmBackgroundColor = DEFAULT_CONFIRM_BG_COLOR;

    /**
     * 确定按钮宽度（dp），0 表示均分占满
     */
    private float confirmButtonWidthDp = DEFAULT_BUTTON_WIDTH_DP;

    /**
     * 确定按钮高度（dp），0 表示使用默认高度
     */
    private float confirmButtonHeightDp = DEFAULT_BUTTON_HEIGHT_DP;

    /**
     * 取消按钮文字，空则使用默认「取消」
     */
    @Nullable
    private String cancelText;

    /**
     * 取消按钮文字颜色
     */
    private int cancelTextColor = DEFAULT_BUTTON_TEXT_COLOR;

    /**
     * 取消按钮文字大小，单位 sp
     */
    private float cancelTextSizeSp = DEFAULT_BUTTON_TEXT_SIZE_SP;

    /**
     * 取消按钮背景色
     */
    private int cancelBackgroundColor = DEFAULT_CANCEL_BG_COLOR;

    /**
     * 取消按钮宽度（dp），0 表示均分占满
     */
    private float cancelButtonWidthDp = DEFAULT_BUTTON_WIDTH_DP;

    /**
     * 取消按钮高度（dp），0 表示使用默认高度
     */
    private float cancelButtonHeightDp = DEFAULT_BUTTON_HEIGHT_DP;

    /**
     * 裁剪结果回调
     */
    @Nullable
    private OnImageCropListener onImageCropListener;

    /**
     * 构造裁剪配置构建器
     *
     * @param context 上下文，不可为空
     */
    public ImageCropBuilder(@NotNull Context context) {
        this.mContext = context.getApplicationContext() != null
                ? context.getApplicationContext() : context;
    }

    /**
     * 获取上下文
     *
     * @return Context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 设置输入图片 Uri
     *
     * @param sourceUri 图片 Uri
     * @return this
     */
    public ImageCropBuilder setSourceUri(@Nullable Uri sourceUri) {
        this.sourceUri = sourceUri;
        if (sourceUri != null) {
            this.sourceFile = null;
        }
        return this;
    }

    /**
     * 获取输入图片 Uri
     *
     * @return 输入 Uri，可能为空
     */
    @Nullable
    public Uri getSourceUri() {
        return sourceUri;
    }

    /**
     * 设置输入图片 File
     *
     * @param sourceFile 图片文件
     * @return this
     */
    public ImageCropBuilder setSourceFile(@Nullable File sourceFile) {
        this.sourceFile = sourceFile;
        if (sourceFile != null) {
            this.sourceUri = null;
        }
        return this;
    }

    /**
     * 获取输入图片 File
     *
     * @return 输入文件，可能为空
     */
    @Nullable
    public File getSourceFile() {
        return sourceFile;
    }

    /**
     * 设置裁剪框形状
     *
     * @param cropShape 圆形 / 正方形 / 长方形
     * @return this
     */
    public ImageCropBuilder setCropShape(@NonNull CropShapeEnum cropShape) {
        this.cropShape = cropShape;
        return this;
    }

    /**
     * 获取裁剪框形状
     *
     * @return 裁剪形状
     */
    @NonNull
    public CropShapeEnum getCropShape() {
        return cropShape;
    }

    /**
     * 设置输出目录（绝对路径）。不设置时默认：Android/data/包名/files/cutting/
     *
     * @param outputDir 输出目录绝对路径
     * @return this
     */
    public ImageCropBuilder setOutputDir(@Nullable String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * 获取配置的输出目录（可能为空，表示使用默认目录）
     *
     * @return 输出目录，可能为空
     */
    @Nullable
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * 解析最终输出目录：优先自定义，否则 Android/data/包名/files/cutting/
     *
     * @return 输出目录绝对路径
     */
    @NonNull
    public String resolveOutputDir() {
        if (!TextUtils.isEmpty(outputDir)) {
            return outputDir;
        }
        File filesDir = mContext.getExternalFilesDir(null);
        if (filesDir == null) {
            filesDir = mContext.getFilesDir();
        }
        return new File(filesDir, DEFAULT_OUTPUT_DIR_NAME).getAbsolutePath();
    }

    /**
     * 设置输出文件名（可含扩展名）。不设置时自动按时间戳生成
     *
     * @param outputFileName 文件名，如 crop_avatar.png
     * @return this
     */
    public ImageCropBuilder setOutputFileName(@Nullable String outputFileName) {
        this.outputFileName = outputFileName;
        return this;
    }

    /**
     * 获取输出文件名配置
     *
     * @return 文件名，可能为空
     */
    @Nullable
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * 设置长方形裁剪宽高比（仅 {@link CropShapeEnum#RECTANGLE} 且非自由裁剪时生效）
     *
     * @param aspectX 宽比例，必须大于 0
     * @param aspectY 高比例，必须大于 0
     * @return this
     */
    public ImageCropBuilder setAspectRatio(int aspectX, int aspectY) {
        if (aspectX > 0 && aspectY > 0) {
            this.aspectX = aspectX;
            this.aspectY = aspectY;
            this.freeCrop = false;
        }
        return this;
    }

    /**
     * 设置长方形是否自由裁剪（不锁定宽高比）。
     * 仅 {@link CropShapeEnum#RECTANGLE} 生效；圆形/正方形始终 1:1。
     *
     * @param freeCrop true 自由裁剪
     * @return this
     */
    public ImageCropBuilder setFreeCrop(boolean freeCrop) {
        this.freeCrop = freeCrop;
        return this;
    }

    /**
     * 是否自由裁剪（不锁定宽高比）
     *
     * @return true 自由裁剪
     */
    public boolean isFreeCrop() {
        return freeCrop && cropShape == CropShapeEnum.RECTANGLE;
    }

    /**
     * 设置裁剪框距屏幕边缘的最小间距（dp）。
     * 用于避免裁剪框贴边时与系统返回手势冲突。
     *
     * @param cropEdgeInsetDp 间距 dp，小于 0 按 0 处理
     * @return this
     */
    public ImageCropBuilder setCropEdgeInsetDp(float cropEdgeInsetDp) {
        this.cropEdgeInsetDp = Math.max(0f, cropEdgeInsetDp);
        return this;
    }

    /**
     * 获取裁剪框距屏幕边缘的最小间距（dp）
     *
     * @return 间距 dp
     */
    public float getCropEdgeInsetDp() {
        return cropEdgeInsetDp;
    }

    /**
     * 设置初始裁剪框相对可用区域的比例（0~1），默认 0.7，不铺满。
     *
     * @param initialCropScale 比例，超出范围会被限制到 (0,1]
     * @return this
     */
    public ImageCropBuilder setInitialCropScale(float initialCropScale) {
        if (initialCropScale <= 0f) {
            this.initialCropScale = DEFAULT_INITIAL_CROP_SCALE;
        } else {
            this.initialCropScale = Math.min(1f, initialCropScale);
        }
        return this;
    }

    /**
     * 获取初始裁剪框比例
     *
     * @return 0~1
     */
    public float getInitialCropScale() {
        return initialCropScale;
    }

    /**
     * 获取宽高比分子（宽）
     *
     * @return aspectX
     */
    public int getAspectX() {
        if (cropShape == CropShapeEnum.CIRCLE || cropShape == CropShapeEnum.SQUARE) {
            return 1;
        }
        return aspectX;
    }

    /**
     * 获取宽高比分母（高）
     *
     * @return aspectY
     */
    public int getAspectY() {
        if (cropShape == CropShapeEnum.CIRCLE || cropShape == CropShapeEnum.SQUARE) {
            return 1;
        }
        return aspectY;
    }

    /**
     * 设置输出最长边像素上限
     *
     * @param maxOutputSize 最长边像素，小于等于 0 表示不限制
     * @return this
     */
    public ImageCropBuilder setMaxOutputSize(int maxOutputSize) {
        this.maxOutputSize = maxOutputSize;
        return this;
    }

    /**
     * 获取输出最长边像素上限
     *
     * @return 最长边像素
     */
    public int getMaxOutputSize() {
        return maxOutputSize;
    }

    /**
     * 设置是否允许用户缩放/拖动裁剪框
     *
     * @param cropFrameScalable true 允许（默认 true）
     * @return this
     */
    public ImageCropBuilder setCropFrameScalable(boolean cropFrameScalable) {
        this.cropFrameScalable = cropFrameScalable;
        return this;
    }

    /**
     * 是否允许用户缩放/拖动裁剪框
     *
     * @return true 允许
     */
    public boolean isCropFrameScalable() {
        return cropFrameScalable;
    }

    /**
     * 设置裁剪框描边颜色
     *
     * @param borderColor 颜色
     * @return this
     */
    public ImageCropBuilder setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * 获取裁剪框描边颜色
     *
     * @return 颜色
     */
    @ColorInt
    public int getBorderColor() {
        return borderColor;
    }

    /**
     * 设置蒙版颜色（裁剪框外遮罩）
     *
     * @param dimColor 颜色，建议带透明度
     * @return this
     */
    public ImageCropBuilder setDimColor(@ColorInt int dimColor) {
        this.dimColor = dimColor;
        return this;
    }

    /**
     * 获取蒙版颜色
     *
     * @return 颜色
     */
    @ColorInt
    public int getDimColor() {
        return dimColor;
    }

    /**
     * 设置九宫格辅助线颜色；传 0 表示跟随描边色自动推导
     *
     * @param gridColor 颜色
     * @return this
     */
    public ImageCropBuilder setGridColor(@ColorInt int gridColor) {
        this.gridColor = gridColor;
        return this;
    }

    /**
     * 获取九宫格辅助线颜色
     *
     * @return 颜色，0 表示未自定义
     */
    @ColorInt
    public int getGridColor() {
        return gridColor;
    }

    /**
     * 设置确定按钮文字
     *
     * @param confirmText 文字，空则使用默认
     * @return this
     */
    public ImageCropBuilder setConfirmText(@Nullable String confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    /**
     * 获取确定按钮文字
     *
     * @return 文字，可能为空
     */
    @Nullable
    public String getConfirmText() {
        return confirmText;
    }

    /**
     * 设置确定按钮文字颜色
     *
     * @param confirmTextColor 颜色
     * @return this
     */
    public ImageCropBuilder setConfirmTextColor(@ColorInt int confirmTextColor) {
        this.confirmTextColor = confirmTextColor;
        return this;
    }

    /**
     * 获取确定按钮文字颜色
     *
     * @return 颜色
     */
    @ColorInt
    public int getConfirmTextColor() {
        return confirmTextColor;
    }

    /**
     * 设置确定按钮文字大小
     *
     * @param confirmTextSizeSp 文字大小，单位 sp，小于等于 0 忽略
     * @return this
     */
    public ImageCropBuilder setConfirmTextSizeSp(float confirmTextSizeSp) {
        if (confirmTextSizeSp > 0) {
            this.confirmTextSizeSp = confirmTextSizeSp;
        }
        return this;
    }

    /**
     * 获取确定按钮文字大小（sp）
     *
     * @return 文字大小
     */
    public float getConfirmTextSizeSp() {
        return confirmTextSizeSp;
    }

    /**
     * 设置确定按钮背景色
     *
     * @param confirmBackgroundColor 背景色
     * @return this
     */
    public ImageCropBuilder setConfirmBackgroundColor(@ColorInt int confirmBackgroundColor) {
        this.confirmBackgroundColor = confirmBackgroundColor;
        return this;
    }

    /**
     * 获取确定按钮背景色
     *
     * @return 背景色
     */
    @ColorInt
    public int getConfirmBackgroundColor() {
        return confirmBackgroundColor;
    }

    /**
     * 设置确定按钮宽度（dp）
     *
     * @param confirmButtonWidthDp 宽度 dp；小于等于 0 表示均分占满
     * @return this
     */
    public ImageCropBuilder setConfirmButtonWidthDp(float confirmButtonWidthDp) {
        this.confirmButtonWidthDp = confirmButtonWidthDp;
        return this;
    }

    /**
     * 获取确定按钮宽度（dp）
     *
     * @return 宽度 dp，0 表示均分
     */
    public float getConfirmButtonWidthDp() {
        return confirmButtonWidthDp;
    }

    /**
     * 设置确定按钮高度（dp）
     *
     * @param confirmButtonHeightDp 高度 dp；小于等于 0 使用默认 48dp
     * @return this
     */
    public ImageCropBuilder setConfirmButtonHeightDp(float confirmButtonHeightDp) {
        this.confirmButtonHeightDp = confirmButtonHeightDp;
        return this;
    }

    /**
     * 获取确定按钮高度（dp）
     *
     * @return 高度 dp
     */
    public float getConfirmButtonHeightDp() {
        return confirmButtonHeightDp;
    }

    /**
     * 同时设置确定按钮宽高（dp）
     *
     * @param widthDp  宽度，0 均分
     * @param heightDp 高度，0 默认
     * @return this
     */
    public ImageCropBuilder setConfirmButtonSizeDp(float widthDp, float heightDp) {
        return setConfirmButtonWidthDp(widthDp).setConfirmButtonHeightDp(heightDp);
    }

    /**
     * 设置取消按钮文字
     *
     * @param cancelText 文字，空则使用默认
     * @return this
     */
    public ImageCropBuilder setCancelText(@Nullable String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    /**
     * 获取取消按钮文字
     *
     * @return 文字，可能为空
     */
    @Nullable
    public String getCancelText() {
        return cancelText;
    }

    /**
     * 设置取消按钮文字颜色
     *
     * @param cancelTextColor 颜色
     * @return this
     */
    public ImageCropBuilder setCancelTextColor(@ColorInt int cancelTextColor) {
        this.cancelTextColor = cancelTextColor;
        return this;
    }

    /**
     * 获取取消按钮文字颜色
     *
     * @return 颜色
     */
    @ColorInt
    public int getCancelTextColor() {
        return cancelTextColor;
    }

    /**
     * 设置取消按钮文字大小
     *
     * @param cancelTextSizeSp 文字大小，单位 sp，小于等于 0 忽略
     * @return this
     */
    public ImageCropBuilder setCancelTextSizeSp(float cancelTextSizeSp) {
        if (cancelTextSizeSp > 0) {
            this.cancelTextSizeSp = cancelTextSizeSp;
        }
        return this;
    }

    /**
     * 获取取消按钮文字大小（sp）
     *
     * @return 文字大小
     */
    public float getCancelTextSizeSp() {
        return cancelTextSizeSp;
    }

    /**
     * 设置取消按钮背景色
     *
     * @param cancelBackgroundColor 背景色
     * @return this
     */
    public ImageCropBuilder setCancelBackgroundColor(@ColorInt int cancelBackgroundColor) {
        this.cancelBackgroundColor = cancelBackgroundColor;
        return this;
    }

    /**
     * 获取取消按钮背景色
     *
     * @return 背景色
     */
    @ColorInt
    public int getCancelBackgroundColor() {
        return cancelBackgroundColor;
    }

    /**
     * 设置取消按钮宽度（dp）
     *
     * @param cancelButtonWidthDp 宽度 dp；小于等于 0 表示均分占满
     * @return this
     */
    public ImageCropBuilder setCancelButtonWidthDp(float cancelButtonWidthDp) {
        this.cancelButtonWidthDp = cancelButtonWidthDp;
        return this;
    }

    /**
     * 获取取消按钮宽度（dp）
     *
     * @return 宽度 dp，0 表示均分
     */
    public float getCancelButtonWidthDp() {
        return cancelButtonWidthDp;
    }

    /**
     * 设置取消按钮高度（dp）
     *
     * @param cancelButtonHeightDp 高度 dp；小于等于 0 使用默认 48dp
     * @return this
     */
    public ImageCropBuilder setCancelButtonHeightDp(float cancelButtonHeightDp) {
        this.cancelButtonHeightDp = cancelButtonHeightDp;
        return this;
    }

    /**
     * 获取取消按钮高度（dp）
     *
     * @return 高度 dp
     */
    public float getCancelButtonHeightDp() {
        return cancelButtonHeightDp;
    }

    /**
     * 同时设置取消按钮宽高（dp）
     *
     * @param widthDp  宽度，0 均分
     * @param heightDp 高度，0 默认
     * @return this
     */
    public ImageCropBuilder setCancelButtonSizeDp(float widthDp, float heightDp) {
        return setCancelButtonWidthDp(widthDp).setCancelButtonHeightDp(heightDp);
    }

    /**
     * 设置裁剪结果回调
     *
     * @param onImageCropListener 回调
     * @return this
     */
    public ImageCropBuilder setOnImageCropListener(@Nullable OnImageCropListener onImageCropListener) {
        this.onImageCropListener = onImageCropListener;
        return this;
    }

    /**
     * 获取裁剪结果回调
     *
     * @return 回调，可能为空
     */
    @Nullable
    public OnImageCropListener getOnImageCropListener() {
        return onImageCropListener;
    }

    /**
     * 是否已配置有效输入源
     *
     * @return true 表示 File 或 Uri 至少有一个有效
     */
    public boolean hasValidSource() {
        return sourceUri != null || (sourceFile != null && sourceFile.exists());
    }

    /**
     * 创建裁剪帮助类并校验必要参数
     *
     * @return ImageCropHelper
     */
    public ImageCropHelper builder() {
        if (!hasValidSource()) {
            throw new IllegalArgumentException("please set source File or Uri");
        }
        return new ImageCropHelper(this);
    }
}
