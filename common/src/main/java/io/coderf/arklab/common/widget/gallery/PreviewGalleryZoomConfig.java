package io.coderf.arklab.common.widget.gallery;

/**
 * 大图预览缩放配置。
 * <ul>
 *   <li>{@link #defaultMaxScale}：小图/常规图在适配屏幕后的最大附加缩放倍数（默认 10）</li>
 *   <li>{@link #extraMaxZoomRatio}：高分辨率图达到 1:1 像素显示后，可继续放大的倍率（默认 3，即约 7.4×3≈22.2 倍）</li>
 * </ul>
 */
public class PreviewGalleryZoomConfig {

    /** 小图默认最大缩放倍数，如 800×600 在屏幕上适配后最多再放大 10 倍 */
    public static final float DEFAULT_MAX_SCALE = 10.0f;
    /** 大图在 1:1 显示后的额外放大倍率 */
    public static final float DEFAULT_EXTRA_MAX_ZOOM_RATIO = 3.0f;

    private final float defaultMaxScale;
    private final float extraMaxZoomRatio;

    public PreviewGalleryZoomConfig(float defaultMaxScale, float extraMaxZoomRatio) {
        if (defaultMaxScale <= 0f) {
            throw new IllegalArgumentException("defaultMaxScale must be > 0");
        }
        if (extraMaxZoomRatio <= 0f) {
            throw new IllegalArgumentException("extraMaxZoomRatio must be > 0");
        }
        this.defaultMaxScale = defaultMaxScale;
        this.extraMaxZoomRatio = extraMaxZoomRatio;
    }

    public static PreviewGalleryZoomConfig defaults() {
        return new PreviewGalleryZoomConfig(DEFAULT_MAX_SCALE, DEFAULT_EXTRA_MAX_ZOOM_RATIO);
    }

    public float getDefaultMaxScale() {
        return defaultMaxScale;
    }

    public float getExtraMaxZoomRatio() {
        return extraMaxZoomRatio;
    }

    public static final class Builder {
        private float defaultMaxScale = DEFAULT_MAX_SCALE;
        private float extraMaxZoomRatio = DEFAULT_EXTRA_MAX_ZOOM_RATIO;

        public Builder setDefaultMaxScale(float defaultMaxScale) {
            this.defaultMaxScale = defaultMaxScale;
            return this;
        }

        public Builder setExtraMaxZoomRatio(float extraMaxZoomRatio) {
            this.extraMaxZoomRatio = extraMaxZoomRatio;
            return this;
        }

        public PreviewGalleryZoomConfig build() {
            return new PreviewGalleryZoomConfig(defaultMaxScale, extraMaxZoomRatio);
        }
    }
}
