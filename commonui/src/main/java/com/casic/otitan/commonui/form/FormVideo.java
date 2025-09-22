package com.casic.otitan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.otitan.commonui.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;
import com.casic.otitan.media.MediaBuilder;
import com.casic.otitan.media.MediaHelper;
import com.casic.otitan.media.dialog.OpenShootDialog;
import com.casic.otitan.media.enums.MediaPickerTypeEnum;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.enums.VideoQualityEnum;
import com.casic.otitan.media.listener.MediaListener;
import com.casic.otitan.media.utils.LogUtil;
import com.casic.otitan.common.adapter.VideoAddAdapter;
import com.casic.otitan.common.api.ErrorConsumer;
import com.casic.otitan.common.bean.ApiRequestOptions;
import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.enums.UploadStatusEnum;
import com.casic.otitan.common.listener.OnUploadRetryClickListener;
import com.casic.otitan.common.utils.common.AttachmentUtil;
import com.casic.otitan.common.utils.common.CollectionUtil;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.FileUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormVideo extends FormMedia implements VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener, OnUploadRetryClickListener {
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
     * 拍摄视频最大时长，单位：秒，默认为30秒
     */
    protected int maxVideoDuration = 30;

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
            maxVideoDuration = typedArray.getInt(R.styleable.FormUI_maxVideoDuration, 30);
            compressVideo = typedArray.getInt(R.styleable.FormUI_compress_video, VideoQualityEnum.LOW.value);
            mediaType = typedArray.getInt(R.styleable.FormUI_mediaType, OpenShootDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormUI_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formCountLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formCountLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            countLabelTextColor = typedArray.getColor(R.styleable.FormUI_countLabelTextColor, ContextCompat.getColor(getContext(), com.casic.otitan.common.R.color.nv_bg_color));
            showCountLabel = typedArray.getBoolean(R.styleable.FormUI_showCountLabel, true);
            typedArray.recycle();
        } else {
            mediaType = OpenShootDialog.CAMERA_ALBUM;
            showCountLabel = true;
            maxVideoDuration = 30;
            formCountLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            countLabelTextColor = ContextCompat.getColor(getContext(), com.casic.otitan.common.R.color.nv_bg_color);
            compress = true;
            compressVideo = VideoQualityEnum.LOW.value;
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
        }
    }

    /**
     * 修改mediaType值，传 OpenImageDialog.ALBUM/ OpenImageDialog.CAMERA/ OpenImageDialog.CAMERA_ALBUM
     *
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
        videoAddAdapter.setOnUploadRetryClickListener(this);
        mediaRecyclerView.setAdapter(videoAddAdapter);
    }

    /**
     * 自动上传图片
     *
     * @param url            相对地址
     * @param attachmentList 文件
     */
    public void upload(String url, List<AttachmentBean> attachmentList) {
        if (CollectionUtil.isEmpty(attachmentList)) {
            return;
        }
        if (fileApiService == null) {
            throw new NullPointerException("fileApiService is null");
        }
        Disposable disposable = Observable.fromIterable(attachmentList)
                .flatMap(attachmentBean -> {
                    // 创建文件部分并处理上传进度
                    MultipartBody.Part filePart = FileUtil.createTempFilePart(getContext(),
                            Uri.parse(attachmentBean.getPath()),
                            (uri, currentPos, totalCount, percent) -> handler.post(() ->
                                    videoAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                            percent == 100 ? UploadStatusEnum.SUCCESS : UploadStatusEnum.UPLOADING,
                                            percent + "")
                            ));
                    // 执行上传请求
                    return fileApiService.performUpload(url, filePart)
                            .doOnSubscribe(dis -> handler.post(() ->
                                    videoAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                            UploadStatusEnum.UPLOADING, "开始上传")
                            ))
                            .doOnNext(responseBody -> {
                                attachmentBean.setUploadInfo(responseBody);
                                handler.post(() ->
                                        videoAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                                UploadStatusEnum.SUCCESS, "上传成功")
                                );
                            })
                            .onErrorResumeNext(throwable -> {
                                handler.post(() ->
                                        videoAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                                UploadStatusEnum.FAILURE, "点击重试")
                                );
                                return Observable.empty();
                            });
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    // 处理成功响应
                }, baseView == null ? error -> {
                    // 处理错误
                } : new ErrorConsumer(baseView, new ApiRequestOptions.Builder()
                        .setShowDialog(false)
                        .setShowToast(true)
                        .build()));
    }

    public VideoAddAdapter getAdapter() {
        return videoAddAdapter;
    }

    @Override
    public String[] defaultFileType() {
        return new String[]{"video/*"};
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

    public List<AttachmentBean> getImages() {
        return videoAddAdapter.getList();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setUriImages(List<Uri> images) {
        if (images == null) {
            videoAddAdapter.setList(new ArrayList<>());
            videoAddAdapter.notifyDataSetChanged();
            return;
        }
        videoAddAdapter.setList(AttachmentUtil.uriListToAttachmentList(images));
        videoAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setVideos(List<AttachmentBean> videos) {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        videoAddAdapter.setList(videos);
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

    public void refreshCountLabel() {
        if (tvCountLabel == null) {
            return;
        }
        tvCountLabel.setText((videoAddAdapter.getList() == null ? 0 : videoAddAdapter.getList().size()) + "/" + maxCount);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setVideoMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setVideoQuality(VideoQualityEnum.getInfo(compressVideo))
                .setVideoType(fileType)
                .setMaxVideoTime(maxVideoDuration)
                .setShowPermissionDialog(protocolDialog)
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
            LogUtil.show(MediaHelper.TAG, "FormVideo");
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                if (compress) {
                    mediaHelper.startCompressVideo(mediaBean.getMediaList());
                } else {
                    List<AttachmentBean> attachmentList = AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList());

                    videoAddAdapter.getList().addAll(attachmentList);
                    videoAddAdapter.notifyDataSetChanged();
                    if (requireUriPermission) {
                        AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                    }
                    refreshCountLabel();
                    if (!autoUpload) {
                        return;
                    }
                    if (handler == null) {
                        handler = new Handler(Looper.getMainLooper());
                    }
                    upload(uploadUrl, attachmentList);
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO) {
                List<AttachmentBean> attachmentList = AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList());

                videoAddAdapter.getList().addAll(attachmentList);
                videoAddAdapter.notifyDataSetChanged();
                if (requireUriPermission) {
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                }
                refreshCountLabel();
                if (!autoUpload) {
                    return;
                }
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
                upload(uploadUrl, attachmentList);
            }
        });
    }

    @Override
    public void onRetryClick(@NotNull View v, int pos) {
        upload(uploadUrl, Collections.singletonList(videoAddAdapter.getList().get(pos)));
    }

}
