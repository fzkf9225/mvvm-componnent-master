package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import pers.fz.mvvm.databinding.AdapterMediaShowItemBinding;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2021/4/2
 * describe:添加视频
 */
public class MediaShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterMediaShowItemBinding> {
    public final String TAG = this.getClass().getSimpleName();

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

    @Override
    public int getLayoutId() {
        return R.layout.adapter_media_show_item;
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
    public void onBindHolder(BaseViewHolder<AdapterMediaShowItemBinding> holder, int pos) {
        AttachmentTypeEnum attachmentTypeEnum = AttachmentUtil.getMediaType(holder.getBinding().imageVideo.getContext(), mList.get(pos).getFileType(), mList.get(pos).getPath());
        if (AttachmentTypeEnum.VIDEO == attachmentTypeEnum) {
            holder.getBinding().mediaPlay.setVisibility(View.VISIBLE);
        } else if (AttachmentTypeEnum.IMAGE == attachmentTypeEnum) {
            holder.getBinding().mediaPlay.setVisibility(View.GONE);
        } else {
            holder.getBinding().mediaPlay.setVisibility(View.GONE);
        }
        Glide.with(holder.getBinding().imageVideo.getContext())
                .load(mList.get(pos).getPath())
                .apply(new RequestOptions().placeholder(placeholderImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : placeholderImage)
                        .error(errorImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : errorImage))
                .into(holder.getBinding().imageVideo);
    }

    @Override
    protected BaseViewHolder<AdapterMediaShowItemBinding> createViewHold(AdapterMediaShowItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    private static class ViewHolder extends BaseViewHolder<AdapterMediaShowItemBinding> {
        public <T> ViewHolder(@NotNull AdapterMediaShowItemBinding binding, MediaShowAdapter adapter) {
            super(binding, adapter);
            binding.imageVideo.setBackgroundColor(adapter.bgColor);
            binding.imageVideo.setRadius((int) adapter.radius);
            binding.imageVideo.setOnClickListener(v -> {
                try {
                    new PreviewPhotoDialog(v.getContext(), adapter.getList(), getAbsoluteAdapterPosition()).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "图片打开失败", Toast.LENGTH_SHORT).show();
                }
            });
            binding.mediaPlay.setOnClickListener(v -> {
                try {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE,
                            TextUtils.isEmpty(adapter.getList().get(getAbsoluteAdapterPosition()).getFileName()) ?
                                    FileUtil.getFileName(adapter.getList().get(getAbsoluteAdapterPosition()).getPath())
                                    : adapter.getList().get(getAbsoluteAdapterPosition()).getFileName());
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, adapter.getList().get(getAbsoluteAdapterPosition()).getPath());
                    VideoPlayerActivity.show(v.getContext(), bundleVideo);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(MediaShowAdapter.class.getSimpleName(), "视频播放失败:" + e);
                    Toast.makeText(v.getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
