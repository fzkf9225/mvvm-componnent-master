package com.casic.titan.commonui.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.casic.titan.commonui.R;

import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.bean.AttachmentBean;

import com.casic.titan.commonui.databinding.AdapterFileAddItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.util.common.AttachmentUtil;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;


/**
 * Created by fz on 2024/3/11
 * describe：添加文件
 */
public class FileAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterFileAddItemBinding> {
    public FileClearListener fileClearListener;
    private int bgColor = Color.WHITE;
    private @ColorInt int textColor = 0x333333;
    private float radius = 4;

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
            binding.layout.setBgColorAndRadius(adapter.bgColor, adapter.radius);
            binding.ivClearImg.setOnClickListener(v -> {
                if (adapter.fileClearListener == null) {
                    return;
                }
                adapter.fileClearListener.fileClear(v, getAbsoluteAdapterPosition());
            });
            binding.tvFile.setOnClickListener(v -> {
                AttachmentBean item = adapter.getItem(getAbsoluteAdapterPosition());
                if (TextUtils.isEmpty(item.getPath())) {
                    Toast.makeText(v.getContext(), "文件不存在或已被删除！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AttachmentTypeEnum.IMAGE.typeValue.equals(item.getFileType())) {
                    new PreviewPhotoDialog(v.getContext(), Collections.singletonList(item.getPath()), getAbsoluteAdapterPosition()).show();
                } else if (AttachmentTypeEnum.VIDEO.typeValue.equals(item.getFileType())) {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, FileUtil.getFileName(item.getPath()));
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, item.getPath());
                    VideoPlayerActivity.show(v.getContext(), bundleVideo);
                } else {
                    AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath());
                }
            });
        }
    }

}
