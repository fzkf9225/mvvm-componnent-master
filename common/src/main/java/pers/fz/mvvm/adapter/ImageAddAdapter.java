package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.ImgAddItemBinding;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2017/10/20.
 * 添加圖片
 */
public class ImageAddAdapter extends BaseRecyclerViewAdapter<Uri, ImgAddItemBinding> {
    public ImageViewClearListener imageViewClearListener;
    public ImageViewAddListener imageViewAddListener;
    //最大上传数量
    private int defaultMaxCount = -1;
    private int bgColor = Color.WHITE;
    private float radius = 8;

    protected Drawable placeholderImage;
    protected Drawable errorImage;

    public ImageAddAdapter() {
        super();
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 8);
        }
    }

    public ImageAddAdapter(int maxCount) {
        super();
        this.defaultMaxCount = maxCount;
    }

    public void setPlaceholderImage(Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
    }

    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.img_add_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ImgAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setVisibility(mList.size() == defaultMaxCount ? View.GONE : View.VISIBLE);
        holder.getBinding().ivClearImg.setOnClickListener(v -> {
            if (imageViewClearListener != null) {
                imageViewClearListener.imgClear(v, pos);
            }
        });
        holder.getBinding().ivImageShow.setRadius((int) this.radius);
        holder.getBinding().ivAdd.setBgColorAndRadius(this.bgColor, this.radius);
        holder.getBinding().ivImageShow.setOnClickListener(v -> {
            try {
                new PreviewPhotoDialog(v.getContext(), PreviewPhotoDialog.createUriImageInfo(mList), pos).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
            }
        });
        if (pos == mList.size() && (mList.size() < defaultMaxCount || defaultMaxCount == -1)) {
            holder.getBinding().ivAdd.setOnClickListener(v -> {
                if (imageViewAddListener != null) {
                    imageViewAddListener.imgAdd(v);
                }
            });
            holder.getBinding().ivAdd.setVisibility(View.VISIBLE);
            holder.getBinding().ivImageShow.setVisibility(View.GONE);
            holder.getBinding().ivClearImg.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivAdd.setVisibility(View.GONE);
            holder.getBinding().ivImageShow.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setVisibility(View.VISIBLE);
            Glide.with(holder.getBinding().ivImageShow.getContext())
                    .load(mList.get(pos))
                    .apply(new RequestOptions().placeholder(placeholderImage ==null? ContextCompat.getDrawable(holder.itemView.getContext(),R.mipmap.ic_default_image) :placeholderImage)
                            .error(errorImage ==null? ContextCompat.getDrawable(holder.itemView.getContext(),R.mipmap.ic_default_image) :errorImage))
                    .into(holder.getBinding().ivImageShow);
        }
    }

    public int getMaxCount() {
        return defaultMaxCount;
    }

    @Override
    public int getItemCount() {
        return (super.getItemCount() == defaultMaxCount && defaultMaxCount != -1) ? super.getItemCount() : super.getItemCount() + 1;
    }

    public void setImageViewClearListener(ImageViewClearListener imageViewClearListener) {
        this.imageViewClearListener = imageViewClearListener;
    }

    public interface ImageViewClearListener {
        void imgClear(View view, int position);
    }

    public void setImageViewAddListener(ImageViewAddListener imageViewAddListener) {
        this.imageViewAddListener = imageViewAddListener;
    }

    public interface ImageViewAddListener {
        void imgAdd(View view);
    }
}
