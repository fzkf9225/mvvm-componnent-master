package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.databinding.AdapterMediaAddItemBinding;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2021/4/2
 * describe:添加视频
 */
public class MediaShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterMediaAddItemBinding> {
    public final String TAG = this.getClass().getSimpleName();

    public MediaClearListener mediaClearListener;
    public MediaAddListener mediaAddListener;
    //最大上传数量
    private int defaultMaxCount = -1;
    private int bgColor = Color.WHITE;
    private float radius = 8;

    protected Drawable placeholderImage;
    protected Drawable errorImage;

    public MediaShowAdapter() {
        super();
        if (Config.getInstance().getApplication() != null) {
            radius = DensityUtil.dp2px(Config.getInstance().getApplication(), 8);
        }
    }

    public MediaShowAdapter(int maxCount) {
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
        holder.getBinding().ivClearMedia.setVisibility((mList.size() == defaultMaxCount) ? View.GONE : View.VISIBLE);
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

    public void setVideoAddListener(MediaAddListener mediaAddListener) {
        this.mediaAddListener = mediaAddListener;
    }

    public interface MediaAddListener {
        void mediaAdd(View view);
    }

    @Override
    protected BaseViewHolder<AdapterMediaAddItemBinding> createViewHold(AdapterMediaAddItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    private static class ViewHolder extends BaseViewHolder<AdapterMediaAddItemBinding> {
        public <T> ViewHolder(@NotNull AdapterMediaAddItemBinding binding, MediaShowAdapter adapter) {
            super(binding, adapter);
            binding.ivMediaShow.setRadius((int) adapter.radius);
            binding.mediaAdd.setBgColorAndRadius(adapter.bgColor, adapter.radius);
            binding.ivMediaShow.setOnClickListener(v -> {
                try {
                    List<String> stringList = AttachmentUtil.toStringList(adapter.getList());
                    new PreviewPhotoDialog(v.getContext(), PreviewPhotoDialog.createImageInfo(stringList), getAbsoluteAdapterPosition()).show();
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
                    LogUtil.e(MediaShowAdapter.class.getSimpleName(), "视频播放失败:" + e);
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
