package pers.fz.mvvm.wight.gallery;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.CommonUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz 2024/12/20
 * describe：大图预览dialog
 */
public class PreviewPhotoDialog extends Dialog {
    public final static String TAG = PreviewPhotoDialog.class.getSimpleName();
    private final Context context;
    private List<Object> imageInfos;
    private ViewPager2 viewPager;
    private LinearLayout llPoint;
    private PreviewViewPagerAdapter pageAdapter;
    private int position = -1;
    private boolean canSaveImage = true;
    private Drawable drawableResCurrent = null;
    private Drawable drawableResNormal = null;
    private float startX,startY;
    private long downTime;

    public PreviewPhotoDialog(Context context) {
        super(context, R.style.Pic_Dialog);
        this.context = context;
    }

    public PreviewPhotoDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        drawableResCurrent = CommonUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.white),
                DensityUtil.dp2px(context, 6));
        drawableResNormal = CommonUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.gray),
                DensityUtil.dp2px(context, 6));
    }

    public PreviewPhotoDialog(Context context, boolean canSaveImage) {
        super(context, R.style.Pic_Dialog);
        this.context = context;
        this.canSaveImage = canSaveImage;
    }

    public PreviewPhotoDialog(Context context, List<Object> imageInfos, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.position = position;
    }

    public PreviewPhotoDialog(Context context, List<Object> imageInfos, boolean canSaveImage, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.canSaveImage = canSaveImage;
        this.position = position;
    }

    public PreviewPhotoDialog setImages(List<Object> imageInfoList) {
        this.imageInfos = imageInfoList;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        return this;
    }

    public boolean isCanSaveImage() {
        return canSaveImage;
    }

    public static List<Object> createImageInfo(String image) {
        return List.of(image);
    }

    public static List<Object> createImageInfo(@DrawableRes int imageRes) {
        return List.of(imageRes);
    }

    public static List<Object> createImageInfo(Bitmap bitmap) {
        return List.of(bitmap);
    }

    public static List<Object> createImageInfo(String... image) {
        if (image == null) {
            return null;
        }
        List<Object> imageInfos = new ArrayList<>();
        Collections.addAll(imageInfos, image);
        return imageInfos;
    }

    public static List<Object> createUriImageInfo(Uri... uri) {
        if (uri == null) {
            return null;
        }
        List<Object> imageInfos = new ArrayList<>();
        Collections.addAll(imageInfos, uri);
        return imageInfos;
    }

    public static List<Object> createImageInfo(List<String> images) {
        if (images == null) {
            return null;
        }
        return new ArrayList<>(images);
    }

    public static List<Object> createUriImageInfo(List<Uri> images) {
        if (images == null) {
            return null;
        }
        return new ArrayList<>(images);
    }

    public void setDrawableResCurrent(Drawable drawableResCurrent) {
        this.drawableResCurrent = drawableResCurrent;
    }

    public void setDrawableResNormal(Drawable drawableResNormal) {
        this.drawableResNormal = drawableResNormal;
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
            params.leftMargin = DensityUtil.dp2px(context, 8f);
            llPoint.addView(round, params);
        });
    }
}

