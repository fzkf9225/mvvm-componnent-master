package io.coderf.arklab.ui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.adapter.BaseMediaRecyclerViewAdapter;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.databinding.AdapterFileShowItemBinding;

/**
 * Created by fz on 2024/2/26.
 * describe：文件展示
 */
public class FileShowAdapter extends BaseMediaRecyclerViewAdapter<AttachmentBean, AdapterFileShowItemBinding> {
    private @ColorInt int textColor = 0x333333;
    private Drawable fileDrawable;
    private boolean isShowFileDrawable = true;
    /**
     * 左侧图标宽高
     */
    protected int fileIconWidth;
    /**
     * 左侧图标宽高
     */
    protected int fileIconHeight;

    protected float itemTextSize;

    public FileShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_file_show_item;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public void setFileDrawable(Drawable fileDrawable) {
        this.fileDrawable = fileDrawable;
    }

    public void setFileIconWidth(int fileIconWidth) {
        this.fileIconWidth = fileIconWidth;
    }

    public void setFileIconHeight(int fileIconHeight) {
        this.fileIconHeight = fileIconHeight;
    }

    public void setShowFileDrawable(boolean showFileDrawable) {
        isShowFileDrawable = showFileDrawable;
    }

    public int getTextColor() {
        return textColor;
    }

    public Drawable getFileDrawable() {
        return fileDrawable;
    }

    public boolean isShowFileDrawable() {
        return isShowFileDrawable;
    }

    public int getFileIconWidth() {
        return fileIconWidth;
    }

    public int getFileIconHeight() {
        return fileIconHeight;
    }

    public float getItemTextSize() {
        return itemTextSize;
    }

    public void setItemTextSize(float itemTextSize) {
        this.itemTextSize = itemTextSize;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterFileShowItemBinding> holder, int pos) {
        if (TextUtils.isEmpty(mList.get(pos).getFileName())) {
            holder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getPath()));
        } else {
            holder.getBinding().tvFile.setText(FileUtil.getFileName(mList.get(pos).getFileName()));
        }
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
                    Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                    intent.putExtras(bundleVideo);
                    v.getContext().startActivity(intent);
                } else {
                    AttachmentUtil.viewFile(v.getContext(), adapter.getItem(getAbsoluteAdapterPosition()).getPath());
                }
            });
            if (adapter.itemTextSize > 0) {
                binding.tvFile.setTextSize(TypedValue.COMPLEX_UNIT_PX, adapter.itemTextSize);
            } else {
                binding.tvFile.setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(binding.getRoot().getContext(), 12f));
            }
            binding.tvFile.setTextColor(adapter.textColor);
            binding.layout.setBgColorAndRadius(Objects.requireNonNullElse(adapter.bgColor, Color.TRANSPARENT), adapter.radius);
            binding.imageFile.setLayoutParams(adapter.getFileIconLayoutParams(binding.imageFile));
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

    public ViewGroup.LayoutParams getFileIconLayoutParams(AppCompatImageView fileIconImage) {
        ConstraintLayout.LayoutParams fileIconLayoutParams = (ConstraintLayout.LayoutParams) fileIconImage.getLayoutParams();
        if (fileIconLayoutParams == null) {
            fileIconLayoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (fileIconWidth > 0) {
            fileIconLayoutParams.width = fileIconWidth;
        }

        if (fileIconHeight > 0) {
            fileIconLayoutParams.height = fileIconHeight;
        }
        return fileIconLayoutParams;
    }
}
