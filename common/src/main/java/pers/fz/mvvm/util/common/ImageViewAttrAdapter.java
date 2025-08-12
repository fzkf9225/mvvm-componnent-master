package pers.fz.mvvm.util.common;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.widget.customview.RoundImageView;

/**
 * Create by fz on 2020/3/27 0027
 * describe:
 */
public class ImageViewAttrAdapter {
    @BindingAdapter({"headerUrl"})
    public static void loadImage(RoundImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().error(R.mipmap.icon_head_default).placeholder(R.mipmap.icon_head_default))
                .into(imageView);
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().error(R.mipmap.ic_default_image).placeholder(R.mipmap.ic_default_image))
                .into(imageView);
    }

    @BindingAdapter({"imageBitmap"})
    public static void loadImage(ImageView imageView, Bitmap bitmap) {
        Glide.with(imageView.getContext())
                .load(bitmap)
                .apply(new RequestOptions().error(R.mipmap.ic_default_image).placeholder(R.mipmap.ic_default_image))
                .into(imageView);
    }
    @BindingAdapter({"imageUri"})
    public static void loadImage(ImageView imageView, Uri uri) {
        Glide.with(imageView.getContext())
                .load(uri)
                .apply(new RequestOptions().error(R.mipmap.ic_default_image).placeholder(R.mipmap.ic_default_image))
                .into(imageView);
    }
}
