package com.casic.titan.commonui.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.ColorInt;

import com.casic.titan.commonui.R;

import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.bean.AttachmentBean;

import com.casic.titan.commonui.databinding.AdapterFileAddItemBinding;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.util.common.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2024/3/11
 * describe：添加文件
 */
public class FileAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterFileAddItemBinding> {
    public FileClearListener fileClearListener;
    private int bgColor = Color.WHITE;
    private @ColorInt int textColor = 0x333333;
    private float radius =4;

    public FileAddAdapter() {
        super();
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 4);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_file_add_item;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterFileAddItemBinding> holder, int pos) {
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            holder.getBinding().tvFile.setText(mList.get(pos).getPath());
        } else {
            holder.getBinding().tvFile.setText(mList.get(pos).getFileName());
        }
    }

    @Override
    protected BaseViewHolder<AdapterFileAddItemBinding> createViewHold(AdapterFileAddItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    public void setFileClearListener(FileClearListener fileClearListener) {
        this.fileClearListener = fileClearListener;
    }

    public interface FileClearListener {
        void fileClear(View view, int position);
    }

    public static class ViewHolder extends BaseViewHolder<AdapterFileAddItemBinding> {
        public <T> ViewHolder(@NotNull AdapterFileAddItemBinding binding, FileAddAdapter adapter) {
            super(binding, adapter);
            binding.tvFile.setTextColor(adapter.textColor);
            binding.layout.setBgColorAndRadius(adapter.bgColor,adapter.radius);
            binding.ivClearImg.setOnClickListener(v -> {
                if (adapter.fileClearListener == null) {
                    return;
                }
                adapter.fileClearListener.fileClear(v, getAbsoluteAdapterPosition());
            });
            binding.tvFile.setOnClickListener(v -> AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath()));
        }
    }

}
