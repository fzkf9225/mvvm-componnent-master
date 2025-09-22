package com.casic.otitan.common.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.databinding.AdapterImageShowItemBinding;
import com.casic.otitan.common.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2025/10/20.
 * describe:展示图片适配器
 */
public class ImageShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterImageShowItemBinding> {
    private @ColorInt int bgColor = Color.WHITE;
    private float radius = 8;

    protected Drawable placeholderImage;
    protected Drawable errorImage;

    public ImageShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_image_show_item;
    }


    public void setBgColor( @ColorInt int bgColor) {
        this.bgColor = bgColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setPlaceholderImage(Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
    }

    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterImageShowItemBinding> holder, int pos) {
        Glide.with(holder.getBinding().cornerImage.getContext())
                .load(mList.get(pos).getPath())
                .apply(new RequestOptions().placeholder(placeholderImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : placeholderImage)
                        .error(errorImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : errorImage))
                .into(holder.getBinding().cornerImage);
    }

    @Override
    protected BaseViewHolder<AdapterImageShowItemBinding> createViewHold(AdapterImageShowItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    private static class ViewHolder extends BaseViewHolder<AdapterImageShowItemBinding> {

        public <T> ViewHolder(@NotNull AdapterImageShowItemBinding binding, ImageShowAdapter adapter) {
            super(binding, adapter);
            binding.cornerImage.setRadius((int) adapter.radius);
            binding.cornerImage.setBackgroundColor(adapter.bgColor);
            binding.cornerImage.setOnClickListener(v -> {
                try {
                    new PreviewPhotoDialog(v.getContext(), adapter.getList(), getAbsoluteAdapterPosition()).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
