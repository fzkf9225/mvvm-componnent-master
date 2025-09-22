package com.casic.otitan.commonui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import com.casic.otitan.commonui.R;
import com.casic.otitan.commonui.databinding.AdapterFileAddItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.casic.otitan.common.activity.VideoPlayerActivity;
import com.casic.otitan.common.api.Config;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.enums.AttachmentTypeEnum;
import com.casic.otitan.common.utils.common.AttachmentUtil;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.FileUtil;
import com.casic.otitan.common.widget.gallery.PreviewPhotoDialog;


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
                    new PreviewPhotoDialog(v.getContext(),  List.of(item), getAbsoluteAdapterPosition()).show();
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
        }
    }

}
