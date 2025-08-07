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

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenMediaDialog;
import pers.fz.media.enums.MediaPickerTypeEnum;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.adapter.MediaAddAdapter;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImageAndVideo extends FormMedia implements MediaAddAdapter.MediaAddListener, MediaAddAdapter.MediaClearListener {
    /**
     * 是否压缩，默认为true：启用压缩
     */
    protected boolean compress = true;
    /**
     * 压缩图片大小，默认为300kb
     */
    protected int compressImageSize = 300;
    /**
     * 压缩质量，默认为低质量
     */
    protected int compressVideo = VideoQualityEnum.MEDIUM.value;
    /**
     * 适配器
     */
    protected MediaAddAdapter mediaAddAdapter;
    /**
     * 媒体管理器
     */
    protected MediaHelper mediaHelper;
    /**
     * 媒体选择类型，默认：相机和相册
     */
    protected int mediaType = OpenMediaDialog.CAMERA_SHOOT_ALBUM;
    /**
     * 添加图片监听器
     */
    protected MediaAddAdapter.MediaAddListener onMediaAddListener;
    /**
     * 删除图片监听器
     */
    protected MediaAddAdapter.MediaClearListener onMediaClearListener;
    /**
     * 最大可选数量
     */
    protected int maxCount = MediaHelper.DEFAULT_MEDIA_MAX_COUNT;
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
    /**
     * 拍摄视频最大时长，单位：秒，默认为30秒
     */
    protected int maxVideoDuration = 30;

    public FormImageAndVideo(@NonNull Context context) {
        super(context);
    }

    public FormImageAndVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormImageAndVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            compress = typedArray.getBoolean(R.styleable.FormUI_compress, true);
            maxVideoDuration = typedArray.getInt(R.styleable.FormUI_maxVideoDuration, 30);
            formCountLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formCountLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            compressVideo = typedArray.getInt(R.styleable.FormUI_compress_video, VideoQualityEnum.LOW.value);
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
            maxVideoDuration = 30;
            compressVideo = VideoQualityEnum.LOW.value;
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
        mediaAddAdapter = new MediaAddAdapter(maxCount);
        mediaAddAdapter.setBgColor(bgColor);
        mediaAddAdapter.setRadius(radius);
        mediaAddAdapter.setErrorImage(errorImage);
        mediaAddAdapter.setPlaceholderImage(placeholderImage);
        mediaAddAdapter.setMediaAddListener(this);
        mediaAddAdapter.setMediaClearListener(this);
        mediaRecyclerView.setAdapter(mediaAddAdapter);
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

    public void setOnMediaClearListener(MediaAddAdapter.MediaClearListener onMediaClearListener) {
        this.onMediaClearListener = onMediaClearListener;
    }

    public void setOnMediaAddListener(MediaAddAdapter.MediaAddListener onMediaAddListener) {
        this.onMediaAddListener = onMediaAddListener;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public List<AttachmentBean> getMedia() {
        return mediaAddAdapter.getList();
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setUriMedia(List<Uri> mediaList) {
        if (mediaList == null) {
            mediaAddAdapter.setList(new ArrayList<>());
            mediaAddAdapter.notifyDataSetChanged();
            return;
        }
        mediaAddAdapter.setList(AttachmentUtil.uriListToAttachmentList(mediaList));
        mediaAddAdapter.notifyDataSetChanged();
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setMedia(List<AttachmentBean> mediaList) {
        if (mediaList == null) {
            mediaList = new ArrayList<>();
        }
        mediaAddAdapter.setList(mediaList);
        mediaAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint("SetTextI18n")
    public void refreshCountLabel() {
        if (tvCountLabel == null) {
            return;
        }
        tvCountLabel.setText((mediaAddAdapter.getList() == null ? 0 : mediaAddAdapter.getList().size()) + "/" + maxCount);
    }


    @Override
    public void mediaAdd(View view) {
        if (onMediaAddListener != null) {
            onMediaAddListener.mediaAdd(view);
            return;
        }
        mediaHelper.openMediaDialog(view, mediaType);
    }

    @Override
    public void mediaClear(View view, int position) {
        if (onMediaClearListener != null) {
            onMediaClearListener.mediaClear(view, position);
            return;
        }
        mediaAddAdapter.getList().remove(position);
        mediaAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    public MediaHelper getMediaHelper() {
        return mediaHelper;
    }

    public int getMediaType() {
        return mediaType;
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void bindLifecycle(LifecycleOwner lifecycleOwner) {
        mediaHelper = new MediaBuilder(getContext())
                .bindLifeCycle(lifecycleOwner)
                .setMediaMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setImageQualityCompress(compressImageSize)
                .setMaxVideoTime(maxVideoDuration)
                .setVideoQuality(VideoQualityEnum.getInfo(compressVideo))
                .setShowPermissionDialog(protocolDialog)
                .setMediaType(fileType)
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
                        return 0;
                    }

                    @Override
                    public int onSelectedMediaCount() {
                        return mediaAddAdapter.getList().size();
                    }
                })
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE_AND_VIDEO) {
                if (compress) {
                    mediaHelper.startCompressMedia(mediaBean.getMediaList());
                } else {
                    mediaAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                    mediaAddAdapter.notifyDataSetChanged();
                    if(requireUriPermission){
                        AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                    }
                    refreshCountLabel();
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE_AND_VIDEO) {
                mediaAddAdapter.getList().addAll(AttachmentUtil.uriListToAttachmentList(mediaBean.getMediaList()));
                mediaAddAdapter.notifyDataSetChanged();
                if(requireUriPermission){
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                }
                refreshCountLabel();
            }
        });
    }

}
