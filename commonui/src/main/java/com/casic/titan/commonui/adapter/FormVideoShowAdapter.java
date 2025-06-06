package com.casic.titan.commonui.adapter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.casic.titan.commonui.bean.AttachmentBean;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.VideoShowItemBinding;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2017/10/20.
 * 视频列表
 */
public class FormVideoShowAdapter extends BaseRecyclerViewAdapter<AttachmentBean, VideoShowItemBinding> {

    private float radius = 8;
    protected Drawable placeholderImage;
    protected Drawable errorImage;
    public FormVideoShowAdapter() {
        super();
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
    protected int getLayoutId() {
        return R.layout.video_show_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<VideoShowItemBinding> viewHolder, int pos) {
        viewHolder.getBinding().imageVideo.setRadius((int) this.radius);
        Glide.with(viewHolder.getBinding().imageVideo.getContext())
                .load(mList.get(pos).getUrl())
                .apply(new RequestOptions()
                        .placeholder(placeholderImage ==null? ContextCompat.getDrawable(viewHolder.itemView.getContext(),R.mipmap.ic_default_image) :placeholderImage)
                        .error(errorImage ==null? ContextCompat.getDrawable(viewHolder.itemView.getContext(),R.mipmap.ic_default_image) :errorImage))
                .into(viewHolder.getBinding().imageVideo);
        viewHolder.getBinding().imagePlay.setOnClickListener(v -> {
            try {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString("videoName", mList.get(pos).getFileName());
                bundleVideo.putString("videoPath", mList.get(pos).getUrl());
                VideoPlayerActivity.show(v.getContext(), bundleVideo);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.show(TAG, "视频播放失败:" + e);
                Toast.makeText(v.getContext(), "视频播放失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
