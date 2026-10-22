package io.coderf.arklab.demo.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.activity.VideoPlayerActivity;
import io.coderf.arklab.common.adapter.MediaAddAdapter;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.CollectionUtil;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.ActivityMediaCompressBinding;
import io.coderf.arklab.demo.viewmodel.MediaCompressViewModel;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.dialog.OpenImageDialog;
import io.coderf.arklab.media.dialog.OpenMediaDialog;
import io.coderf.arklab.media.dialog.OpenShootDialog;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.enums.VideoQualityEnum;
import io.coderf.arklab.media.module.ActivityMediaHelper;

/**
 * MediaCompressActivity 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@AndroidEntryPoint
public class MediaCompressActivity extends BaseActivity<MediaCompressViewModel, ActivityMediaCompressBinding> implements MediaAddAdapter.MediaClearListener, MediaAddAdapter.MediaAddListener {
    private UseCase useCase;
    @Inject
    @ActivityMediaHelper
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
        mediaAddAdapter.setBgColor(Color.WHITE);
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

        // 点击源视频 / 压缩后视频缩略图，跳转 VideoPlayerActivity 播放
        binding.video.setOnClickListener(v -> playVideoIfPresent(binding.getSourceVideoPath(), "源视频"));
        binding.videoCompress.setOnClickListener(v -> playVideoIfPresent(binding.getCompressVideoPath(), "压缩后视频"));
        binding.image.setOnClickListener(v -> showPreviewPhoto(binding.getSourceImagePath()));
        binding.imageCompress.setOnClickListener(v -> showPreviewPhoto(binding.getCompressImagePath()));
    }

    /**
     * 打开大图预览
     */
    protected void showPreviewPhoto(Uri imagePath) {
        if (imagePath == null) {
            return;
        }
        new PreviewPhotoDialog(this)
                .createUriImageInfo(imagePath)
                .currentPosition(0)
                .show();
    }

    /**
     * 有有效 Uri 时打开 {@link VideoPlayerActivity} 播放；本地 Uri 关闭缓存。
     *
     * @param uri   视频 Uri，null 时仅提示
     * @param title 播放页标题
     */
    private void playVideoIfPresent(Uri uri, String title) {
        if (uri == null) {
            showToast("暂无可播放的视频");
            return;
        }
        VideoPlayerActivity.show(this, title, uri, false);
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
                .setVideoQuality(VideoQualityEnum.LOW)
                // 相册 HEVC/高分辨率视频偶发卡在 98%/99%，设置超时后回调 CompressListener.onResult 失败
                .setVideoCompressTimeoutMs(90_000L);
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean == null || CollectionUtil.isEmpty(mediaBean.getMediaList())) {
                showToast("未选择图片");
                return;
            }
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