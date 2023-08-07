package pers.fz.mvvm.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.databinding.VideoShowItemBinding;

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
            if(onVideoPlayerClickListener!=null) {
                onVideoPlayerClickListener.onVideoPlay(v,pos);
            }
        });
    }

    public interface OnVideoPlayerClickListener{
        void onVideoPlay(View v, int pos);
    }

    private OnVideoPlayerClickListener onVideoPlayerClickListener;

    public void setOnVideoPlayerClickListener(OnVideoPlayerClickListener onVideoPlayerClickListener){
        this.onVideoPlayerClickListener = onVideoPlayerClickListener;
    }

}
