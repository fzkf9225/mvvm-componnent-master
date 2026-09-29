package io.coderf.arklab.common.widget.gallery.config;

import android.app.Application;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.widget.dialog.ImageSaveDialogConfig;

/**
 * 大图预览全局配置，在 {@link Application#onCreate()} 中初始化。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public final class PreviewGalleryConfig {

    /**
     * 全局缩放配置
     */
    private static volatile PreviewGalleryZoomConfig sGlobalZoomConfig = PreviewGalleryZoomConfig.defaults();
    /**
     * 全局保存图片弹窗配置，为空时使用内置默认
     */
    @Nullable
    private static volatile ImageSaveDialogConfig sGlobalImageSaveDialogConfig;

    private PreviewGalleryConfig() {
    }

    /**
     * 在 Application 中设置全局缩放配置，只需调用一次。
     *
     * @param zoomConfig 全局缩放配置，不能为空
     */
    public static void init(PreviewGalleryZoomConfig zoomConfig) {
        if (zoomConfig == null) {
            throw new IllegalArgumentException("zoomConfig cannot be null");
        }
        sGlobalZoomConfig = zoomConfig;
    }

    /**
     * @param application 保留 Application 参数，便于与项目其他 Config 初始化方式一致
     * @param zoomConfig  全局缩放配置，不能为空
     */
    public static void init(Application application, PreviewGalleryZoomConfig zoomConfig) {
        init(zoomConfig);
    }

    /**
     * 当前全局缩放配置
     */
    public static PreviewGalleryZoomConfig getGlobalZoomConfig() {
        return sGlobalZoomConfig;
    }

    /**
     * 设置全局「保存图片」弹窗样式；未设置时各字段使用 {@link ImageSaveDialogConfig} 内置默认。
     */
    /**
     * @param config 全局保存弹窗配置，null 表示恢复内置默认
     */
    public static void setGlobalImageSaveDialogConfig(@Nullable ImageSaveDialogConfig config) {
        sGlobalImageSaveDialogConfig = config;
    }

    @Nullable
    public static ImageSaveDialogConfig getGlobalImageSaveDialogConfig() {
        return sGlobalImageSaveDialogConfig;
    }
}
