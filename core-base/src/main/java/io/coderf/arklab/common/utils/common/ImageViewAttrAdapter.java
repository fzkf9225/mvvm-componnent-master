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

/**
 * ShapeableImageView DataBinding 适配器。
 * <p>
 * 支持可选 placeholder / error，未配置时保持原有默认图行为。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
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

    @BindingAdapter(value = {"headerUrl", "placeholder", "error"}, requireAll = false)
    public static void loadHeader(ShapeableImageView imageView, String url,
                                  @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, url, resolve(placeholder, DEFAULT_HEAD), resolve(error, DEFAULT_HEAD));
    }

    // ---------- imageUrl / imageBitmap / imageUri（普通 ShapeableImageView） ----------

    @BindingAdapter(value = {"imageUrl", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ShapeableImageView imageView, String url,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, url, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()));
    }

    @BindingAdapter(value = {"imageBitmap", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ShapeableImageView imageView, Bitmap bitmap,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, bitmap, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()));
    }

    @BindingAdapter(value = {"imageUri", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ShapeableImageView imageView, Uri uri,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, uri, resolve(placeholder, defaultImageRes()), resolve(error, defaultErrorRes()));
    }

    // ---------- 内部统一加载 ----------

    private static int resolve(@DrawableRes Integer value, @DrawableRes int fallback) {
        return value != null ? value : fallback;
    }

    private static void load(ShapeableImageView imageView, Object model,
                             @DrawableRes int placeholder, @DrawableRes int error) {
        if (imageView == null) {
            return;
        }
        // 空 model 时直接展示默认图，避免无意义请求
        if (model == null || (model instanceof String && ((String) model).isEmpty())) {
            imageView.setImageResource(placeholder);
            return;
        }
        Glide.with(imageView.getContext())
                .load(model)
                .apply(new RequestOptions().placeholder(placeholder).error(error))
                .into(imageView);
    }
}

