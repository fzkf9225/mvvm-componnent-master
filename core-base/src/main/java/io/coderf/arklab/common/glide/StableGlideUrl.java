package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.GlideUrl;

import java.util.Map;

/**
 * 请求仍走原始带签名 URL，缓存身份使用规范化后的 key。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
final class StableGlideUrl extends GlideUrl {

    private final GlideUrl source;
    private final String cacheKey;

    StableGlideUrl(@NonNull GlideUrl source, @NonNull String cacheKey) {
        super(source.getCacheKey());
        this.source = source;
        this.cacheKey = cacheKey;
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public Map<String, String> getHeaders() {
        return source.getHeaders();
    }
}
