package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;


import com.casic.titan.commonui.R;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.adapter.ImageAddAdapter;
import pers.fz.mvvm.base.BaseModelEntity;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.media.MediaBuilder;
import pers.fz.mvvm.util.media.MediaHelper;
import pers.fz.mvvm.util.media.MediaListener;
import pers.fz.mvvm.util.media.MediaTypeEnum;
import pers.fz.mvvm.wight.dialog.CustomProgressDialog;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormImage extends ConstraintLayout implements ImageAddAdapter.ImageViewAddListener, ImageAddAdapter.ImageViewClearListener, DefaultLifecycleObserver,
        BaseView {
    protected String labelString;
    protected int bgColor;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected TextView tvLabel, tvRequired;
    protected TextView tvSelection;
    protected RecyclerView mRecyclerViewImage;
    private ImageAddAdapter imageAddAdapter;
    private MediaHelper mediaHelper;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int mediaType = OpenImageDialog.CAMERA_ALBUM;
    private ImageAddAdapter.ImageViewAddListener onImageAddListener;
    private ImageAddAdapter.ImageViewClearListener onImageClearListener;
    private int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;

    protected int labelTextColor = 0xFF666666;
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
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            mediaType = typedArray.getInt(R.styleable.FormImage_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormImage_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, labelTextColor);
            typedArray.recycle();
        }
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_image, this, true);
        setLayoutParams(new Constraints.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        setPadding(0, DensityUtil.dp2px(getContext(),12),
                0, DensityUtil.dp2px(getContext(),12));
        tvLabel = findViewById(R.id.tv_label);
        mRecyclerViewImage = findViewById(R.id.mRecyclerViewImage);
        tvRequired = findViewById(R.id.tv_required);
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvLabel.setText(labelString);
        tvLabel.setTextColor(labelTextColor);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        imageAddAdapter = new ImageAddAdapter(getContext(), maxCount);
        imageAddAdapter.setImageViewAddListener(this);
        imageAddAdapter.setImageViewClearListener(this);
        mRecyclerViewImage.setLayoutManager(new FullyGridLayoutManager(getContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerViewImage.setAdapter(imageAddAdapter);
        if (mediaHelper != null) {
            mediaHelper.getMediaBuilder().setImageMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount);
        }
    }

    public void setOnImageAddListener(ImageAddAdapter.ImageViewAddListener onImageAddListener) {
        this.onImageAddListener = onImageAddListener;
    }

    public void setOnImageClearListener(ImageAddAdapter.ImageViewClearListener onImageClearListener) {
        this.onImageClearListener = onImageClearListener;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
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

    public void setImages(List<Uri> images) {
        imageAddAdapter.setList(images);
        imageAddAdapter.notifyDataSetChanged();
    }

    public CharSequence getText() {
        return tvSelection.getText();
    }

    public void setText(String text) {
        tvSelection.setText(text);
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
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


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        mediaHelper = new MediaBuilder((ComponentActivity) getContext(), this)
                .setImageMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaHelper.PICK_TYPE)
                .setWaterMark("金光林务")
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
                .setImageQualityCompress(200)
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(owner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(owner, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.IMAGE.getMediaType()) {
                imageAddAdapter.getList().addAll(mediaBean.getMediaList());
                imageAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            }
        });
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        if (mediaHelper != null) {
            mediaHelper.unregister();
        }
    }

    @Override
    public void showLoading(String dialogMessage) {
        handler.post(() -> CustomProgressDialog.getInstance(getContext())
                .setCanCancel(false)
                .setMessage(dialogMessage)
                .builder()
                .show());
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        handler.post(() -> CustomProgressDialog.getInstance(getContext())
                .refreshMessage(dialogMessage));
    }

    @Override
    public void hideLoading() {
        handler.post(() -> CustomProgressDialog.getInstance(getContext()).dismiss());
    }

    @Override
    public void showToast(String s) {
        handler.post(() -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onErrorCode(BaseModelEntity model) {

    }

}
