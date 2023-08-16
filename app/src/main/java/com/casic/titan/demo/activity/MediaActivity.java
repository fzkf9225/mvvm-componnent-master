package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityMediaBinding;
import com.casic.titan.demo.viewmodel.MediaViewModel;

import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.media.MediaBuilder;
import pers.fz.mvvm.util.media.MediaHelper;
import pers.fz.mvvm.util.media.MediaTypeEnum;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.dialog.OpenShootDialog;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;

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

    @Override
    public void initView(Bundle savedInstanceState) {
        //初始化一些媒体配置
        //新api不支持最大可选张数，因此没有实现，当然你可以变通很多方式去实现它，后期可能会新增吧
        mediaHelper = new MediaBuilder(this, this)
                .setImageMaxSelectedCount(9)
                .setImageQualityCompress(200)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                mediaHelper.setCurrentImageCount(imageAddAdapter.getList().size());
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                videoAddAdapter.notifyDataSetChanged();
                mediaHelper.setCurrentImageCount(videoAddAdapter.getList().size());
            }
        });
        imageAddAdapter = new ImageAddAdapter(this);
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
        mediaHelper.setCurrentImageCount(imageAddAdapter.getList().size());
    }

    @Override
    public void imgAdd(View view) {
        mediaHelper.openImageDialog(view,OpenImageDialog.CAMERA_ALBUM);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void videoClear(View view, int position) {
        videoAddAdapter.getList().remove(position);
        videoAddAdapter.notifyDataSetChanged();
        mediaHelper.setCurrentImageCount(videoAddAdapter.getList().size());
    }

    @Override
    public void videoAdd(View view) {
        mediaHelper.openShootDialog(view,OpenShootDialog.CAMERA_ALBUM);
    }
}