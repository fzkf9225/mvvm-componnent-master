package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.databinding.AdapterImageAddItemBinding;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2017/10/20.
 * 添加圖片
 */
public class ImageAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterImageAddItemBinding> {
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
        this();
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
        return R.layout.adapter_image_add_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterImageAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setVisibility(mList.size() == defaultMaxCount ? View.GONE : View.VISIBLE);
        if (pos == mList.size() && (mList.size() < defaultMaxCount || defaultMaxCount == -1)) {
            holder.getBinding().ivAdd.setVisibility(View.VISIBLE);
            holder.getBinding().ivImageShow.setVisibility(View.GONE);
            holder.getBinding().ivClearImg.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivAdd.setVisibility(View.GONE);
            holder.getBinding().ivImageShow.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setVisibility(View.VISIBLE);
            Glide.with(holder.getBinding().ivImageShow.getContext())
                    .load(mList.get(pos).getPath())
                    .apply(new RequestOptions().placeholder(placeholderImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : placeholderImage)
                            .error(errorImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : errorImage))
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

    @Override
    protected BaseViewHolder<AdapterImageAddItemBinding> createViewHold(AdapterImageAddItemBinding binding) {
        return new ViewHolder(binding, this);
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

    public ImageViewClearListener getImageViewClearListener() {
        return imageViewClearListener;
    }

    public ImageViewAddListener getImageViewAddListener() {
        return imageViewAddListener;
    }

    private static class ViewHolder extends BaseViewHolder<AdapterImageAddItemBinding> {
        public ViewHolder(@NotNull AdapterImageAddItemBinding binding, ImageAddAdapter adapter) {
            super(binding, adapter);
            binding.ivImageShow.setRadius((int) adapter.radius);
            binding.ivAdd.setBgColorAndRadius(adapter.bgColor, adapter.radius);
            binding.ivImageShow.setOnClickListener(v -> {
                try {
                    new PreviewPhotoDialog(v.getContext(), adapter.getList(), getAbsoluteAdapterPosition()).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
            });
            binding.ivAdd.setOnClickListener(v -> {
                if (adapter.getImageViewAddListener() == null) {
                    return;
                }
                adapter.getImageViewAddListener().imgAdd(v);
            });
            binding.ivClearImg.setOnClickListener(v -> {
                if (adapter.getImageViewClearListener() == null) {
                    return;
                }
                adapter.getImageViewClearListener().imgClear(v, getAbsoluteAdapterPosition());
            });
        }

    }
}
