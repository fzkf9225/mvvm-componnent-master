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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        viewPager.setOnTouchListener((v, event) -> {
            try {
                return super.onTouchEvent(event);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            return false;
        });
    }

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

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true; // 返回 true 表示事件已被处理
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    public void show() {
        super.show();
        // 确保对话框获得焦点
        if (getWindow() == null) {
            return;
        }
        getWindow().getDecorView().setFocusableInTouchMode(true);
        getWindow().getDecorView().requestFocus();
    }

}

