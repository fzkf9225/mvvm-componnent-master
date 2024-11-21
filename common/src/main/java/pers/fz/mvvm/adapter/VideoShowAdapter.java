package pers.fz.mvvm.adapter;

import android.content.Context;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.VideoPlayerActivity;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.VideoShowItemBinding;
import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.ToastUtils;

/**
 * Created by fz on 2017/10/20.
 * 视频列表
 */
public class VideoShowAdapter extends BaseRecyclerViewAdapter<String, VideoShowItemBinding> {

    public VideoShowAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.video_show_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<VideoShowItemBinding> viewHolder, int pos) {
        viewHolder.itemView.setTag(pos);
        viewHolder.getBinding().imagePlay.setTag(R.id.imageUrl, pos);
        Glide.with(mContext)
                .load(mList.get(pos))
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(viewHolder.getBinding().imageVideo);
        viewHolder.getBinding().imagePlay.setOnClickListener( v -> {
            try {
                Bundle bundleVideo = new Bundle();
                bundleVideo.putString("videoName", FileUtils.getFileName(mList.get(pos).toString()));
                bundleVideo.putString("videoPath", mList.get(pos).toString());
                VideoPlayerActivity.show(mContext, bundleVideo);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.show(TAG,"视频播放失败:" + e);
                ToastUtils.showShort(mContext, "视频播放失败");
            }
        });
    }

}
