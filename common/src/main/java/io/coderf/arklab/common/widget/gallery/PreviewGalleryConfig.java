package io.coderf.arklab.common.widget.gallery;

import android.app.Application;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.widget.dialog.ImageSaveDialogConfig;

/**
 * 大图预览全局配置，在 {@link Application#onCreate()} 中初始化。
 */
public final class PreviewGalleryConfig {

    private static volatile PreviewGalleryZoomConfig sGlobalZoomConfig = PreviewGalleryZoomConfig.defaults();
    @Nullable
    private static volatile ImageSaveDialogConfig sGlobalImageSaveDialogConfig;

    private PreviewGalleryConfig() {
    }

    /**
     * 在 Application 中设置全局缩放配置（仅需调用一次）。
     */
    public static void init(PreviewGalleryZoomConfig zoomConfig) {
        if (zoomConfig == null) {
            throw new IllegalArgumentException("zoomConfig cannot be null");
        }
        sGlobalZoomConfig = zoomConfig;
    }

    /**
     * @param application 保留 Application 参数，便于与项目其他 Config 初始化方式一致
     */
    public static void init(Application application, PreviewGalleryZoomConfig zoomConfig) {
        init(zoomConfig);
    }

    public static PreviewGalleryZoomConfig getGlobalZoomConfig() {
        return sGlobalZoomConfig;
    }

    /**
     * 设置全局「保存图片」弹窗样式；未设置时各字段使用 {@link ImageSaveDialogConfig} 内置默认。
     */
    public static void setGlobalImageSaveDialogConfig(@Nullable ImageSaveDialogConfig config) {
        sGlobalImageSaveDialogConfig = config;
    }

    @Nullable
    public static ImageSaveDialogConfig getGlobalImageSaveDialogConfig() {
        return sGlobalImageSaveDialogConfig;
    }
}
