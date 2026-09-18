package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

import io.coderf.arklab.common.api.Config;

/**
 * 将 http(s) {@link GlideUrl} 包装为稳定缓存 key，实际请求 URL 不变。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
final class StableCacheGlideUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private final ModelLoader<GlideUrl, InputStream> delegate;

    StableCacheGlideUrlLoader(@NonNull ModelLoader<GlideUrl, InputStream> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean handles(@NonNull GlideUrl model) {
        return !(model instanceof RawImageUrl)
                && !(model instanceof StableGlideUrl)
                && Config.getInstance().isStableImageCacheStrategyActive();
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl model, int width, int height,
                                               @NonNull Options options) {
        GlideUrl toLoad = model;
        if (!ImageCacheOptions.isSkipStableKey(options)) {
            String original = model.getCacheKey();
            String cacheKey = UrlCacheKeyNormalizer.normalize(
                    original, Config.getInstance().getStableImageCacheIgnoredQueryParams());
            if (cacheKey != null && !cacheKey.equals(original)) {
                toLoad = new StableGlideUrl(model, cacheKey);
            }
        }
        return delegate.buildLoadData(toLoad, width, height, options);
    }

    static final class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new StableCacheGlideUrlLoader(multiFactory.build(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // no-op
        }
    }
}
