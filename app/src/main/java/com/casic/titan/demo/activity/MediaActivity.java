package com.casic.titan.demo.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.dialog.OpenFileDialog;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.listener.MediaListener;
import pers.fz.media.module.MediaModule;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;

@AndroidEntryPoint
public class MediaActivity extends BaseActivity<MediaViewModel, ActivityMediaBinding> implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener, VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener {

    private UseCase useCase;
    private ImageAddAdapter imageAddAdapter;
    private VideoAddAdapter videoAddAdapter;

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
                        return 0;
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
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                binding.tvImage.setText("图片选择（" + imageAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getImageMaxSelectedCount() + "）");
            } else if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                videoAddAdapter.notifyDataSetChanged();
                binding.tvVideo.setText("视频选择（" + videoAddAdapter.getList().size() + "/" + mediaHelper.getMediaBuilder().getVideoMaxSelectedCount() + "）");
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
        imageAddAdapter = new ImageAddAdapter(MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        binding.imageRecyclerView.setLayoutManager(new FullyGridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.imageRecyclerView.setAdapter(imageAddAdapter);

        videoAddAdapter = new VideoAddAdapter();
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
        binding.chooseAudio.setOnClickListener(v -> mediaHelper.openFileDialog(v, null, OpenFileDialog.AUDIO));
        binding.chooseFile.setOnClickListener(v -> mediaHelper.openFileDialog(v, null, OpenFileDialog.FILE));
        binding.waterMarkImage.setOnClickListener(v -> {
            if (binding.getWaterMarkImagePath() == null) {
                return;
            }
            new PreviewPhotoDialog(MediaActivity.this, PreviewPhotoDialog.createUriImageInfo(binding.getWaterMarkImagePath()), 0)
                    .show();
        });
    }

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
}