package pers.fz.mvvm.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.ImgAddItemBinding;
import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.ToastUtils;

/**
 * Created by fz on 2021/4/2
 * 添加视频
 */
public class VideoAddAdapter extends BaseRecyclerViewAdapter<Uri, ImgAddItemBinding> {
    private final String TAG = this.getClass().getSimpleName();

    public VideoClearListener videoClearListener;
    public VideoAddListener videoAddListener;
    private int defaultMaxCount = 1;//最大上传数量

    public VideoAddAdapter(Context context) {
        super(context);
    }

    public VideoAddAdapter(Context context, int maxCount) {
        super(context);
        this.defaultMaxCount = maxCount;
    }

    @Override
    public int getLayoutId() {
        return R.layout.img_add_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ImgAddItemBinding> holder, int pos) {
        holder.getBinding().ivClearImg.setVisibility((mList.size() == defaultMaxCount) ? View.GONE : View.VISIBLE);
        if (pos == mList.size() && mList.size() < defaultMaxCount) {
            holder.getBinding().ivAdd.setOnClickListener(v -> {
                if (videoAddListener != null) {
                    videoAddListener.videoAdd(v);
                }
            });
            holder.getBinding().ivPlayer.setVisibility(View.GONE);
            Glide.with(mContext).load(R.mipmap.ic_tweet_add).into(holder.getBinding().ivAdd);
            holder.getBinding().ivClearImg.setVisibility(View.GONE);
        } else {
            holder.getBinding().ivPlayer.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setVisibility(View.VISIBLE);
            holder.getBinding().ivClearImg.setOnClickListener(v -> {
                if (videoClearListener != null) {
                    videoClearListener.videoClear(v, pos);
                }
            });
            holder.getBinding().ivAdd.setOnClickListener(v -> {
//                try {
//                    Bundle bundleVideo = new Bundle();
//                    bundleVideo.putString("videoName", FileUtils.getFileName(mList.get(pos)));
//                    bundleVideo.putString("videoPath", mList.get(pos));
//                    VideoPlayerActivity.show(mContext, bundleVideo);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LogUtil.show(TAG,"视频播放失败:" + e);
//                    ToastUtils.showShort(mContext, "视频播放失败");
//                }
            });
            Glide.with(mContext)
                    .load(mList.get(pos))
                    .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                    .into(holder.getBinding().ivAdd);
        }
    }

    public int getMaxCount() {
        return defaultMaxCount;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() == defaultMaxCount ? super.getItemCount() : super.getItemCount() + 1;
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
