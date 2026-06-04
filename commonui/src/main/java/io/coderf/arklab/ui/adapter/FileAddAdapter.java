package io.coderf.arklab.ui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.adapter.BaseMediaRecyclerViewAdapter;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.databinding.AdapterFileAddItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;


/**
 * Created by fz on 2024/3/11
 * describe：添加文件
 */
public class FileAddAdapter extends BaseMediaRecyclerViewAdapter<AttachmentBean, AdapterFileAddItemBinding> {
    public FileClearListener fileClearListener;
    private @ColorInt int textColor = 0x333333;

    protected float itemTextSize;

    public FileAddAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_file_add_item;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }


    public FileClearListener getFileClearListener() {
        return fileClearListener;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getItemTextSize() {
        return itemTextSize;
    }

    public void setItemTextSize(float itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    @Override
    protected void initArguments() {
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 4f);
            clearImageWidth = DensityUtil.dp2px(Config.getInstance().getApplication(), 20f);
            clearImageHeight = DensityUtil.dp2px(Config.getInstance().getApplication(), 20f);
            clearImageTopMargin = DensityUtil.dp2px(Config.getInstance().getApplication(), -12f);
            clearImageEndMargin = DensityUtil.dp2px(Config.getInstance().getApplication(), -12f);
            clearImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), io.coderf.arklab.common.R.drawable.ib_clear_image_selector);
            placeholderDefaultImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), io.coderf.arklab.common.R.mipmap.ic_default_image);
            errorDefaultImage = ContextCompat.getDrawable(Config.getInstance().getApplication(), io.coderf.arklab.common.R.mipmap.ic_default_image);
        }
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterFileAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setLayoutParams(getClearLayoutParams(holder.getBinding().ivClearImg));
        holder.getBinding().ivClearImg.setImageDrawable(getClearImage() == null ? getClearDefaultImage() : getClearImage());
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
            binding.layout.setBgColorAndRadius(Objects.requireNonNullElse(adapter.bgColor, Color.TRANSPARENT), adapter.radius);
            binding.ivClearImg.setOnClickListener(v -> {
                if (adapter.fileClearListener == null) {
                    return;
                }
                adapter.fileClearListener.fileClear(v, getAbsoluteAdapterPosition());
            });
            if (adapter.itemTextSize > 0) {
                binding.tvFile.setTextSize(TypedValue.COMPLEX_UNIT_PX, adapter.itemTextSize);
            } else {
                binding.tvFile.setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(binding.getRoot().getContext(), 12f));
            }
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
                    Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                    intent.putExtras(bundleVideo);
                    v.getContext().startActivity(intent);
                } else {
                    AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath());
                }
            });
        }
    }

    @Override
    public ViewGroup.LayoutParams getClearLayoutParams(AppCompatImageView clearImage) {
        ConstraintLayout.LayoutParams clearLayoutParams = (ConstraintLayout.LayoutParams) clearImage.getLayoutParams();
        if (clearLayoutParams == null) {
            clearLayoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        clearLayoutParams.width = clearImageWidth;
        clearLayoutParams.height = clearImageHeight;
        return clearLayoutParams;
    }

}
