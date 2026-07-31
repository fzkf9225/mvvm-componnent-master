package io.coderf.arklab.media.crop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 图片裁剪入口帮助类。
 * 通过 {@link ImageCropBuilder} 配置后调用 {@link #start(Context)} 打开裁剪页。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:51
 */
public class ImageCropHelper {

    /**
     * 日志 TAG
     */
    public static final String TAG = ImageCropHelper.class.getSimpleName();

    /**
     * Intent Extra：源 Uri
     */
    public static final String EXTRA_SOURCE_URI = "extra_crop_source_uri";

    /**
     * Intent Extra：源文件路径
     */
    public static final String EXTRA_SOURCE_PATH = "extra_crop_source_path";

    /**
     * Intent Extra：裁剪形状 ordinal
     */
    public static final String EXTRA_CROP_SHAPE = "extra_crop_shape";

    /**
     * Intent Extra：输出目录
     */
    public static final String EXTRA_OUTPUT_DIR = "extra_crop_output_dir";

    /**
     * Intent Extra：输出文件名
     */
    public static final String EXTRA_OUTPUT_FILE_NAME = "extra_crop_output_file_name";

    /**
     * Intent Extra：宽高比 X
     */
    public static final String EXTRA_ASPECT_X = "extra_crop_aspect_x";

    /**
     * Intent Extra：宽高比 Y
     */
    public static final String EXTRA_ASPECT_Y = "extra_crop_aspect_y";

    /**
     * Intent Extra：最大输出边长
     */
    public static final String EXTRA_MAX_OUTPUT_SIZE = "extra_crop_max_output_size";

    /**
     * Intent Extra：是否允许缩放裁剪框
     */
    public static final String EXTRA_CROP_FRAME_SCALABLE = "extra_crop_frame_scalable";

    /**
     * Intent Extra：裁剪框描边颜色
     */
    public static final String EXTRA_BORDER_COLOR = "extra_crop_border_color";

    /**
     * Intent Extra：蒙版颜色
     */
    public static final String EXTRA_DIM_COLOR = "extra_crop_dim_color";

    /**
     * Intent Extra：网格线颜色
     */
    public static final String EXTRA_GRID_COLOR = "extra_crop_grid_color";

    /**
     * Intent Extra：确定按钮文字
     */
    public static final String EXTRA_CONFIRM_TEXT = "extra_crop_confirm_text";

    /**
     * Intent Extra：确定按钮文字颜色
     */
    public static final String EXTRA_CONFIRM_TEXT_COLOR = "extra_crop_confirm_text_color";

    /**
     * Intent Extra：确定按钮文字大小 sp
     */
    public static final String EXTRA_CONFIRM_TEXT_SIZE_SP = "extra_crop_confirm_text_size_sp";

    /**
     * Intent Extra：确定按钮背景色
     */
    public static final String EXTRA_CONFIRM_BG_COLOR = "extra_crop_confirm_bg_color";

    /**
     * Intent Extra：取消按钮文字
     */
    public static final String EXTRA_CANCEL_TEXT = "extra_crop_cancel_text";

    /**
     * Intent Extra：取消按钮文字颜色
     */
    public static final String EXTRA_CANCEL_TEXT_COLOR = "extra_crop_cancel_text_color";

    /**
     * Intent Extra：取消按钮文字大小 sp
     */
    public static final String EXTRA_CANCEL_TEXT_SIZE_SP = "extra_crop_cancel_text_size_sp";

    /**
     * Intent Extra：取消按钮背景色
     */
    public static final String EXTRA_CANCEL_BG_COLOR = "extra_crop_cancel_bg_color";

    /**
     * Intent Extra：长方形自由裁剪
     */
    public static final String EXTRA_FREE_CROP = "extra_crop_free_crop";

    /**
     * Intent Extra：裁剪框距边缘最小间距 dp
     */
    public static final String EXTRA_CROP_EDGE_INSET_DP = "extra_crop_edge_inset_dp";

    /**
     * Intent Extra：初始裁剪框比例
     */
    public static final String EXTRA_INITIAL_CROP_SCALE = "extra_crop_initial_scale";

    /**
     * Intent Extra：确定按钮宽度 dp
     */
    public static final String EXTRA_CONFIRM_BUTTON_WIDTH_DP = "extra_crop_confirm_btn_width_dp";

    /**
     * Intent Extra：确定按钮高度 dp
     */
    public static final String EXTRA_CONFIRM_BUTTON_HEIGHT_DP = "extra_crop_confirm_btn_height_dp";

    /**
     * Intent Extra：取消按钮宽度 dp
     */
    public static final String EXTRA_CANCEL_BUTTON_WIDTH_DP = "extra_crop_cancel_btn_width_dp";

    /**
     * Intent Extra：取消按钮高度 dp
     */
    public static final String EXTRA_CANCEL_BUTTON_HEIGHT_DP = "extra_crop_cancel_btn_height_dp";

    /**
     * 当前配置
     */
    private final ImageCropBuilder imageCropBuilder;

    /**
     * 弱引用回调，避免 Activity 泄漏；裁剪页通过静态方法取回
     */
    private static WeakReference<OnImageCropListener> listenerRef;

    /**
     * 构造裁剪帮助类
     *
     * @param imageCropBuilder 裁剪配置
     */
    protected ImageCropHelper(@NonNull ImageCropBuilder imageCropBuilder) {
        this.imageCropBuilder = imageCropBuilder;
    }

    /**
     * 获取裁剪配置
     *
     * @return ImageCropBuilder
     */
    @NonNull
    public ImageCropBuilder getImageCropBuilder() {
        return imageCropBuilder;
    }

    /**
     * 启动裁剪页面
     *
     * @param context 用于 startActivity 的上下文（建议 Activity）
     */
    public void start(@NonNull Context context) {
        listenerRef = new WeakReference<>(imageCropBuilder.getOnImageCropListener());
        Intent intent = new Intent(context, ImageCropActivity.class);
        if (imageCropBuilder.getSourceUri() != null) {
            intent.putExtra(EXTRA_SOURCE_URI, imageCropBuilder.getSourceUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else if (imageCropBuilder.getSourceFile() != null) {
            intent.putExtra(EXTRA_SOURCE_PATH, imageCropBuilder.getSourceFile().getAbsolutePath());
        }
        intent.putExtra(EXTRA_CROP_SHAPE, imageCropBuilder.getCropShape().ordinal());
        intent.putExtra(EXTRA_OUTPUT_DIR, imageCropBuilder.resolveOutputDir());
        intent.putExtra(EXTRA_OUTPUT_FILE_NAME, imageCropBuilder.getOutputFileName());
        intent.putExtra(EXTRA_ASPECT_X, imageCropBuilder.getAspectX());
        intent.putExtra(EXTRA_ASPECT_Y, imageCropBuilder.getAspectY());
        intent.putExtra(EXTRA_MAX_OUTPUT_SIZE, imageCropBuilder.getMaxOutputSize());
        intent.putExtra(EXTRA_CROP_FRAME_SCALABLE, imageCropBuilder.isCropFrameScalable());
        intent.putExtra(EXTRA_BORDER_COLOR, imageCropBuilder.getBorderColor());
        intent.putExtra(EXTRA_DIM_COLOR, imageCropBuilder.getDimColor());
        intent.putExtra(EXTRA_GRID_COLOR, imageCropBuilder.getGridColor());
        intent.putExtra(EXTRA_CONFIRM_TEXT, imageCropBuilder.getConfirmText());
        intent.putExtra(EXTRA_CONFIRM_TEXT_COLOR, imageCropBuilder.getConfirmTextColor());
        intent.putExtra(EXTRA_CONFIRM_TEXT_SIZE_SP, imageCropBuilder.getConfirmTextSizeSp());
        intent.putExtra(EXTRA_CONFIRM_BG_COLOR, imageCropBuilder.getConfirmBackgroundColor());
        intent.putExtra(EXTRA_CANCEL_TEXT, imageCropBuilder.getCancelText());
        intent.putExtra(EXTRA_CANCEL_TEXT_COLOR, imageCropBuilder.getCancelTextColor());
        intent.putExtra(EXTRA_CANCEL_TEXT_SIZE_SP, imageCropBuilder.getCancelTextSizeSp());
        intent.putExtra(EXTRA_CANCEL_BG_COLOR, imageCropBuilder.getCancelBackgroundColor());
        intent.putExtra(EXTRA_FREE_CROP, imageCropBuilder.isFreeCrop());
        intent.putExtra(EXTRA_CROP_EDGE_INSET_DP, imageCropBuilder.getCropEdgeInsetDp());
        intent.putExtra(EXTRA_INITIAL_CROP_SCALE, imageCropBuilder.getInitialCropScale());
        intent.putExtra(EXTRA_CONFIRM_BUTTON_WIDTH_DP, imageCropBuilder.getConfirmButtonWidthDp());
        intent.putExtra(EXTRA_CONFIRM_BUTTON_HEIGHT_DP, imageCropBuilder.getConfirmButtonHeightDp());
        intent.putExtra(EXTRA_CANCEL_BUTTON_WIDTH_DP, imageCropBuilder.getCancelButtonWidthDp());
        intent.putExtra(EXTRA_CANCEL_BUTTON_HEIGHT_DP, imageCropBuilder.getCancelButtonHeightDp());
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 取出当前回调（不清除）
     *
     * @return 回调，可能为空
     */
    @Nullable
    static OnImageCropListener peekListener() {
        return listenerRef == null ? null : listenerRef.get();
    }

    /**
     * 取出并清除当前回调
     *
     * @return 回调，可能为空
     */
    @Nullable
    static OnImageCropListener drainListener() {
        OnImageCropListener listener = peekListener();
        listenerRef = null;
        return listener;
    }

    /**
     * 将输出 File 转为可分享 Uri（优先 FileProvider）
     *
     * @param context 上下文
     * @param file    输出文件
     * @return Uri
     */
    @NonNull
    public static Uri fileToUri(@NonNull Context context, @NonNull File file) {
        try {
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".FileProvider", file);
        } catch (Exception e) {
            return Uri.fromFile(file);
        }
    }
}
