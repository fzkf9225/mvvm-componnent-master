package com.casic.titan.commonui.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;

import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.bean.AttachmentBean;

import com.casic.titan.commonui.databinding.AdapterFileShowItemBinding;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.util.common.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.FileUtil;

/**
 * Created by fz on 2024/2/26.
 * describe：文件展示
 */
public class FileShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterFileShowItemBinding> {
    private @ColorInt int bgColor = Color.WHITE;
    private @ColorInt int textColor = 0x333333;
    private float radius = 4;
    private Drawable fileDrawable;
    private boolean isShowFileDrawable = true;

    public FileShowAdapter() {
        super();
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 4);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_file_show_item;
    }

    public void setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setFileDrawable(Drawable fileDrawable) {
        this.fileDrawable = fileDrawable;
    }

    public void setShowFileDrawable(boolean showFileDrawable) {
        isShowFileDrawable = showFileDrawable;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterFileShowItemBinding> viewHolder, int pos) {
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            viewHolder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getPath()));
        } else {
            viewHolder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getFileName()));
        }
    }

    @Override
    protected BaseViewHolder<AdapterFileShowItemBinding> createViewHold(AdapterFileShowItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    public static class ViewHolder extends BaseViewHolder<AdapterFileShowItemBinding> {
        public <T> ViewHolder(@NotNull AdapterFileShowItemBinding binding, FileShowAdapter adapter) {
            super(binding, adapter);
            binding.tvFile.setOnClickListener(v -> AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath()));
            binding.tvFile.setTextColor(adapter.textColor);
            binding.layout.setBgColorAndRadius(adapter.bgColor, adapter.radius);

            if (adapter.isShowFileDrawable) {
                binding.imageFile.setVisibility(View.VISIBLE);
                if (adapter.fileDrawable == null) {
                    binding.imageFile.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.icon_file));
                } else {
                    binding.imageFile.setImageDrawable(adapter.fileDrawable);
                }
            } else {
                binding.imageFile.setVisibility(View.GONE);
            }
        }

    }

}
