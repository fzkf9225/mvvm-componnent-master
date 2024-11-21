package com.casic.titan.demo.activity;

import android.os.Build;
import android.os.Bundle;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityMediaCompressBinding;
import com.casic.titan.demo.viewmodel.MediaCompressViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.listener.OnLoadingListener;
import pers.fz.mvvm.base.BaseActivity;
@AndroidEntryPoint
public class MediaCompressActivity extends BaseActivity<MediaCompressViewModel, ActivityMediaCompressBinding> {
    private UseCase useCase;
    private MediaHelper mediaHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_media_compress;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.buttonImage.setOnClickListener(v -> {
            mediaHelper.openImageDialog(v, OpenImageDialog.CAMERA_ALBUM);
        });
        binding.buttonVideo.setOnClickListener(v -> {
            mediaHelper.openShootDialog(v, OpenShootDialog.CAMERA_ALBUM);
        });
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());

        mediaHelper = new MediaBuilder(this)
                .setOnLoadingListener(new OnLoadingListener() {
                    @Override
                    public void showLoading(String dialogMessage) {
                       MediaCompressActivity.this.showLoading(dialogMessage);
                    }

                    @Override
                    public void refreshLoading(String dialogMessage) {
                        MediaCompressActivity.this.refreshLoading(dialogMessage);
                    }

                    @Override
                    public void hideLoading() {
                        MediaCompressActivity.this.hideLoading();
                    }
                })
                .setImageMaxSelectedCount(1)
                .setImageQualityCompress(200)
                .setVideoQuality(MediaHelper.VIDEO_MEDIUM)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                binding.setSourceImagePath(mediaBean.getMediaList().get(0));
                mediaHelper.startCompressImage(mediaBean.getMediaList());
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                binding.setSourceVideoPath(mediaBean.getMediaList().get(0));
                mediaHelper.startCompressVideo(mediaBean.getMediaList());
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                binding.setCompressImagePath(mediaBean.getMediaList().get(0));
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                binding.setCompressVideoPath(mediaBean.getMediaList().get(0));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaHelper != null) {
            mediaHelper.unregister(this);
        }
    }
}