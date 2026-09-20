package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * 请求始终用当前完整签名 URL，磁盘缓存身份用规范化后的 key。
 * <p>
 * 不能把两者塞进同一个 {@link GlideUrl}：HttpGlideUrlLoader 的 ModelCache 按
 * {@link GlideUrl#equals} 复用对象，会把过期签名拿去发请求。
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
                && Config.getInstance().isStableImageCacheStrategyActive();
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl model, int width, int height,
                                               @NonNull Options options) {
        GlideUrl fetchUrl = unwrap(model);
        if (ImageCacheOptions.isSkipStableKey(options)) {
            return delegate.buildLoadData(fetchUrl, width, height, options);
        }
        String original = fetchUrl.getCacheKey();
        String cacheKey = UrlCacheKeyNormalizer.normalize(
                original, Config.getInstance().getStableImageCacheIgnoredQueryParams());
        LoadData<InputStream> inner = delegate.buildLoadData(fetchUrl, width, height, options);
        if (inner == null || cacheKey == null || cacheKey.equals(original)) {
            return inner;
        }
        if (Config.enableDebug.get()) {
            LogUtil.logger("StableImageCache", "rewrite cacheKey, fetch still signed\nfetch="
                    + original + "\ncacheKey=" + cacheKey);
        }
        return new LoadData<>(new ObjectKey(cacheKey), inner.alternateKeys, inner.fetcher);
    }

    @NonNull
    private static GlideUrl unwrap(@NonNull GlideUrl model) {
        return model instanceof StableGlideUrl ? ((StableGlideUrl) model).unwrap() : model;
    }

    static final class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private final HttpGlideUrlLoader.Factory httpFactory = new HttpGlideUrlLoader.Factory();

        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            // 必须自己包 HttpGlideUrlLoader，不能 multiFactory.build(GlideUrl)：
            // replace 之后再 build 会递归；prepend 则会进 MultiModelLoader，sourceKey 被改回完整 GlideUrl。
            return new StableCacheGlideUrlLoader(httpFactory.build(multiFactory));
        }

        @Override
        public void teardown() {
            // no-op
        }
    }
}
