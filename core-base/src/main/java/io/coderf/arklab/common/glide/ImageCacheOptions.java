package io.coderf.arklab.common.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.request.RequestOptions;

/**
 * 单次 Glide 请求的稳定缓存 key 选项。
 * <p>
 * 全局策略开启后，个别请求不想走稳定 key 时：
 * <pre>
 * Glide.with(context)
 *     .load(url)
 *     .apply(ImageCacheOptions.skipStableKey())
 *     .into(imageView);
 * </pre>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
public final class ImageCacheOptions {

    /**
     * 为 true 时本次请求仍用完整 URL 作为缓存 key，不受 Config 忽略 query 配置影响。
     */
    public static final Option<Boolean> SKIP_STABLE_KEY =
            Option.memory("io.coderf.arklab.glide.skip_stable_cache_key", false);

    private ImageCacheOptions() {
    }

    @NonNull
    public static RequestOptions skipStableKey() {
        return new RequestOptions().set(SKIP_STABLE_KEY, true);
    }

    static boolean isSkipStableKey(@NonNull Options options) {
        return Boolean.TRUE.equals(options.get(SKIP_STABLE_KEY));
    }
}
