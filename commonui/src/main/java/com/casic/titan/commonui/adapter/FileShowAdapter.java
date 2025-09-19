package com.casic.titan.commonui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.AdapterFileShowItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.utils.common.DensityUtil;
import pers.fz.mvvm.utils.common.FileUtil;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;

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
            binding.tvFile.setOnClickListener(v -> {
                AttachmentBean item = adapter.getItem(getAbsoluteAdapterPosition());
                if (TextUtils.isEmpty(item.getPath())) {
                    Toast.makeText(v.getContext(), "文件不存在或已被删除！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AttachmentTypeEnum.IMAGE.typeValue.equals(item.getFileType())) {
                    new PreviewPhotoDialog(v.getContext(), List.of(item), getAbsoluteAdapterPosition()).show();
                } else if (AttachmentTypeEnum.VIDEO.typeValue.equals(item.getFileType())) {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, FileUtil.getFileName(item.getPath()));
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, item.getPath());
                    Intent intent = new Intent(v.getContext(),VideoPlayerActivity.class);
                    intent.putExtras(bundleVideo);
                    v.getContext().startActivity(intent);
                } else {
                    AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath());
                }
            });
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
