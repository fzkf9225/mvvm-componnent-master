package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.databinding.AdapterMediaAddItemBinding;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.enums.UploadStatusEnum;
import pers.fz.mvvm.listener.OnUploadRetryClickListener;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.utils.common.CollectionUtil;
import pers.fz.mvvm.utils.common.DensityUtil;
import pers.fz.mvvm.utils.common.FileUtil;
import pers.fz.mvvm.utils.log.LogUtil;
import pers.fz.mvvm.widget.customview.CornerTextView;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2021/4/2
 * describe:添加视频
 */
public class MediaAddAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterMediaAddItemBinding> {
    public final String TAG = this.getClass().getSimpleName();

    public MediaClearListener mediaClearListener;
    public MediaAddListener mediaAddListener;
    //最大上传数量
    private int defaultMaxCount = -1;
    private int bgColor = Color.WHITE;
    private float radius = 8;

    protected Drawable placeholderImage;
    protected Drawable errorImage;

    public MediaAddAdapter() {
        super();
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 8);
        }
    }

    public MediaAddAdapter(int maxCount) {
        this();
        this.defaultMaxCount = maxCount;
    }

    @Override
    public int getLayoutId() {
        return R.layout.adapter_media_add_item;
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
    public void onBindHolder(BaseViewHolder<AdapterMediaAddItemBinding> holder, int pos) {
        if (pos == mList.size() && (mList.size() < defaultMaxCount || defaultMaxCount == -1)) {
            holder.getBinding().ivPlayer.setVisibility(View.GONE);
            holder.getBinding().mediaAdd.setVisibility(View.VISIBLE);
            holder.getBinding().ivMediaShow.setVisibility(View.GONE);
            holder.getBinding().ivClearMedia.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivClearMedia.setVisibility(View.VISIBLE);
            holder.getBinding().mediaAdd.setVisibility(View.GONE);
            holder.getBinding().ivMediaShow.setVisibility(View.VISIBLE);
            AttachmentTypeEnum attachmentTypeEnum = AttachmentUtil.getMediaType(holder.getBinding().ivMediaShow.getContext(), mList.get(pos).getFileType(), mList.get(pos).getPath());
            if (AttachmentTypeEnum.VIDEO == attachmentTypeEnum) {
                holder.getBinding().ivPlayer.setVisibility(View.VISIBLE);
            } else if (AttachmentTypeEnum.IMAGE == attachmentTypeEnum) {
                holder.getBinding().ivPlayer.setVisibility(View.GONE);
            } else {
                holder.getBinding().ivPlayer.setVisibility(View.GONE);
            }
            Glide.with(holder.getBinding().ivMediaShow.getContext())
                    .load(mList.get(pos).getPath())
                    .apply(new RequestOptions().placeholder(placeholderImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : placeholderImage)
                            .error(errorImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : errorImage))
                    .into(holder.getBinding().ivMediaShow);
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

    public MediaAddListener getMediaAddListener() {
        return mediaAddListener;
    }

    public MediaClearListener getMediaClearListener() {
        return mediaClearListener;
    }

    public void setMediaClearListener(MediaClearListener mediaClearListener) {
        this.mediaClearListener = mediaClearListener;
    }

    public interface MediaClearListener {
        void mediaClear(View view, int position);
    }

    public void setMediaAddListener(MediaAddListener mediaAddListener) {
        this.mediaAddListener = mediaAddListener;
    }

    public interface MediaAddListener {
        void mediaAdd(View view);
    }

    @Override
    protected BaseViewHolder<AdapterMediaAddItemBinding> createViewHold(AdapterMediaAddItemBinding binding) {
        return new ViewHolder(binding, this);
    }


    // 在ViewHolder类中添加recyclerView的引用
    private RecyclerView recyclerView;


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
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

    private static class ViewHolder extends BaseViewHolder<AdapterMediaAddItemBinding> {
        public <T> ViewHolder(@NotNull AdapterMediaAddItemBinding binding, MediaAddAdapter adapter) {
            super(binding, adapter);
            binding.ivMediaShow.setRadius((int) adapter.radius);
            binding.mediaAdd.setBgColorAndRadius(adapter.bgColor, adapter.radius);
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
            binding.ivMediaShow.setOnClickListener(v -> {
                try {
                    new PreviewPhotoDialog(v.getContext(), adapter.getList(), getAbsoluteAdapterPosition()).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
            });
            binding.ivPlayer.setOnClickListener(v -> {
                try {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE, FileUtil.getFileName(adapter.getList().get(getAbsoluteAdapterPosition()).getPath()));
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, adapter.getList().get(getAbsoluteAdapterPosition()).getPath());
                    VideoPlayerActivity.show(v.getContext(), bundleVideo);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(MediaAddAdapter.class.getSimpleName(), "视频播放失败:" + e);
                    Toast.makeText(v.getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                }
            });
            binding.ivClearMedia.setOnClickListener(v -> {
                if (adapter.getMediaClearListener() == null) {
                    return;
                }
                adapter.getMediaClearListener().mediaClear(v, getAbsoluteAdapterPosition());
            });
            binding.mediaAdd.setOnClickListener(v -> {
                if (adapter.getMediaAddListener() == null) {
                    return;
                }
                adapter.getMediaAddListener().mediaAdd(v);
            });
        }
    }
}
