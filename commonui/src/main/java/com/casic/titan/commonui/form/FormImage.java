package com.casic.titan.commonui.form;

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

import com.casic.titan.commonui.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.api.ErrorConsumer;
import pers.fz.mvvm.bean.ApiRequestOptions;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.enums.UploadStatusEnum;
import pers.fz.mvvm.listener.OnUploadRetryClickListener;
import pers.fz.mvvm.utils.common.AttachmentUtil;
import pers.fz.mvvm.utils.common.CollectionUtil;
import pers.fz.mvvm.utils.common.DensityUtil;
import pers.fz.mvvm.utils.common.FileUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImage extends FormMedia implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener, OnUploadRetryClickListener {
    /**
     * 是否压缩，默认为true：启用压缩
     */
    protected boolean compress = true;
    /**
     * 压缩图片大小，默认为300kb
     */
    protected int compressImageSize = 300;
    /**
     * 适配器
     */
    protected ImageAddAdapter imageAddAdapter;
    /**
     * 媒体管理器
     */
    protected MediaHelper mediaHelper;
    /**
     * 媒体选择类型，默认：相机和相册
     */
    protected int mediaType = OpenImageDialog.CAMERA_ALBUM;
    /**
     * 添加图片监听器
     */
    protected ImageAddAdapter.ImageViewAddListener onImageAddListener;
    /**
     * 删除图片监听器
     */
    protected ImageAddAdapter.ImageViewClearListener onImageClearListener;
    /**
     * 最大可选数量
     */
    protected int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    /**
     * 显示数量标签文字大小
     */
    protected float formCountLabelTextSize;
    /**
     * 显示数量标签颜色
     */
    protected int countLabelTextColor;
    /**
     * 是否显示数量标签
     */
    protected boolean showCountLabel;
    /**
     * 数量标签控件
     */
    protected AppCompatTextView tvCountLabel;

    public FormImage(@NonNull Context context) {
        super(context);
    }

    public FormImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            compress = typedArray.getBoolean(R.styleable.FormUI_compress, true);
            formCountLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formCountLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            countLabelTextColor = typedArray.getColor(R.styleable.FormUI_countLabelTextColor, ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.nv_bg_color));
            showCountLabel = typedArray.getBoolean(R.styleable.FormUI_showCountLabel, true);
            compressImageSize = typedArray.getInt(R.styleable.FormUI_compressImageSize, 300);
            mediaType = typedArray.getInt(R.styleable.FormUI_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormUI_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            typedArray.recycle();
        } else {
            showCountLabel = true;
            formCountLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            countLabelTextColor = ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.nv_bg_color);
            compress = true;
            compressImageSize = 300;
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
        }
    }

    @Override
    public String[] defaultFileType() {
        return new String[]{"image/*"};
    }

    public AppCompatTextView getTvCountLabel() {
        return tvCountLabel;
    }

    /**
     * 修改mediaType值，传 OpenImageDialog.ALBUM/ OpenImageDialog.CAMERA/ OpenImageDialog.CAMERA_ALBUM
     *
     * @param mediaType
     */
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    protected void init() {
        super.init();
        if (showCountLabel) {
            createCountLabel();
            layoutCountLabel();
        }
        imageAddAdapter = new ImageAddAdapter(maxCount);
        imageAddAdapter.setBgColor(bgColor);
        imageAddAdapter.setRadius(radius);
        imageAddAdapter.setErrorImage(errorImage);
        imageAddAdapter.setPlaceholderImage(placeholderImage);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        imageAddAdapter.setOnUploadRetryClickListener(this);
        mediaRecyclerView.setAdapter(imageAddAdapter);
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

    public void setOnImageAddListener(ImageAddAdapter.ImageViewAddListener onImageAddListener) {
        this.onImageAddListener = onImageAddListener;
    }

    public void setOnImageClearListener(ImageAddAdapter.ImageViewClearListener onImageClearListener) {
        this.onImageClearListener = onImageClearListener;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public List<AttachmentBean> getImages() {
        return imageAddAdapter.getList();
    }

    public ImageAddAdapter getAdapter() {
        return imageAddAdapter;
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setUriImages(List<Uri> images) {
        if (images == null) {
            imageAddAdapter.setList(new ArrayList<>());
            imageAddAdapter.notifyDataSetChanged();
            return;
        }
        imageAddAdapter.setList(AttachmentUtil.uriListToAttachmentList(images));
        imageAddAdapter.notifyDataSetChanged();
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
                                    imageAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                            percent == 100 ? UploadStatusEnum.SUCCESS : UploadStatusEnum.UPLOADING,
                                            percent + "")
                            ));
                    // 执行上传请求
                    return fileApiService.performUpload(url, filePart)
                            .doOnSubscribe(dis-> handler.post(() ->
                                    imageAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                            UploadStatusEnum.UPLOADING, "开始上传")
                            ))
                            .doOnNext(responseBody -> {
                                attachmentBean.setUploadInfo(responseBody);
                                handler.post(() ->
                                        imageAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                                UploadStatusEnum.SUCCESS, "上传成功")
                                );
                            })
                            .onErrorResumeNext(throwable -> {
                                // 单个上传失败时，发送一个空响应并继续
                                handler.post(() ->
                                        imageAddAdapter.updateUploadStatus(attachmentBean.getMobileId(),
                                                UploadStatusEnum.FAILURE, "点击重试")
                                );
                                return Observable.empty(); // 返回空Observable继续执行
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

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setImages(List<AttachmentBean> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        imageAddAdapter.setList(images);
        imageAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint("SetTextI18n")
    public void refreshCountLabel() {
        if (tvCountLabel == null) {
            return;
        }
        tvCountLabel.setText((imageAddAdapter.getList() == null ? 0 : imageAddAdapter.getList().size()) + "/" + maxCount);
    }

    @Override
    public void imgAdd(View view) {
        if (onImageAddListener != null) {
            onImageAddListener.imgAdd(view);
            return;
        }
        mediaHelper.openImageDialog(view, mediaType);
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }

    public int getMediaType() {
        return mediaType;
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void imgClear(View view, int position) {
        if (onImageClearListener != null) {
            onImageClearListener.imgClear(view, position);
            return;
        }
        imageAddAdapter.getList().remove(position);
        imageAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setImageMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setShowPermissionDialog(protocolDialog)
                .setImageType(fileType)
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
                        return imageAddAdapter.getList().size();
                    }

                    @Override
                    public int onSelectedVideoCount() {
                        return 0;
                    }

                    @Override
                    public int onSelectedMediaCount() {
                        return 0;
                    }
                })
                .setImageQualityCompress(compressImageSize)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                if (compress) {
                    mediaHelper.startCompressImage(mediaBean.getMediaList());
                } else {
                    List<AttachmentBean> attachmentList = AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList());
                    imageAddAdapter.getList().addAll(attachmentList);
                    imageAddAdapter.notifyDataSetChanged();
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
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE) {
                List<AttachmentBean> attachmentList = AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList());
                imageAddAdapter.getList().addAll(attachmentList);
                imageAddAdapter.notifyDataSetChanged();
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
        upload(uploadUrl, Collections.singletonList(imageAddAdapter.getList().get(pos)));
    }
}
