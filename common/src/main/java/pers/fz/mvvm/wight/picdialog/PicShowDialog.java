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
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.ThreadExecutorArray;
import pers.fz.mvvm.util.download.DownLoadImageService;
import pers.fz.mvvm.util.download.ImageDownLoadCallBack;
import pers.fz.mvvm.util.log.ToastUtils;
import pers.fz.mvvm.wight.dialog.ImageSaveDialog;
import pers.fz.mvvm.wight.picdialog.bean.ImageInfo;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面点击发布，弹出半透明对话框
 */
public class PicShowDialog extends Dialog {
    private Context context;
    private View view;
    private List<ImageInfo> imageInfos;
    private MyViewPager vp;
    private List<View> views = new ArrayList<View>();
    private LayoutAnimationController lac;
    private LinearLayout ll_point;
    private ViewPagerAdapter pageAdapter;
    private int position;
    private boolean canSaveImage = true;
    private LinearLayout.LayoutParams paramsL = new LinearLayout.LayoutParams(10, 10);
    // 图片缓存 默认 等
    private DisplayImageOptions optionsImag = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.mipmap.ic_default_image)
            .showImageOnFail(R.mipmap.ic_default_image).cacheInMemory(true).cacheOnDisk(true)
            .considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565).build();

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

    public PicShowDialog(Context context, List<ImageInfo> imageInfos, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        this.position = position;
    }

    public PicShowDialog(Context context, List<ImageInfo> imageInfos, boolean canSaveImage, int position) {
        this(context, R.style.Pic_Dialog);
        this.imageInfos = imageInfos;
        this.canSaveImage = canSaveImage;
        this.position = position;
    }

    public PicShowDialog setImages(List<ImageInfo> imageInfoList) {

        return this;
    }

    public static List<ImageInfo> createImageInfo(String image) {
        List<ImageInfo> imageInfos = new ArrayList<>();
        imageInfos.add(new ImageInfo(image, 600, 800));
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(@DrawableRes int imageRes) {
        List<ImageInfo> imageInfos = new ArrayList<>();
        imageInfos.add(new ImageInfo(imageRes, 600, 800));
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(Bitmap bitmap) {
        List<ImageInfo> imageInfos = new ArrayList<>();
        imageInfos.add(new ImageInfo(bitmap, 600, 800));
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(String... image) {
        if (image == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        for (String img : image) {
            imageInfos.add(new ImageInfo(img, 600, 800));
        }
        return imageInfos;
    }

    public static List<ImageInfo> createUriImageInfo(Uri... uri) {
        if (uri == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        for (Uri img : uri) {
            imageInfos.add(new ImageInfo(img, 600, 800));
        }
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(List<String> images) {
        if (images == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        for (String img : images) {
            imageInfos.add(new ImageInfo(img, 600, 800));
        }
        return imageInfos;
    }

    public static List<ImageInfo> createUriImageInfo(List<Uri> images) {
        if (images == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        for (Uri img : images) {
            imageInfos.add(new ImageInfo(img, 600, 800));
        }
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(String image, int width, int height) {
        if (image == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        imageInfos.add(new ImageInfo(image, width, height));
        return imageInfos;
    }

    public static List<ImageInfo> createImageInfo(int width, int height, String... image) {
        if (image == null) {
            return null;
        }
        List<ImageInfo> imageInfos = new ArrayList<>();
        for (String img : image) {
            imageInfos.add(new ImageInfo(img, width, height));
        }
        return imageInfos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dialog_pic);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        getWindow().setLayout(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        vp = (MyViewPager) findViewById(R.id.vp);
        ll_point = (LinearLayout) findViewById(R.id.ll_point);
//        init();
        initMyPageAdapter();
//        vp.setAdapter(new ViewPagerAdapter());
        vp.setCurrentItem(position);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (views.size() != 0 && views.get(position) != null) {

                    for (int i = 0; i < views.size(); i++) {
                        if (i == position) {
                            views.get(i).setBackgroundResource(R.mipmap.icon_point2);
                        } else {
                            views.get(i).setBackgroundResource(R.mipmap.icon_point1);
                        }
                    }

                }
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
            if (vp != null) {
                vp.setAdapter(pageAdapter);
            }

        } else {
            pageAdapter.notifyDataSetChanged();
        }
    }

    private void initPoint() {
        views.clear();
        ll_point.removeAllViews();
        if (imageInfos.size() == 1) {
            ll_point.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < imageInfos.size(); i++) {
                View view = new View(context);
                paramsL.setMargins(dip2px(context, 5), dip2px(context, 2), 0, dip2px(context, 5));
                view.setLayoutParams(paramsL);
                if (i == position) {
                    view.setBackgroundResource(R.mipmap.icon_point2);
                } else {
                    view.setBackgroundResource(R.mipmap.icon_point1);
                }
                views.add(view);
                ll_point.addView(view);
            }
        }

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
                                if (imageInfos.get(position).getUrl() instanceof String ||
                                        imageInfos.get(position).getUrl() instanceof Integer ||
                                        imageInfos.get(position).getUrl() instanceof Uri) {
                                    downloadImage(imageInfos.get(position).getUrl());
                                } else {
                                    ToastUtils.showShort(context, "图片缓存失败");
                                }
                            })
                            .build()
                            .show();
                    return false;
                });
            }

            Glide.with(context)
                    .asBitmap()
                    .load(imageInfos.get(position).getUrl())
                    .apply(new RequestOptions().error(R.mipmap.ic_default_image))
                    .into(photoView);
//            if (imageInfos.get(position).getUrl() instanceof String) {
//                ImageLoader imageLoader = ImageLoader.getInstance();
//                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
//                imageLoader.displayImage((String) imageInfos.get(position).getUrl(), photoView, optionsImag);
//            } else if (imageInfos.get(position).getUrl() instanceof Integer) {
//                Glide.with(context).asBitmap().load(imageInfos.get(position).getUrl())
//                        .apply(new RequestOptions().error(R.mipmap.ic_default_image)).into(photoView);
//            } else {
//                Glide.with(context).asBitmap().load(R.mipmap.ic_default_image).into(photoView);
//            }
            photoView.setOnPhotoTapListener((view1, x, y) -> dismiss());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void downloadImage(Object path) {
        ThreadExecutorArray.getInstance().execute(new DownLoadImageService(context, path,
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

    private final Handler handler = new Handler(Looper.myLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            ToastUtils.showShort(context, message.obj == null ? "图片保存失败" : message.obj.toString());
            return false;
        }
    });

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}

