package io.coderf.arklab.common.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 向 Glide Registry 安装稳定缓存 key 包装器。由 {@link io.coderf.arklab.common.api.Config#init} 调用。
 * <p>
 * 宿主若自建 {@code AppGlideModule} 并 {@code replace(GlideUrl, InputStream, ...)}，
 * 请把本方法放在所有 GlideUrl 替换之后，或直接依赖 {@code Config.init()}（它在 {@code Glide.get()} 之后 prepend）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/18
 */
public final class StableImageCache {

    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);

    private StableImageCache() {
    }

    /**
     * 在 Glide 初始化完成后 prepend 包装器。重复调用是空操作。
     */
    public static void install(@NonNull Context context) {
        register(Glide.get(context.getApplicationContext()).getRegistry());
    }

    /**
     * 供宿主 {@code AppGlideModule#registerComponents} 在其它 GlideUrl loader 注册完成之后调用。
     */
    public static void register(@NonNull Registry registry) {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }
        registry.prepend(GlideUrl.class, InputStream.class, new StableCacheGlideUrlLoader.Factory());
    }
}
