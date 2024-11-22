package pers.fz.mvvm.wight.picdialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.ThreadExecutorBounded;
import pers.fz.mvvm.util.download.DownLoadImageService;
import pers.fz.mvvm.util.download.ImageDownLoadCallBack;
import pers.fz.mvvm.wight.dialog.ImageSaveDialog;

/**
 * 主界面点击发布，弹出半透明对话框
 */
public class PicShowDialog extends Dialog {
    private final Context context;
    private List<Object> imageInfos;
    private MyViewPager vp;
    private final List<View> dotsView = new ArrayList<>();
    private LayoutAnimationController lac;
    private LinearLayout ll_point;
    private ViewPagerAdapter pageAdapter;
    private int position;
    private boolean canSaveImage = true;
    private final @DrawableRes int drawableResCurrent = R.mipmap.icon_point2;
    private final @DrawableRes int drawableResNormal = R.mipmap.icon_point1;

    public PicShowDialog(Context context) {
        super(context, R.style.Pic_Dialog);
        this.context = context;
    }

    public PicShowDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    public PicShowDialog(Context context, boolean canSaveImage) {
        super(context, R.style.Pic_Dialog);
        this.context = context;
        this.canSaveImage = canSaveImage;
    }

    public PicShowDialog(Context context, List<Object> imageInfos, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.position = position;
    }

    public PicShowDialog(Context context, List<Object> imageInfos, boolean canSaveImage, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        this.canSaveImage = canSaveImage;
        this.position = position;
    }

    public PicShowDialog setImages(List<Object> imageInfoList) {
        this.imageInfos = imageInfoList;
        if (this.imageInfos == null) {
            this.imageInfos = new ArrayList<>();
        }
        return this;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dialog_pic);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        vp = findViewById(R.id.vp);
        ll_point = findViewById(R.id.ll_point);
        initMyPageAdapter();
        vp.setCurrentItem(position);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (dotsView.isEmpty()) {
                    return;
                }
                IntStream.range(0, dotsView.size()).forEach(i -> {
                    if (i == position) {
                        dotsView.get(i).setBackground(ContextCompat.getDrawable(getContext(), drawableResCurrent));
                    } else {
                        dotsView.get(i).setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    /***
     * 初始化viewpager适配器
     */

    private void initMyPageAdapter() {
        initPoint();
        if (pageAdapter == null) {
            pageAdapter = new ViewPagerAdapter();
            vp.setAdapter(pageAdapter);
        } else {
            pageAdapter.notifyDataSetChanged();
        }
    }

    private void initPoint() {
        dotsView.clear();
        ll_point.removeAllViews();
        if (imageInfos.size() > 1) {
            ll_point.setVisibility(View.VISIBLE);
        } else {
            ll_point.setVisibility(View.INVISIBLE);
        }
        IntStream.range(0, imageInfos.size()).forEach(i -> {
            ImageView round = new ImageView(getContext());
            if (i == 0) {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResCurrent));
            } else {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
            }
            dotsView.add(round);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = DensityUtil.dp2px(context, 8f);
            ll_point.addView(round, params);
        });

    }

    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return imageInfos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @NotNull
        @Override
        public Object instantiateItem(@NotNull ViewGroup container, int position) {
            View view = View.inflate(context, R.layout.item_pic_show, null);
            PhotoView photoView = view.findViewById(R.id.pic_pv);
            if (canSaveImage) {
                photoView.setOnLongClickListener(view12 -> {
                    new ImageSaveDialog(context)
                            .setOnImageSaveListener(dialog -> {
                                dialog.dismiss();
                                if (imageInfos.get(position) instanceof String ||
                                        imageInfos.get(position) instanceof Integer ||
                                        imageInfos.get(position) instanceof Uri) {
                                    downloadImage(imageInfos.get(position));
                                } else {
                                    Toast.makeText(context, "图片缓存失败", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .build()
                            .show();
                    return false;
                });
            }

            Glide.with(context)
                    .asBitmap()
                    .load(imageInfos.get(position))
                    .apply(new RequestOptions().error(R.mipmap.ic_default_image))
                    .into(photoView);
            photoView.setOnPhotoTapListener((view1, x, y) -> dismiss());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void downloadImage(Object path) {
        ThreadExecutorBounded.getInstance().execute(new DownLoadImageService(context, path,
                "image", new ImageDownLoadCallBack() {
            @Override
            public void onDownLoadSuccess(File file) {
                Message message = new Message();
                message.obj = "图片已保存至" + file.getAbsolutePath();
                handler.sendMessage(message);
            }

            @Override
            public void onDownLoadFailed(String errorMsg) {
                Message message = new Message();
                message.obj = TextUtils.isEmpty(errorMsg) ? "图片保存失败" : errorMsg;
                handler.sendMessage(message);
            }
        }));
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Toast.makeText(context, message.obj == null ? "图片保存失败" : message.obj.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
    });
}

