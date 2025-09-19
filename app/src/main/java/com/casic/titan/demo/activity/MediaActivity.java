package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityMediaBinding;
import com.casic.titan.demo.viewmodel.MediaViewModel;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenFileDialog;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenMediaDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.media.module.MediaModule;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.adapter.MediaAddAdapter;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.enums.UploadStatusEnum;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.utils.log.LogUtil;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;
import pers.fz.mvvm.widget.recyclerview.FullyGridLayoutManager;

@AndroidEntryPoint
public class MediaActivity extends BaseActivity<MediaViewModel, ActivityMediaBinding> implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener,
        VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener, MediaAddAdapter.MediaClearListener, MediaAddAdapter.MediaAddListener {

    private UseCase useCase;
    private ImageAddAdapter imageAddAdapter;
    private VideoAddAdapter videoAddAdapter;
    private MediaAddAdapter mediaAddAdapter;

    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;

    private final List<String> audioList = new ArrayList<>();
    private final List<String> fileList = new ArrayList<>();

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
        mediaHelper.getMediaBuilder()
                .setImageMaxSelectedCount(5)
                .setVideoMaxSelectedCount(2)
                .setMediaMaxSelectedCount(6)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setWaterMark("仅测试使用")
                .setMediaListener(new MediaListener() {
                    @Override
                    public int onSelectedFileCount() {
                        return fileList.size();
                    }

                    @Override
                    public int onSelectedAudioCount() {
                        return audioList.size();
                    }

                    @Override
                    public int onSelectedImageCount() {
                        return imageAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedVideoCount() {
                        return videoAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedMediaCount() {
                        return mediaAddAdapter.getList().size();
                    }
                })
                .setImageQualityCompress(200);
        binding.buttonImage.setOnClickListener(v -> {
            try {
                if (binding.getSourceImagePath() == null) {
                    showToast("请先选择图片");
                    return;
                }
                InputStream inputStream = getContentResolver().openInputStream(binding.getSourceImagePath());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mediaHelper.startWaterMark(bitmap, 66);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showToast(e.getMessage());
            }
        });
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                if (mediaBean.getMediaList() != null && mediaBean.getMediaList().size() > 0) {
                    binding.setSourceImagePath(mediaBean.getMediaList().get(0));
                }
                imageAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                imageAddAdapter.notifyDataSetChanged();
                percentage = 0;
                handler.post(runnable);
                binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                videoAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                videoAddAdapter.notifyDataSetChanged();
                binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
            } else if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE_AND_VIDEO) {
                mediaAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                mediaAddAdapter.notifyDataSetChanged();
                binding.tvImageVideo.setText("图片、视频混合选择（" + mediaAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() + "）");
            } else if (mediaBean.getMediaType() == MediaTypeEnum.AUDIO) {
                audioList.clear();
                audioList.addAll(coverUriToString(mediaBean.getMediaList()));
                binding.chooseAudioResult.setText("音频选择结果：" + new Gson().toJson(audioList));
            } else if (mediaBean.getMediaType() == MediaTypeEnum.FILE) {
                fileList.clear();
                fileList.addAll(coverUriToString(mediaBean.getMediaList()));
                binding.chooseFileResult.setText("文件选择结果：" + new Gson().toJson(fileList));
            }
        });
        mediaHelper.getMutableLiveDataWaterMark().observe(this, mediaBean -> binding.setWaterMarkImagePath(mediaBean.getMediaList().get(0)));
        imageAddAdapter = new ImageAddAdapter(mediaHelper.getMediaBuilder().imageMaxSelectedCount);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        binding.imageRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.imageRecyclerView.setAdapter(imageAddAdapter);

        videoAddAdapter = new VideoAddAdapter(mediaHelper.getMediaBuilder().videoMaxSelectedCount);
        videoAddAdapter.setVideoAddListener(this);
        videoAddAdapter.setVideoClearListener(this);
        binding.videoRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.videoRecyclerView.setAdapter(videoAddAdapter);

        mediaAddAdapter = new MediaAddAdapter(mediaHelper.getMediaBuilder().mediaMaxSelectedCount);
        mediaAddAdapter.setMediaClearListener(this);
        mediaAddAdapter.setMediaAddListener(this);
        binding.imageVideoRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.imageVideoRecyclerView.setAdapter(mediaAddAdapter);

        binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
        binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
        binding.tvImageVideo.setText("图片、视频混合选择（" + mediaAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() + "）");
        binding.chooseAudio.setOnClickListener(v -> mediaHelper.openFileDialog(v, null, OpenFileDialog.AUDIO));
        binding.chooseFile.setOnClickListener(v -> mediaHelper.openFileDialog(v, null, OpenFileDialog.FILE));
        binding.waterMarkImage.setOnClickListener(v -> {
            if (binding.getWaterMarkImagePath() == null) {
                return;
            }
            new PreviewPhotoDialog(MediaActivity.this)
                    .createUriImageInfo(binding.getWaterMarkImagePath())
                    .currentPosition(0)
                    .show();
        });
    }

    private double percentage = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (percentage >= 100) {
                imageAddAdapter.updateUploadStatus(0, UploadStatusEnum.SUCCESS, "上传成功！");
                handler.removeCallbacks(runnable);
                return;
            }
            percentage++;
            imageAddAdapter.updateUploadStatus(0, UploadStatusEnum.UPLOADING, percentage + "%");
            handler.postDelayed(runnable, 300);
        }
    };

    private List<String> coverUriToString(List<Uri> uriList) {
        if (uriList == null) {
            return null;
        }
        List<String> resultList = new ArrayList<>();
        for (Uri uri : uriList) {
            resultList.add(uri.toString());
        }
        return resultList;
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
        LogUtil.show(TAG,"上传是否成功："+imageAddAdapter.isUploadingSuccess());
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
    public void mediaAdd(View view) {
        mediaHelper.openMediaDialog(view, OpenMediaDialog.CAMERA_SHOOT_ALBUM);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void mediaClear(View view, int position) {
        mediaAddAdapter.getList().remove(position);
        mediaAddAdapter.notifyDataSetChanged();
        binding.tvImageVideo.setText("图片、视频混合选择（" + mediaAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getMediaMaxSelectedCount() + "）");
    }
}