package io.coderf.arklab.common.widget.gallery;

/**
 * 大图预览缩放配置。
 * <ul>
 *   <li>{@link #defaultMaxScale}：小图/常规图在适配屏幕后的最大附加缩放倍数（默认 10）</li>
 *   <li>{@link #extraMaxZoomRatio}：高分辨率图达到 1:1 像素显示后，可继续放大的倍率（默认 3）</li>
 *   <li>{@link #overscrollBounceEnabled}：到达最大倍数后继续捏合时允许短暂越过上限，松手回弹（默认开启，仿微信）</li>
 *   <li>{@link #overscrollMaxRatio}：越过上限时的临时最大倍数相对 {@code maxScale} 的倍率（默认 1.4）</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/11 16:30
 */
public class PreviewGalleryZoomConfig {

    /** 小图默认最大缩放倍数，如 800×600 在屏幕上适配后最多再放大 10 倍 */
    public static final float DEFAULT_MAX_SCALE = 10.0f;
    /** 大图在 1:1 显示后的额外放大倍率 */
    public static final float DEFAULT_EXTRA_MAX_ZOOM_RATIO = 3.0f;
    /** 到达最大倍数后继续捏合时允许越过上限的临时倍率 */
    public static final float DEFAULT_OVERSCROLL_MAX_RATIO = 1.4f;
    /** 默认开启越过上限后的松手回弹 */
    public static final boolean DEFAULT_OVERSCROLL_BOUNCE_ENABLED = true;

    private final float defaultMaxScale;
    private final float extraMaxZoomRatio;
    private final boolean overscrollBounceEnabled;
    private final float overscrollMaxRatio;

    public PreviewGalleryZoomConfig(float defaultMaxScale, float extraMaxZoomRatio) {
        this(defaultMaxScale, extraMaxZoomRatio,
                DEFAULT_OVERSCROLL_BOUNCE_ENABLED, DEFAULT_OVERSCROLL_MAX_RATIO);
    }

    public PreviewGalleryZoomConfig(float defaultMaxScale, float extraMaxZoomRatio,
                                    boolean overscrollBounceEnabled, float overscrollMaxRatio) {
        if (defaultMaxScale <= 0f) {
            throw new IllegalArgumentException("defaultMaxScale must be > 0");
        }
        if (extraMaxZoomRatio <= 0f) {
            throw new IllegalArgumentException("extraMaxZoomRatio must be > 0");
        }
        if (overscrollMaxRatio < 1f) {
            throw new IllegalArgumentException("overscrollMaxRatio must be >= 1");
        }
        this.defaultMaxScale = defaultMaxScale;
        this.extraMaxZoomRatio = extraMaxZoomRatio;
        this.overscrollBounceEnabled = overscrollBounceEnabled;
        this.overscrollMaxRatio = overscrollMaxRatio;
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

    public boolean isOverscrollBounceEnabled() {
        return overscrollBounceEnabled;
    }

    public float getOverscrollMaxRatio() {
        return overscrollMaxRatio;
    }

    public static final class Builder {
        private float defaultMaxScale = DEFAULT_MAX_SCALE;
        private float extraMaxZoomRatio = DEFAULT_EXTRA_MAX_ZOOM_RATIO;
        private boolean overscrollBounceEnabled = DEFAULT_OVERSCROLL_BOUNCE_ENABLED;
        private float overscrollMaxRatio = DEFAULT_OVERSCROLL_MAX_RATIO;

        public Builder setDefaultMaxScale(float defaultMaxScale) {
            this.defaultMaxScale = defaultMaxScale;
            return this;
        }

        public Builder setExtraMaxZoomRatio(float extraMaxZoomRatio) {
            this.extraMaxZoomRatio = extraMaxZoomRatio;
            return this;
        }

        public Builder setOverscrollBounceEnabled(boolean overscrollBounceEnabled) {
            this.overscrollBounceEnabled = overscrollBounceEnabled;
            return this;
        }

        public Builder setOverscrollMaxRatio(float overscrollMaxRatio) {
            this.overscrollMaxRatio = overscrollMaxRatio;
            return this;
        }

        public PreviewGalleryZoomConfig build() {
            return new PreviewGalleryZoomConfig(defaultMaxScale, extraMaxZoomRatio,
                    overscrollBounceEnabled, overscrollMaxRatio);
        }
    }
}
