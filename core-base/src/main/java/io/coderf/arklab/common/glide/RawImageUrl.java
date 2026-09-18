package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.GlideUrl;

/**
 * 强制使用完整 URL 作为 Glide 缓存 key，忽略全局稳定缓存策略。
 * <pre>
 * Glide.with(context).load(RawImageUrl.of(url)).into(imageView);
 * </pre>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
public final class RawImageUrl extends GlideUrl {

    private RawImageUrl(@NonNull String url) {
        super(url);
    }

    @NonNull
    public static RawImageUrl of(@NonNull String url) {
        return new RawImageUrl(url);
    }
}
