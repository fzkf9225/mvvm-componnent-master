package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.LogUtil;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormVideo extends FormMedia implements VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener {
    /**
     * 是否启用压缩，默认为true：启用压缩
     */
    protected boolean compress = true;
    /**
     * 压缩质量，默认为低质量
     */
    protected int compressVideo = VideoQualityEnum.MEDIUM.value;
    /**
     * 适配器
     */
    protected VideoAddAdapter videoAddAdapter;
    /**
     * 媒体管理器
     */
    protected MediaHelper mediaHelper;
    /**
     * 媒体选择类型，默认为：相册相机都可
     */
    protected int mediaType = OpenShootDialog.CAMERA_ALBUM;
    /**
     * 添加视频适配监听器
     */
    protected VideoAddAdapter.VideoAddListener videoAddListener;
    /**
     * 删除视频监听器
     */
    protected VideoAddAdapter.VideoClearListener videoClearListener;
    /**
     * 最大可选数量
     */
    protected int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    /**
     * 显示数量标签文字大小
     */
    protected float formCountLabelTextSize;
    /**
     * 显示数量标签文字颜色
     */
    protected int countLabelTextColor;
    /**
     * 是否显示数量标签
     */
    protected boolean showCountLabel;

    /**
     * 显示数量标签控件
     */
    protected AppCompatTextView tvCountLabel;

    public FormVideo(@NonNull Context context) {
        super(context);
    }

    public FormVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            required = typedArray.getBoolean(R.styleable.FormUI_required, false);
            compress = typedArray.getBoolean(R.styleable.FormUI_compress, compress);
            compressVideo = typedArray.getInt(R.styleable.FormUI_compressImageSize, VideoQualityEnum.LOW.value);
            mediaType = typedArray.getInt(R.styleable.FormUI_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormUI_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formCountLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formCountLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            countLabelTextColor = typedArray.getColor(R.styleable.FormUI_countLabelTextColor, ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.nv_bg_color));
            showCountLabel = typedArray.getBoolean(R.styleable.FormUI_showCountLabel, true);
            typedArray.recycle();
        } else {
            showCountLabel = true;
            formCountLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            countLabelTextColor = ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.nv_bg_color);
            compress = true;
            compressVideo = VideoQualityEnum.LOW.value;
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
        }
    }

    /**
     * 修改mediaType值，传 OpenImageDialog.ALBUM/ OpenImageDialog.CAMERA/ OpenImageDialog.CAMERA_ALBUM
     * @param mediaType
     */
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public AppCompatTextView getTvCountLabel() {
        return tvCountLabel;
    }

    @Override
    protected void init() {
        super.init();
        if (showCountLabel) {
            createCountLabel();
            layoutCountLabel();
        }
        videoAddAdapter = new VideoAddAdapter(maxCount);
        videoAddAdapter.setBgColor(bgColor);
        videoAddAdapter.setRadius(radius);
        videoAddAdapter.setErrorImage(errorImage);
        videoAddAdapter.setPlaceholderImage(placeholderImage);
        videoAddAdapter.setVideoAddListener(this);
        videoAddAdapter.setVideoClearListener(this);
        mediaRecyclerView.setAdapter(videoAddAdapter);
    }

    public void createCountLabel() {
        tvCountLabel = new AppCompatTextView(getContext());
        tvCountLabel.setId(View.generateViewId());
        tvCountLabel.setLines(1);
        tvCountLabel.setTextColor(countLabelTextColor);
        tvCountLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formCountLabelTextSize);
        tvCountLabel.setText("0/" + maxCount);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart((int) labelStartMargin);
        params.setMarginEnd((int) labelStartMargin);
        addView(tvCountLabel, params);
    }

    public void layoutCountLabel() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvCountLabel.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(tvCountLabel.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(tvCountLabel.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }


    public void setOnVideoAddListener(VideoAddAdapter.VideoAddListener videoAddListener) {
        this.videoAddListener = videoAddListener;
    }

    public void setOnVideoClearListener(VideoAddAdapter.VideoClearListener onVideoClearListener) {
        this.videoClearListener = onVideoClearListener;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public List<Uri> getImages() {
        return videoAddAdapter.getList();
    }

    public List<String> getStringImages() {
        if (videoAddAdapter.getList() == null) {
            return null;
        }
        List<String> uriStringList = new ArrayList<>();
        for (Uri uri : videoAddAdapter.getList()) {
            uriStringList.add(uri.toString());
        }
        return uriStringList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setStringImages(List<String> images) {
        if (images == null) {
            videoAddAdapter.setList(new ArrayList<>());
            videoAddAdapter.notifyDataSetChanged();
            return;
        }
        List<Uri> uriList = new ArrayList<>();
        for (String img : images) {
            uriList.add(Uri.parse(img));
        }
        videoAddAdapter.setList(uriList);
        videoAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImages(List<Uri> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        videoAddAdapter.setList(images);
        videoAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @Override
    public void videoAdd(View view) {
        if (videoAddListener != null) {
            videoAddListener.videoAdd(view);
            return;
        }
        mediaHelper.openShootDialog(view, mediaType);
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }

    public int getMediaType() {
        return mediaType;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void videoClear(View view, int position) {
        if (videoClearListener != null) {
            videoClearListener.videoClear(view, position);
            return;
        }
        videoAddAdapter.getList().remove(position);
        videoAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    public void refreshCountLabel(){
        if (tvCountLabel == null) {
            return;
        }
        tvCountLabel.setText((videoAddAdapter.getList() == null ? 0 : videoAddAdapter.getList().size()) + "/" + maxCount);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void bindLifecycle(LifecycleOwner lifecycleOwner){
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setVideoMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setVideoQuality(VideoQualityEnum.getInfo(compressVideo))
                .setMediaListener(new MediaListener() {
                    @Override
                    public int onSelectedFileCount() {
                        return 0;
                    }

                    @Override
                    public int onSelectedAudioCount() {
                        return 0;
                    }

                    @Override
                    public int onSelectedImageCount() {
                        return 0;
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
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            LogUtil.show(MediaHelper.TAG,"FormVideo");
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                if (compress) {
                    mediaHelper.startCompressVideo(mediaBean.getMediaList());
                } else {
                    videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                    videoAddAdapter.notifyDataSetChanged();
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());

                    refreshCountLabel();
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                videoAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                refreshCountLabel();
            }
        });
    }

}
