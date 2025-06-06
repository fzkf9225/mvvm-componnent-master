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

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImage extends FormMedia implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener {
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

    public List<Uri> getImages() {
        return imageAddAdapter.getList();
    }

    public List<String> getStringImages() {
        if (imageAddAdapter.getList() == null) {
            return null;
        }
        List<String> uriStringList = new ArrayList<>();
        for (Uri uri : imageAddAdapter.getList()) {
            uriStringList.add(uri.toString());
        }
        return uriStringList;
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setStringImages(List<String> images) {
        if (images == null) {
            imageAddAdapter.setList(new ArrayList<>());
            imageAddAdapter.notifyDataSetChanged();
            return;
        }
        List<Uri> uriList = new ArrayList<>();
        for (String img : images) {
            uriList.add(Uri.parse(img));
        }
        imageAddAdapter.setList(uriList);
        imageAddAdapter.notifyDataSetChanged();
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void setImages(List<Uri> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        imageAddAdapter.setList(images);
        imageAddAdapter.notifyDataSetChanged();
        refreshCountLabel();
    }

    @SuppressLint("SetTextI18n")
    public void refreshCountLabel(){
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
                .setChooseType(MediaHelper.PICK_TYPE)
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
                })
                .setImageQualityCompress(compressImageSize)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                if (compress) {
                    mediaHelper.startCompressImage(mediaBean.getMediaList());
                } else {
                    imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                    imageAddAdapter.notifyDataSetChanged();
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                    refreshCountLabel();
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                refreshCountLabel();
            }
        });
    }

}
