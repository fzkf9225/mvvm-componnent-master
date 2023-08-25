package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityMediaBinding;
import com.casic.titan.demo.viewmodel.MediaViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.media.MediaBuilder;
import pers.fz.mvvm.util.media.MediaHelper;
import pers.fz.mvvm.util.media.MediaListener;
import pers.fz.mvvm.util.media.MediaTypeEnum;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.dialog.OpenShootDialog;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
@AndroidEntryPoint
public class MediaActivity extends BaseActivity<MediaViewModel, ActivityMediaBinding> implements ImageAddAdapter.ImageViewAddListener,
        ImageAddAdapter.ImageViewClearListener, VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener {

    private UseCase useCase;
    private ImageAddAdapter imageAddAdapter;
    private VideoAddAdapter videoAddAdapter;

    private MediaHelper mediaHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_media;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void initView(Bundle savedInstanceState) {
        //初始化一些媒体配置
        //新api实现最大可选数量比较鸡肋因此我直接判断选择完的回调方法，当然应该也可以通过重新PickMultipleVisualMedia去实现，没试过看源码应该是可以实现的
        mediaHelper = new MediaBuilder(this, this)
                .setImageMaxSelectedCount(5)
                .setVideoMaxSelectedCount(2)
                .setChooseType(MediaHelper.PICK_TYPE)
                .setMediaListener(new MediaListener() {
                    @Override
                    public int onSelectedImageCount() {
                        return imageAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedVideoCount() {
                        return videoAddAdapter.getList().size();
                    }
                })
                .setImageQualityCompress(200)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                videoAddAdapter.notifyDataSetChanged();
                binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
            }
        });
        imageAddAdapter = new ImageAddAdapter(this, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        binding.imageRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.imageRecyclerView.setAdapter(imageAddAdapter);

        videoAddAdapter = new VideoAddAdapter(this);
        videoAddAdapter.setVideoAddListener(this);
        videoAddAdapter.setVideoClearListener(this);
        binding.videoRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.videoRecyclerView.setAdapter(videoAddAdapter);
        binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
        binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void imgClear(View view, int position) {
        imageAddAdapter.getList().remove(position);
        imageAddAdapter.notifyDataSetChanged();
        binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
    }

    @Override
    public void imgAdd(View view) {
        mediaHelper.openImageDialog(view, OpenImageDialog.CAMERA_ALBUM);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void videoClear(View view, int position) {
        videoAddAdapter.getList().remove(position);
        videoAddAdapter.notifyDataSetChanged();
        binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
    }

    @Override
    public void videoAdd(View view) {
        mediaHelper.openShootDialog(view, OpenShootDialog.CAMERA_ALBUM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaHelper != null) {
            mediaHelper.unregister(this);
        }
    }
}