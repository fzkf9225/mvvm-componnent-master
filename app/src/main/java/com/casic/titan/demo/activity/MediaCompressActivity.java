package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityMediaCompressBinding;
import com.casic.titan.demo.viewmodel.MediaCompressViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenMediaDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.module.MediaModule;
import pers.fz.mvvm.adapter.MediaAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.widget.recyclerview.FullyGridLayoutManager;

@AndroidEntryPoint
public class MediaCompressActivity extends BaseActivity<MediaCompressViewModel, ActivityMediaCompressBinding> implements MediaAddAdapter.MediaClearListener,MediaAddAdapter.MediaAddListener {
    private UseCase useCase;
    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;

    private MediaAddAdapter mediaAddAdapter;

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
        mediaAddAdapter = new MediaAddAdapter(mediaHelper.getMediaBuilder().mediaMaxSelectedCount);
        mediaAddAdapter.setMediaClearListener(this);
        mediaAddAdapter.setMediaAddListener(this);
        binding.imageVideoRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.imageVideoRecyclerView.setAdapter(mediaAddAdapter);

        binding.buttonImage.setOnClickListener(v -> mediaHelper.openImageDialog(v, OpenImageDialog.CAMERA_ALBUM));
        binding.buttonVideo.setOnClickListener(v -> mediaHelper.openShootDialog(v, OpenShootDialog.CAMERA_ALBUM));
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());

        mediaHelper.getMediaBuilder()
                .setImageMaxSelectedCount(1)
                .setVideoMaxSelectedCount(1)
                .setImageQualityCompress(300)
                .setVideoQuality(VideoQualityEnum.LOW);
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                binding.setSourceImagePath(mediaBean.getMediaList().get(0));
                mediaHelper.startCompressImage(mediaBean.getMediaList());
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                binding.setSourceVideoPath(mediaBean.getMediaList().get(0));
                mediaHelper.startCompressVideo(mediaBean.getMediaList());
            } else if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE_AND_VIDEO) {
                mediaHelper.startCompressMedia(mediaBean.getMediaList());
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                binding.setCompressImagePath(mediaBean.getMediaList().get(0));
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                binding.setCompressVideoPath(mediaBean.getMediaList().get(0));
            } else if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE_AND_VIDEO) {
                mediaAddAdapter.addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                mediaAddAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void mediaAdd(View view) {
        mediaHelper.openMediaDialog(view, OpenMediaDialog.CAMERA_SHOOT_ALBUM);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void mediaClear(View view, int position) {
        mediaAddAdapter.getList().remove(position);
        mediaAddAdapter.notifyDataSetChanged();
    }
}