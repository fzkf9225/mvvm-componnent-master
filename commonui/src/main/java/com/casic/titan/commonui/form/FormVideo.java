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
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.utils.AttachmentUtil;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaBuilder;
import pers.fz.media.MediaHelper;
import pers.fz.media.MediaTypeEnum;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.listener.MediaListener;
import pers.fz.media.listener.OnLoadingListener;
import pers.fz.mvvm.adapter.VideoAddAdapter;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseFragment;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.wight.recyclerview.GridSpacingItemDecoration;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormVideo extends ConstraintLayout implements VideoAddAdapter.VideoAddListener, VideoAddAdapter.VideoClearListener, DefaultLifecycleObserver{
    protected String labelString;
    protected int bgColor = 0xFFF1F3F2;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected boolean compress = false;
    protected int compressVideo = MediaHelper.VIDEO_LOW;
    protected TextView tvLabel, tvRequired;
    protected RecyclerView mRecyclerViewVideo;
    private VideoAddAdapter videoAddAdapter;
    private MediaHelper mediaHelper;
    private int mediaType = OpenShootDialog.CAMERA_ALBUM;
    private VideoAddAdapter.VideoAddListener videoAddListener;
    private VideoAddAdapter.VideoClearListener videoClearListener;
    private int maxCount = MediaHelper.DEFAULT_ALBUM_MAX_COUNT;
    //不用转换单位
    private float radius = 4;
    protected int labelTextColor = 0xFF999999;
    private float formLabelTextSize;
    private float formRequiredSize;

    public FormVideo(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormVideo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormVideo(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
            compress = typedArray.getBoolean(R.styleable.FormImage_compress, false);
            radius = typedArray.getDimension(R.styleable.FormImage_add_image_radius, 4);
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, labelTextColor);
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            compressVideo = typedArray.getInt(R.styleable.FormImage_compressImageSize, MediaHelper.VIDEO_LOW);
            mediaType = typedArray.getInt(R.styleable.FormImage_mediaType, OpenImageDialog.CAMERA_ALBUM);
            maxCount = typedArray.getInt(R.styleable.FormImage_maxCount, MediaHelper.DEFAULT_ALBUM_MAX_COUNT);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormImage_formLabelTextSize, DensityUtil.sp2px(getContext(),14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormImage_formRequiredSize, DensityUtil.sp2px(getContext(),14));
            typedArray.recycle();
        } else {
            radius =  DensityUtil.dp2px(getContext(),4);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    /**
     * 修改mediaType值，传 OpenImageDialog.ALBUM/ OpenImageDialog.CAMERA/ OpenImageDialog.CAMERA_ALBUM
     * @param mediaType
     */
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_image, this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(0, DensityUtil.dp2px(getContext(),12),
                0, DensityUtil.dp2px(getContext(),12));
        tvLabel = findViewById(R.id.tv_label);
        mRecyclerViewVideo = findViewById(R.id.mRecyclerViewImage);
        tvRequired = findViewById(R.id.tv_required);
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvLabel.setText(labelString);
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        videoAddAdapter = new VideoAddAdapter(getContext(), maxCount);
        videoAddAdapter.setBgColor(bgColor);
        videoAddAdapter.setRadius(radius);
        videoAddAdapter.setVideoAddListener(this);
        videoAddAdapter.setVideoClearListener(this);
        ConstraintLayout.LayoutParams imageLayoutParams = (LayoutParams) mRecyclerViewVideo.getLayoutParams();
        imageLayoutParams.topMargin = DensityUtil.dp2px(getContext(), 6);
        mRecyclerViewVideo.setLayoutParams(imageLayoutParams);
        mRecyclerViewVideo.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(getContext(),4), 0x00000000));
        mRecyclerViewVideo.setLayoutManager(new FullyGridLayoutManager(getContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerViewVideo.setAdapter(videoAddAdapter);
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
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
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImages(List<Uri> images) {
        if (images == null) {
            images = new ArrayList<>();
        }
        videoAddAdapter.setList(images);
        videoAddAdapter.notifyDataSetChanged();
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
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

    @Override
    public void videoClear(View view, int position) {
        if (videoClearListener != null) {
            videoClearListener.videoClear(view, position);
            return;
        }
        videoAddAdapter.getList().remove(position);
        videoAddAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void initFragment(Fragment fragment) {
        this.mediaHelper = new MediaBuilder(fragment)
                .setVideoMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                .setChooseType(MediaHelper.PICK_TYPE)
                .setVideoQuality(compressVideo)
                .setOnLoadingListener(new OnLoadingListener() {
                    @Override
                    public void showLoading(String dialogMessage) {
                        ((BaseFragment<?, ?>)fragment).showLoading(dialogMessage);
                    }

                    @Override
                    public void refreshLoading(String dialogMessage) {
                        ((BaseFragment<?, ?>)fragment).refreshLoading(dialogMessage);
                    }

                    @Override
                    public void hideLoading() {
                        ((BaseFragment<?, ?>)fragment).hideLoading();
                    }
                })
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
                })
                .builder();
        //图片、视频选择结果回调通知
        mediaHelper.getMutableLiveData().observe(fragment, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                if (compress) {
                    mediaHelper.startCompressVideo(mediaBean.getMediaList());
                } else {
                    videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                    videoAddAdapter.notifyDataSetChanged();
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                }
            }
        });
        mediaHelper.getMutableLiveDataCompress().observe(fragment, mediaBean -> {
            if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                videoAddAdapter.notifyDataSetChanged();
                AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        if (getContext() instanceof ComponentActivity) {
            mediaHelper = new MediaBuilder((ComponentActivity) getContext())
                    .setVideoMaxSelectedCount(maxCount == -1 ? Integer.MAX_VALUE : maxCount)
                    .setChooseType(MediaHelper.PICK_TYPE)
                    .setVideoQuality(compressVideo)
                    .setOnLoadingListener(new OnLoadingListener() {
                        @Override
                        public void showLoading(String dialogMessage) {
                            ((BaseActivity<?, ?>)getContext()).showLoading(dialogMessage);
                        }

                        @Override
                        public void refreshLoading(String dialogMessage) {
                            ((BaseActivity<?, ?>)getContext()).refreshLoading(dialogMessage);
                        }

                        @Override
                        public void hideLoading() {
                            ((BaseActivity<?, ?>)getContext()).hideLoading();
                        }
                    })
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
                    })
                    .builder();
            //图片、视频选择结果回调通知
            mediaHelper.getMutableLiveData().observe(owner, mediaBean -> {
                if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                    if (compress) {
                        mediaHelper.startCompressVideo(mediaBean.getMediaList());
                    } else {
                        videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                        videoAddAdapter.notifyDataSetChanged();
                        AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                    }
                }
            });
            mediaHelper.getMutableLiveDataCompress().observe(owner, mediaBean -> {
                if (mediaBean.getMediaType() == MediaTypeEnum.VIDEO.getMediaType()) {
                    videoAddAdapter.getList().addAll(mediaBean.getMediaList());
                    videoAddAdapter.notifyDataSetChanged();
                    AttachmentUtil.takeUriPermission(getContext(), mediaBean.getMediaList());
                }
            });
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        if (mediaHelper != null) {
            mediaHelper.unregister();
        }
    }


}
