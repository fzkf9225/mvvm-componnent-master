package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormImageBinding;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.LogUtil;
import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.listener.MediaListener;
import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.wight.recyclerview.GridSpacingItemDecoration;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImage extends ConstraintLayout implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener {
    protected String labelString;
    protected int bgColor;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected boolean compress = false;
    protected int compressImageSize = 300;
    private ImageAddAdapter imageAddAdapter;
    private MediaHelper mediaHelper;
    private int mediaType = OpenImageDialog.CAMERA_ALBUM;
    private ImageAddAdapter.ImageViewAddListener onImageAddListener;
    private ImageAddAdapter.ImageViewClearListener onImageClearListener;
    private int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    //不用转换单位
    private float radius = 8;
    protected int labelTextColor;
    private float formLabelTextSize;
    private float formRequiredSize;
    private FormImageBinding binding;

    public FormImage(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormImage);
            labelString = typedArray.getString(R.styleable.FormImage_label);
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, ContextCompat.getColor(getContext(), R.color.dark_color));
            compress = typedArray.getBoolean(R.styleable.FormImage_compress, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            radius = typedArray.getDimension(R.styleable.FormImage_add_image_radius, DensityUtil.dp2px(getContext(), 8));
            compressImageSize = typedArray.getInt(R.styleable.FormImage_compressImageSize, 300);
            mediaType = typedArray.getInt(R.styleable.FormImage_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormImage_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormImage_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormImage_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            typedArray.recycle();
        } else {
            bgColor = 0xFFF1F3F2;
            labelTextColor = ContextCompat.getColor(getContext(), R.color.dark_color);
            radius = DensityUtil.dp2px(getContext(), 8);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
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

    protected void init() {
        binding = FormImageBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(getPaddingStart(), DensityUtil.dp2px(getContext(), 12),
                getPaddingEnd(), DensityUtil.dp2px(getContext(), 12));
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        binding.tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        imageAddAdapter = new ImageAddAdapter(maxCount);
        imageAddAdapter.setBgColor(bgColor);
        imageAddAdapter.setRadius(radius);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        ConstraintLayout.LayoutParams imageLayoutParams = (LayoutParams) binding.mRecyclerViewImage.getLayoutParams();
        imageLayoutParams.topMargin = DensityUtil.dp2px(getContext(), 6);
        binding.mRecyclerViewImage.setLayoutParams(imageLayoutParams);
        binding.mRecyclerViewImage.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(getContext(), 4), 0x00000000));
        binding.mRecyclerViewImage.setLayoutManager(new FullyGridLayoutManager(getContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        binding.mRecyclerViewImage.setAdapter(imageAddAdapter);
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
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

    @SuppressLint("NotifyDataSetChanged")
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

    @SuppressLint("NotifyDataSetChanged")
    public void setImages(List<Uri> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        imageAddAdapter.setList(images);
        imageAddAdapter.notifyDataSetChanged();
    }

    public void setLabel(String text) {
        binding.tvLabel.setText(text);
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

    @Override
    public void imgClear(View view, int position) {
        if (onImageClearListener != null) {
            onImageClearListener.imgClear(view, position);
            return;
        }
        imageAddAdapter.getList().remove(position);
        imageAddAdapter.notifyDataSetChanged();
    }

    public void bindLifecycle(LifecycleOwner lifecycleOwner){
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
            LogUtil.show(MediaHelper.TAG,"FormImage");
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                if (compress) {
                    mediaHelper.startCompressImage(mediaBean.getMediaList());
                } else {
                    imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                    imageAddAdapter.notifyDataSetChanged();
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(lifecycleOwner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            }
        });
    }

    public FormImageBinding getBinding() {
        return binding;
    }

}
