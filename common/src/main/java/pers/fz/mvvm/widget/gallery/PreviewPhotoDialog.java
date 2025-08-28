package pers.fz.mvvm.widget.gallery;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.AttachmentBean;
import pers.fz.mvvm.enums.AttachmentTypeEnum;
import pers.fz.mvvm.util.common.AttachmentUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.widget.gallery.adapter.PreviewViewPagerAdapter;

/**
 * created by fz 2024/12/20
 * describe：大图预览dialog，支持视频预览，但是默认为图片，如果希望是视频的话必须提前指定AttachmentBean的type
 */
public class PreviewPhotoDialog extends Dialog {
    public final static String TAG = PreviewPhotoDialog.class.getSimpleName();
    /**
     * 图片集合
     */
    private List<AttachmentBean> imageInfos = new ArrayList<>();
    /**
     * 大图预览空间
     */
    private ViewPager2 viewPager;
    /**
     * 下面的点
     */
    private LinearLayout llPoint;
    /**
     * 适配器
     */
    private PreviewViewPagerAdapter pageAdapter;
    /**
     * 当前位置
     */
    private int position = 0;
    /**
     * 是否可以保存图片，默认为true
     */
    private boolean canSaveImage = true;
    /**
     * 选中原点样式
     */
    private Drawable drawableResCurrent = null;
    /**
     * 未选原点样式
     */
    private Drawable drawableResNormal = null;
    /**
     * 触摸事件
     */
    private float startX, startY;
    /**
     * 触摸时间
     */
    private long downTime;
    /**
     * 占位图
     */
    protected Drawable placeholderImage;
    /**
     * 错误图
     */
    protected Drawable errorImage;

    public PreviewPhotoDialog(Context context) {
        this(context, R.style.Pic_Dialog);
    }

    public PreviewPhotoDialog(Context context, int themeResId) {
        super(context, themeResId);
        drawableResCurrent = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.white),
                DensityUtil.dp2px(context, 6));
        drawableResNormal = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.gray),
                DensityUtil.dp2px(context, 6));
    }

    public PreviewPhotoDialog(Context context, boolean canSaveImage) {
        this(context, R.style.Pic_Dialog);
        this.canSaveImage = canSaveImage;
    }

    public PreviewPhotoDialog(Context context, List<AttachmentBean> imageInfos, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.position = position;
    }

    public PreviewPhotoDialog(Context context, List<AttachmentBean> imageInfos, boolean canSaveImage, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.canSaveImage = canSaveImage;
        this.position = position;
    }

    public PreviewPhotoDialog currentPosition(int position) {
        this.position = position;
        return this;
    }

    public PreviewPhotoDialog setImages(List<AttachmentBean> imageInfoList) {
        this.imageInfos = imageInfoList;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        return this;
    }

    public boolean isCanSaveImage() {
        return canSaveImage;
    }

    public Drawable getErrorImage() {
        return errorImage;
    }

    public Drawable getPlaceholderImage() {
        return placeholderImage;
    }

    public PreviewPhotoDialog createImageInfo(String image) {
        AttachmentBean attachmentBean = new AttachmentBean();
        attachmentBean.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
        attachmentBean.setPath(image);
        attachmentBean.setRelativePath(image);
        attachmentBean.setFileName(FileUtil.getFileName(image));
        imageInfos = List.of(attachmentBean);
        return this;
    }

    public PreviewPhotoDialog createImageResInfo(@DrawableRes int imageRes) {
        return createImageResInfo(List.of(imageRes));
    }

    public PreviewPhotoDialog createImageResInfo(@DrawableRes List<Integer> imageResList) {
        imageInfos = AttachmentUtil.drawableResToAttachmentList(getContext(), imageResList,null,null);
        return this;
    }

    public PreviewPhotoDialog createImageInfo(String... image) {
        if (image == null) {
            return null;
        }
        return createImageInfo(Arrays.asList(image));
    }

    @SuppressLint("Range")
    public PreviewPhotoDialog createUriImageInfo(Uri... uri) {
        if (uri == null) {
            return null;
        }
        return createUriImageInfo(Arrays.asList(uri));
    }

    public PreviewPhotoDialog createImageInfo(List<String> images) {
        if (images == null) {
            return null;
        }
        imageInfos = images.stream().map(item -> {
            AttachmentBean attachmentBean = new AttachmentBean();
            attachmentBean.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
            attachmentBean.setPath(item);
            attachmentBean.setRelativePath(item);
            attachmentBean.setFileName(FileUtil.getFileName(item));
            return attachmentBean;
        }).collect(Collectors.toList());
        return this;
    }

    @SuppressLint("Range")
    public PreviewPhotoDialog createUriImageInfo(List<Uri> images) {
        if (images == null) {
            return null;
        }
        imageInfos = AttachmentUtil.uriListToAttachmentList(images);
        return this;
    }

    public PreviewPhotoDialog setDrawableResCurrent(Drawable drawableResCurrent) {
        this.drawableResCurrent = drawableResCurrent;
        return this;
    }

    public PreviewPhotoDialog setDrawableResNormal(Drawable drawableResNormal) {
        this.drawableResNormal = drawableResNormal;
        return this;
    }

    public PreviewPhotoDialog setPlaceholderImage(Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
        return this;
    }

    public PreviewPhotoDialog setErrorImage(Drawable errorImage) {
        this.errorImage = errorImage;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dialog_pic);
        if (getWindow() != null) {
            getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        viewPager = findViewById(R.id.vp);
        llPoint = findViewById(R.id.ll_point);
        initPageAdapter();
        viewPager.setCurrentItem(position);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                super.onPageSelected(position);
                if (llPoint.getChildCount() == 0) {
                    return;
                }
                llPoint.getChildAt(pos).setBackground(drawableResCurrent);
                if (position >= 0 && position < llPoint.getChildCount() && position != pos) {
                    llPoint.getChildAt(position).setBackground(drawableResNormal);
                }
                position = pos;
            }
        });
        //用于拦截ViewPager2的触摸事件，但是无效
//        viewPager.setOnTouchListener(onViewPagerTouchListener);
    }

    /**
     * 这个方法用来判断是否拦截ViewPager的触摸时间，但是不知道为什么无效不执行
     */
    private final View.OnTouchListener onViewPagerTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int pointerCount = event.getPointerCount();  // 获取当前触摸点的数量
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // 记录按下的时间和位置
                    downTime = System.currentTimeMillis();
                    startX = event.getX();
                    startY = event.getY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    // 如果是单指且滑动距离超过100，允许ViewPager2处理事件
                    if (pointerCount == 1) {
                        float deltaX = Math.abs(event.getX() - startX);
                        float deltaY = Math.abs(event.getY() - startY);
                        long duration = System.currentTimeMillis() - downTime;

                        // 满足单指且横向滑动超过100，且时间超过300毫秒
                        if (duration > 300 && deltaX > 100 && deltaY < 50) {
                            return false;  // ViewPager2可以处理事件
                        }
                    }
                    // 如果是双指触摸，或者超过双指触摸，拦截事件交给子View
                    return pointerCount < 2;  // 拦截事件交给子View

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 单击且时间小于300毫秒，拦截事件交给子View
                    long upTime = System.currentTimeMillis();
                    return upTime - downTime > 300;  // 拦截事件交给子View
            }
            return false;
        }
    };

    /***
     * 初始化viewpager适配器
     */
    @SuppressLint("NotifyDataSetChanged")
    private void initPageAdapter() {
        initPoint();
        if (pageAdapter == null) {
            pageAdapter = new PreviewViewPagerAdapter(this);
            pageAdapter.setList(imageInfos);
            viewPager.setAdapter(pageAdapter);
        } else {
            pageAdapter.setList(imageInfos);
            pageAdapter.notifyDataSetChanged();
        }
        viewPager.setOffscreenPageLimit(imageInfos.size());
    }

    private void initPoint() {
        llPoint.removeAllViews();
        if (imageInfos.size() > 1) {
            llPoint.setVisibility(View.VISIBLE);
        } else {
            llPoint.setVisibility(View.INVISIBLE);
        }
        IntStream.range(0, imageInfos.size()).forEach(i -> {
            ImageView round = new ImageView(getContext());
            if (i == position) {
                round.setBackground(drawableResCurrent);
            } else {
                round.setBackground(drawableResNormal);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = DensityUtil.dp2px(getContext(), 8f);
            llPoint.addView(round, params);
        });
    }
}

