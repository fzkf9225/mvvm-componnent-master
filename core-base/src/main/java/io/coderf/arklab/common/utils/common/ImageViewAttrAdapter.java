package io.coderf.arklab.common.utils.common;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.customview.RoundImageView;

/**
 * Create by fz on 2020/3/27 0027
 * describe: ImageView DataBinding 适配器。
 * <p>
 * 支持可选 placeholder / error，未配置时保持原有默认图行为。
 */
public class ImageViewAttrAdapter {

    private static final int DEFAULT_HEAD = R.mipmap.icon_head_default;
    private static final int DEFAULT_IMAGE = R.mipmap.ic_default_image;

    // ---------- headerUrl（RoundImageView，默认头像图） ----------

    @BindingAdapter(value = {"headerUrl", "placeholder", "error"}, requireAll = false)
    public static void loadHeader(RoundImageView imageView, String url,
                                  @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, url, resolve(placeholder, DEFAULT_HEAD), resolve(error, DEFAULT_HEAD));
    }

    // ---------- imageUrl / imageBitmap / imageUri（普通 ImageView） ----------

    @BindingAdapter(value = {"imageUrl", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ImageView imageView, String url,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, url, resolve(placeholder, DEFAULT_IMAGE), resolve(error, DEFAULT_IMAGE));
    }

    @BindingAdapter(value = {"imageBitmap", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ImageView imageView, Bitmap bitmap,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, bitmap, resolve(placeholder, DEFAULT_IMAGE), resolve(error, DEFAULT_IMAGE));
    }

    @BindingAdapter(value = {"imageUri", "placeholder", "error"}, requireAll = false)
    public static void loadImage(ImageView imageView, Uri uri,
                                 @DrawableRes Integer placeholder, @DrawableRes Integer error) {
        load(imageView, uri, resolve(placeholder, DEFAULT_IMAGE), resolve(error, DEFAULT_IMAGE));
    }

    // ---------- 内部统一加载 ----------

    private static int resolve(@DrawableRes Integer value, @DrawableRes int fallback) {
        return value != null ? value : fallback;
    }

    private static void load(ImageView imageView, Object model,
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
