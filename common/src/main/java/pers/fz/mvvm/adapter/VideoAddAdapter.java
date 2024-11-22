package pers.fz.mvvm.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.VideoAddItemBinding;
import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2021/4/2
 * describe:添加视频
 */
public class VideoAddAdapter extends BaseRecyclerViewAdapter<Uri, VideoAddItemBinding> {
    private final String TAG = this.getClass().getSimpleName();

    public VideoClearListener videoClearListener;
    public VideoAddListener videoAddListener;
    //最大上传数量
    private int defaultMaxCount = -1;
    private int bgColor = Color.WHITE;
    private float radius = 5;
    public VideoAddAdapter(Context context) {
        super(context);
    }

    public VideoAddAdapter(Context context, int maxCount) {
        super(context);
        this.defaultMaxCount = maxCount;
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_add_item;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public void onBindHolder(BaseViewHolder<VideoAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setVisibility((mList.size() == defaultMaxCount) ? View.GONE : View.VISIBLE);
        holder.getBinding().ivClearImg.setOnClickListener(v -> {
            if (videoClearListener != null) {
                videoClearListener.videoClear(v, pos);
            }
        });
        holder.getBinding().videoAdd.setBgColorAndRadius(this.bgColor,this.radius);
        holder.getBinding().ivVideoShow.setOnClickListener(v -> {
            try {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString("videoName", FileUtils.getFileName(mList.get(pos).toString()));
                bundleVideo.putString("videoPath", mList.get(pos).toString());
                VideoPlayerActivity.show(mContext, bundleVideo);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.show(TAG,"视频播放失败:" + e);
                Toast.makeText(mContext, "视频播放失败", Toast.LENGTH_SHORT).show();
            }
        });
        if (pos == mList.size() && (mList.size() < defaultMaxCount || defaultMaxCount == -1)) {
            holder.getBinding().videoAdd.setOnClickListener(v -> {
                if (videoAddListener != null) {
                    videoAddListener.videoAdd(v);
                }
            });
            holder.getBinding().ivPlayer.setVisibility(View.GONE);
            holder.getBinding().videoAdd.setVisibility(View.VISIBLE);
            holder.getBinding().ivVideoShow.setVisibility(View.GONE);
            holder.getBinding().ivClearImg.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivPlayer.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setVisibility(View.VISIBLE);
            holder.getBinding().videoAdd.setVisibility(View.GONE);
            holder.getBinding().ivVideoShow.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(mList.get(pos))
                    .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                    .into(holder.getBinding().ivVideoShow);
        }
    }

    public int getMaxCount() {
        return defaultMaxCount;
    }

    @Override
    public int getItemCount() {
        return (super.getItemCount() == defaultMaxCount && defaultMaxCount != -1) ? super.getItemCount() : super.getItemCount() + 1;
    }

    public void setVideoClearListener(VideoClearListener videoClearListener) {
        this.videoClearListener = videoClearListener;
    }

    public interface VideoClearListener {
        void videoClear(View view, int position);
    }

    public void setVideoAddListener(VideoAddListener videoAddListener) {
        this.videoAddListener = videoAddListener;
    }

    public interface VideoAddListener {
        void videoAdd(View view);
    }
}
