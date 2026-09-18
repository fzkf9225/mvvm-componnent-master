package io.coderf.arklab.common.utils.common;

import android.graphics.Bitmap;
import android.net.Uri;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.glide.ImageCacheOptions;

/**
 * ShapeableImageView DataBinding 适配器。
 * <p>
 * 支持可选 placeholder / error / skipStableImageCache，未配置时保持原有默认图行为。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/18
 */
public class ImageViewAttrAdapter {

    private static final int DEFAULT_HEAD = R.mipmap.icon_head_default;

    private static int defaultImageRes() {
        return Config.getInstance().getDefaultPlaceholderRes();
    }

    private static int defaultErrorRes() {
        return Config.getInstance().getDefaultErrorImageRes();
    }

    // ---------- headerUrl（圆形头像，默认头像图） ----------

    @BindingAdapter(value = {"headerUrl", "placeholder", "error", "skipStableImageCache"},
            requireAll = false)
    public static void loadHeader(ShapeableImageView imageView, String url,
                                  @DrawableRes Integer placeholder, @DrawableRes Integer error,
                                  Boolean skipStableImageCache) {
        load(imageView, url, resolve(placeholder, DEFAULT_HEAD), resolve(error, DEFAULT_HEAD),
                Boolean.TRUE.equals(skipStableImageCache));
    }

    // ---------- imageUrl / imageBitmap / imageUri（普通 ShapeableImageView） ----------

    @BindingAdapter(value = {"imageUrl", "placeholder", "error", "skipStableImageCache"},
            requireAll = false)
    public static void loadImage(ShapeableImageView imageView, String url,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error,
                                 Boolean skipStableImageCache) {
        load(imageView, url, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()),
                Boolean.TRUE.equals(skipStableImageCache));
    }

    @BindingAdapter(value = {"imageBitmap", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ShapeableImageView imageView, Bitmap bitmap,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, bitmap, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()),
                false);
    }

    @BindingAdapter(value = {"imageUri", "placeholder", "error", "skipStableImageCache"},
            requireAll = false)
    public static void loadImage(ShapeableImageView imageView, Uri uri,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error,
                                 Boolean skipStableImageCache) {
        load(imageView, uri, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()),
                Boolean.TRUE.equals(skipStableImageCache));
    }

    // ---------- 内部统一加载 ----------

    private static int resolve(@DrawableRes Integer value, @DrawableRes int fallback) {
        return value != null ? value : fallback;
    }

    private static void load(ShapeableImageView imageView, Object model,
                             @DrawableRes int placeholder, @DrawableRes int error,
                             boolean skipStableImageCache) {
        if (imageView == null) {
            return;
        }
        // 空 model 时直接展示默认图，避免无意义请求
        if (model == null || (model instanceof String && ((String) model).isEmpty())) {
            imageView.setImageResource(placeholder);
            return;
        }
        RequestOptions options = new RequestOptions().placeholder(placeholder).error(error);
        if (skipStableImageCache) {
            options = options.set(ImageCacheOptions.SKIP_STABLE_KEY, true);
        }
        Glide.with(imageView.getContext())
                .load(model)
                .apply(options)
                .into(imageView);
    }
}

