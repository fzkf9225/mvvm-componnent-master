package com.casic.otitan.common.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.IntStream;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.databinding.AdapterImageAddItemBinding;
import com.casic.otitan.common.enums.UploadStatusEnum;
import com.casic.otitan.common.listener.OnUploadRetryClickListener;
import com.casic.otitan.common.utils.common.AttachmentUtil;
import com.casic.otitan.common.utils.common.CollectionUtil;
import com.casic.otitan.common.widget.customview.CornerTextView;
import com.casic.otitan.common.widget.gallery.PreviewPhotoDialog;

/**
 * 添加图片适配器
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2026/3/5 9:40
 */
public class ImageAddAdapter extends BaseMediaRecyclerViewAdapter<AttachmentBean, AdapterImageAddItemBinding> {
    public ImageViewClearListener imageViewClearListener;
    public ImageViewAddListener imageViewAddListener;

    public ImageAddAdapter() {
        super();
    }

    public ImageAddAdapter(int maxCount) {
        super(maxCount);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_image_add_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterImageAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setLayoutParams(getClearLayoutParams(holder.getBinding().ivClearImg));
        holder.getBinding().ivClearImg.setImageDrawable(getClearImage() == null ? getClearDefaultImage() : getClearImage());
        if (pos == mList.size() && (mList.size() < defaultMaxCount || defaultMaxCount == -1)) {
            holder.getBinding().uploadProcess.setVisibility(View.GONE);
            holder.getBinding().uploadMark.setVisibility(View.GONE);
            holder.getBinding().ivAdd.setVisibility(View.VISIBLE);
            holder.getBinding().ivImageShow.setVisibility(View.GONE);
            holder.getBinding().ivClearImg.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivAdd.setVisibility(View.GONE);
            holder.getBinding().ivImageShow.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setVisibility(View.VISIBLE);
            Glide.with(holder.getBinding().ivImageShow.getContext())
                    .load(mList.get(pos).getPath())
                    .apply(new RequestOptions().placeholder(placeholderImage == null ? getPlaceholderDefaultImage() : placeholderImage)
                            .error(errorImage == null ? getErrorDefaultImage() : errorImage))
                    .into(holder.getBinding().ivImageShow);
            holder.getBinding().uploadProcess.setText(mList.get(pos).getUploadingPercent());
            updateUploadView(UploadStatusEnum.getInfo(mList.get(pos).getUploading()), holder.getBinding().uploadProcess, holder.getBinding().uploadMark);
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

    // 在ViewHolder类中添加recyclerView的引用
    private RecyclerView recyclerView;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    /**
     * 上传失败重试监听
     */
    private OnUploadRetryClickListener onUploadRetryClickListener;

    /**
     * 设置上传失败重试监听
     *
     * @param onUploadRetryClickListener 上传失败重试监听
     */
    public void setOnUploadRetryClickListener(OnUploadRetryClickListener onUploadRetryClickListener) {
        this.onUploadRetryClickListener = onUploadRetryClickListener;
    }

    /**
     * 上传是否失败
     *
     * @return true为失败
     */
    public boolean isUploadingFailure() {
        if (CollectionUtil.isEmpty(mList)) {
            return true;
        }
        return mList.stream()
                .anyMatch(item ->
                        (!AttachmentUtil.isHttp(item.getPath()) && item.getUploadInfo() == null) ||
                                UploadStatusEnum.FAILURE.typeValue == item.getUploading() ||
                                UploadStatusEnum.CANCELED.typeValue == item.getUploading());
    }

    /**
     * 上传是否成功
     *
     * @return true为成功
     */
    public boolean isUploadingSuccess() {
        return !isUploadingFailure();
    }

    /**
     * 更新指定位置的上传状态
     *
     * @param mobileId 主键id
     * @param status   上传状态
     * @param percent  进度百分比
     */
    public void updateUploadStatus(String mobileId, UploadStatusEnum status, String percent) {
        if (TextUtils.isEmpty(mobileId)) {
            return;
        }
        if (CollectionUtil.isEmpty(mList)) {
            return;
        }
        int pos = IntStream.range(0, mList.size()).filter(i -> mobileId.equals(mList.get(i).getMobileId())).findFirst().orElse(-1);
        if (pos < 0) {
            return;
        }
        updateUploadStatus(pos, status, percent);
    }

    /**
     * 更新指定位置的上传状态
     *
     * @param position 位置
     * @param status   上传状态
     * @param percent  进度百分比
     */
    public void updateUploadStatus(int position, UploadStatusEnum status, String percent) {
        if (position < 0 || position >= mList.size()) {
            return;
        }
        mList.get(position).setUploading(status.typeValue);
        mList.get(position).setUploadingPercent(percent);
        if (recyclerView == null) {
            return;
        }
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder instanceof ViewHolder holder) {
            holder.getBinding().uploadProcess.setText(percent);
            updateUploadView(status, holder.getBinding().uploadProcess, holder.getBinding().uploadMark);
        }
    }

    public void updateUploadView(UploadStatusEnum status, AppCompatTextView uploadProcess, CornerTextView markView) {
        if (status == UploadStatusEnum.UPLOADING) {
            uploadProcess.setVisibility(View.VISIBLE);
            markView.setVisibility(View.VISIBLE);
            uploadProcess.setTextColor(Color.WHITE);
        } else if (status == UploadStatusEnum.SUCCESS) {
            uploadProcess.setVisibility(View.VISIBLE);
            markView.setVisibility(View.GONE);
            uploadProcess.setTextColor(ContextCompat.getColor(uploadProcess.getContext(), R.color.theme_green));
        } else if (status == UploadStatusEnum.FAILURE) {
            uploadProcess.setVisibility(View.VISIBLE);
            markView.setVisibility(View.GONE);
            uploadProcess.setTextColor(ContextCompat.getColor(uploadProcess.getContext(), R.color.theme_red));
        } else if (status == UploadStatusEnum.CANCELED) {
            uploadProcess.setVisibility(View.VISIBLE);
            markView.setVisibility(View.GONE);
            uploadProcess.setTextColor(ContextCompat.getColor(uploadProcess.getContext(), R.color.theme_red));
        } else {
            uploadProcess.setVisibility(View.GONE);
            markView.setVisibility(View.GONE);
        }
    }

    private static class ViewHolder extends BaseViewHolder<AdapterImageAddItemBinding> {
        public ViewHolder(@NotNull AdapterImageAddItemBinding binding, ImageAddAdapter adapter) {
            super(binding, adapter);
            binding.ivImageShow.setRadius((int) adapter.radius);
            binding.ivAdd.setBgColorAndRadius(Objects.requireNonNullElse(adapter.bgColor, Color.TRANSPARENT), adapter.radius);
            binding.uploadMark.setBgColorAndRadius(0x80000000, adapter.radius);
            binding.uploadProcess.setOnClickListener(v -> {
                if (UploadStatusEnum.CANCELED.typeValue != adapter.getList().get(getAbsoluteAdapterPosition()).getUploading() &&
                        UploadStatusEnum.FAILURE.typeValue != adapter.getList().get(getAbsoluteAdapterPosition()).getUploading()) {
                    return;
                }
                if (adapter.onUploadRetryClickListener == null) {
                    return;
                }
                adapter.onUploadRetryClickListener.onRetryClick(v, getAbsoluteAdapterPosition());
            });
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
