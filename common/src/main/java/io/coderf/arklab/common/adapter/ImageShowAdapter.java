package io.coderf.arklab.common.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.databinding.AdapterImageShowItemBinding;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2025/10/20.
 * describe:展示图片适配器
 */
public class ImageShowAdapter extends BaseMediaRecyclerViewAdapter<AttachmentBean, AdapterImageShowItemBinding> {

    public ImageShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_image_show_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterImageShowItemBinding> holder, int pos) {
        Glide.with(holder.getBinding().cornerImage.getContext())
                .load(mList.get(pos).getPath())
                .apply(new RequestOptions().placeholder(placeholderImage == null ? getPlaceholderDefaultImage() : placeholderImage)
                        .error(errorImage == null ? getErrorDefaultImage() : errorImage))
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
            binding.cornerImage.setBgColor(Objects.requireNonNullElse(adapter.bgColor, Color.TRANSPARENT));
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
