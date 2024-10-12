package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.WebViewActivity;
import pers.fz.mvvm.adapter.PictureAdapter;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.databinding.BannerCornerImageViewBinding;
import pers.fz.mvvm.databinding.CustomBannerPictureBinding;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;
import pers.fz.mvvm.wight.picdialog.bean.ImageInfo;

/**
 * Created by fz on 2018/3/15.
 * 自定义banner轮播图
 */

public class CustomBannerPicture extends ConstraintLayout implements View.OnClickListener, BannerViewPager.OnImageItemClickListener {
    private List<ImageView> images = new ArrayList<>();
    private CustomBannerPictureBinding binding;
    private boolean canBrowse = false, autoBanner = true, canDownload = true;
    private int dotPosition = 0;
    private List<ImageInfo> imageInfosList = new ArrayList<>();
    private int mCurrentPosition = 0;
    private List<BannerBean> imgPic;
    private float width, height;
    private int bgColor = Color.WHITE;
    private int leftTopRadius;
    private int rightTopRadius;
    private int rightBottomRadius;
    private int leftBottomRadius;
    private BannerViewPager.OnImageItemClickListener onImageItemClickListener = this;
    private @DrawableRes
    int drawableResCurrent = R.mipmap.icon_point2;
    private @DrawableRes
    int drawableResNormal = R.mipmap.icon_point1;
    private final Path mPath = new Path();
    private Paint mPaint;

    public CustomBannerPicture(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomBannerPicture(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CustomBannerPictureBinding.inflate(LayoutInflater.from(context), this, true);
        setLayoutParams(new Constraints.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));
        binding.bannerViewPager.setOpen(autoBanner);
        binding.bannerViewPager.setDrawableResCurrent(drawableResCurrent);
        binding.bannerViewPager.setDrawableResNormal(drawableResNormal);
        binding.bannerViewPager.setOnImageItemClickListener(onImageItemClickListener);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        setBackgroundColor(bgColor);
        if (attrs == null) {
            canBrowse = false;
            autoBanner = false;
            canDownload = false;
            dotPosition = 0;
            leftTopRadius = 0;
            leftBottomRadius = 0;
            rightBottomRadius = 0;
            rightTopRadius = 0;
            drawableResCurrent = R.mipmap.icon_point2;
            drawableResNormal = R.mipmap.icon_point1;
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.banner);
        bgColor = ta.getColor(R.styleable.banner_banner_bg_color, Color.WHITE);
        canBrowse = ta.getBoolean(R.styleable.banner_can_browse, false);
        autoBanner = ta.getBoolean(R.styleable.banner_auto_banner, false);
        canDownload = ta.getBoolean(R.styleable.banner_can_download, false);
        dotPosition = ta.getInt(R.styleable.banner_dot_position, 0);
        leftTopRadius = ta.getDimensionPixelOffset(R.styleable.banner_banner_left_top_radius, 0);
        rightTopRadius = ta.getDimensionPixelOffset(R.styleable.banner_banner_right_top_radius, 0);
        rightBottomRadius = ta.getDimensionPixelOffset(R.styleable.banner_banner_right_bottom_radius, 0);
        leftBottomRadius = ta.getDimensionPixelOffset(R.styleable.banner_banner_left_bottom_radius, 0);
        drawableResCurrent = ta.getResourceId(R.styleable.banner_icon_selected, R.mipmap.icon_point2);
        drawableResNormal = ta.getResourceId(R.styleable.banner_icon_unselected, R.mipmap.icon_point1);
        ta.recycle();
        setBackgroundColor(bgColor);
    }

    public void setOnImageItemClickListener(BannerViewPager.OnImageItemClickListener onImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener;
    }

    public void initView(List<BannerBean> imgPic) {
        this.imgPic = imgPic;
        if (!imgPic.isEmpty()) {
            imgPic.size();
            if (imgPic.size() == 1) {
                getImages(imgPic.get(0).getPath());
                imageInfosList.add(new ImageInfo(imgPic.get(0).getPath(), 1920, 1080));
            } else {
                images.clear();
                for (int k = 0; k < imgPic.size(); k++) {
                    Object imgPath = imgPic.get(k).getPath();
                    imageInfosList.add(new ImageInfo(imgPath, 1920, 1080));
                    getImages(imgPath);
                }
            }
            addHeader();
        }
    }

    /**
     * 创建ViewPager的子item项
     */
    private void getImages(Object oldPath) {
        BannerCornerImageViewBinding binding = BannerCornerImageViewBinding.inflate(LayoutInflater.from(getContext()));
        binding.cornerImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        binding.cornerImage.setOnClickListener(this);
        Glide.with(getContext())
                .load(oldPath)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(binding.cornerImage);
        images.add(binding.cornerImage);
    }

    public ViewPager getViewPager() {
        return binding.bannerViewPager;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * 填充布局
     */
    private void addHeader() {
        binding.bannerViewPager.setImages(images);
        initImageRounds();
        binding.bannerViewPager.setAdapter(new PictureAdapter(images));
        binding.bannerViewPager.setCurrentItem(Integer.MAX_VALUE / 2);
    }

    public void setRoundDots(@DrawableRes int drawableResCurrent, @DrawableRes int drawableResNormal) {
        this.drawableResCurrent = drawableResCurrent;
        this.drawableResNormal = drawableResNormal;
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private void initImageRounds() {
        List<ImageView> dots = new ArrayList<>();
        binding.llDots.removeAllViews();
        /*
         *当轮播图大于1张时小圆点显示
         */
        if (images.size() > 1) {
            binding.llDots.setVisibility(View.VISIBLE);
        } else {
            binding.llDots.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < images.size(); i++) {
            ImageView round = new ImageView(getContext());
            /*
             * 默认让第一张图片显示深颜色的圆点
             */
            if (i == 0) {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResCurrent));
            } else {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
            }
            dots.add(round);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = 20;
            binding.llDots.addView(round, params);
        }
        binding.bannerViewPager.setDots(dots);
    }

    @Override
    public void onClick(View v) {
        if (imageInfosList == null || imageInfosList.isEmpty()) {
            return;
        }
        if (!StringUtil.isEmpty(imgPic.get(mCurrentPosition).getLinkPath())) {
            //排除默认的“#”号空链接
            if ("#".equals(imgPic.get(mCurrentPosition).getLinkPath())) {
                return;
            }
            WebViewActivity.show(getContext(), imgPic.get(mCurrentPosition).getLinkPath(), null);
            return;
        }
        if (canBrowse) {
            new PicShowDialog(getContext(), imageInfosList, mCurrentPosition).show();
        }
    }

    @Override
    public void onItemClick(int itemPosition) {

    }

    @Override
    public void getPosition(int itemPosition) {
        binding.llDots.requestLayout();
        mCurrentPosition = itemPosition;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
        int maxRight = Math.max(rightTopRadius, rightBottomRadius);
        int minWidth = maxLeft + maxRight;
        int maxTop = Math.max(leftTopRadius, rightTopRadius);
        int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
        int minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            mPath.reset();
            //四个角：右上，右下，左下，左上
            mPath.moveTo(leftTopRadius, 0);
            mPath.lineTo(width - rightTopRadius, 0);
            mPath.quadTo(width, 0, width, rightTopRadius);

            mPath.lineTo(width, height - rightBottomRadius);
            mPath.quadTo(width, height, width - rightBottomRadius, height);

            mPath.lineTo(leftBottomRadius, height);
            mPath.quadTo(0, height, 0, height - leftBottomRadius);

            mPath.lineTo(0, leftTopRadius);
            mPath.quadTo(0, 0, leftTopRadius, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                canvas.clipPath(mPath);
            } else {
                canvas.clipPath(mPath, Region.Op.INTERSECT);
            }
            canvas.drawPath(mPath,mPaint);
        }
        super.onDraw(canvas);
    }
}
