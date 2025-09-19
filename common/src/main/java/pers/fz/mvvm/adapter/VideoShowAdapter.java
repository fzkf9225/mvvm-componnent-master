package pers.fz.mvvm.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.databinding.AdapterVideoShowItemBinding;
import pers.fz.mvvm.utils.common.FileUtil;
import pers.fz.mvvm.utils.log.LogUtil;

/**
 * Created by fz on 2024/10/20.
 * describe:视频列表
 */
public class VideoShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, AdapterVideoShowItemBinding> {
    private @ColorInt int bgColor = Color.WHITE;
    private float radius = 8;

    protected Drawable placeholderImage;
    protected Drawable errorImage;

    public VideoShowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_video_show_item;
    }

    public void setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setPlaceholderImage(Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
    }

    public void setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterVideoShowItemBinding> holder, int pos) {
        Glide.with(holder.getBinding().imageVideo.getContext())
                .load(mList.get(pos).getPath())
                .apply(new RequestOptions().placeholder(placeholderImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : placeholderImage)
                        .error(errorImage == null ? ContextCompat.getDrawable(holder.itemView.getContext(), R.mipmap.ic_default_image) : errorImage))
                .into(holder.getBinding().imageVideo);
    }

    @Override
    protected BaseViewHolder<AdapterVideoShowItemBinding> createViewHold(AdapterVideoShowItemBinding binding) {
        return new ViewHolder(binding, this);
    }

    public static class ViewHolder extends BaseViewHolder<AdapterVideoShowItemBinding> {

        public <T> ViewHolder(@NotNull AdapterVideoShowItemBinding binding, VideoShowAdapter adapter) {
            super(binding, adapter);
            binding.imageVideo.setRadius((int) adapter.radius);
            binding.imageVideo.setBackgroundColor(adapter.bgColor);
            binding.videoPlay.setOnClickListener(v -> {
                try {
                    Bundle bundleVideo = new Bundle();
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_TITLE,
                            TextUtils.isEmpty(adapter.getList().get(getAbsoluteAdapterPosition()).getFileName()) ?
                                    FileUtil.getFileName(adapter.getList().get(getAbsoluteAdapterPosition()).getPath()) :
                                    adapter.getList().get(getAbsoluteAdapterPosition()).getFileName());
                    bundleVideo.putString(VideoPlayerActivity.VIDEO_PATH, adapter.getList().get(getAbsoluteAdapterPosition()).getPath());
                    VideoPlayerActivity.show(v.getContext(), bundleVideo);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.show(VideoShowAdapter.class.getSimpleName(), "视频播放失败:" + e);
                    Toast.makeText(v.getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
