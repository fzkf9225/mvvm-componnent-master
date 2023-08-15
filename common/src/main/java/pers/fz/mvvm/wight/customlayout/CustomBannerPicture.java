package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.WebViewActivity;
import pers.fz.mvvm.adapter.PictureAdapter;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.databinding.BannerCornerImageViewBinding;
import pers.fz.mvvm.util.apiUtil.DensityUtil;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.wight.picDialog.PicShowDialog;
import pers.fz.mvvm.wight.picDialog.bean.ImageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2018/3/15.
 * 自定义banner轮播图
 */

public class CustomBannerPicture extends RelativeLayout implements View.OnClickListener, BannerViewPager.OnImageItemClickListener {
    private ViewGroup.LayoutParams matchParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private List<ImageView> images = new ArrayList<>();
    private BannerViewPager viewPager;
    private LinearLayout dotsLayout;
    private boolean canBrowse = false, autoBanner = true, canDownload = true;
    private int dotPosition = 0;
    private List<ImageInfo> imageInfosList = new ArrayList<>();
    private int mCurrentPosition = 0;
    private List<BannerBean> imgPic;
    private float width, height;
    private float radius = 8;
    private BannerViewPager.OnImageItemClickListener onImageItemClickListener;
    private @DrawableRes
    int drawableResCurrent = R.mipmap.icon_point2;
    private @DrawableRes
    int drawableResNormal = R.mipmap.icon_point1;

    public CustomBannerPicture(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.banner);
        canBrowse = ta.getBoolean(R.styleable.banner_can_browse, false);
        autoBanner = ta.getBoolean(R.styleable.banner_auto_banner, false);
        canDownload = ta.getBoolean(R.styleable.banner_can_download, false);
        dotPosition = ta.getInt(R.styleable.banner_dot_position, 0);
        radius = ta.getDimension(R.styleable.banner_banner_radius, 0);
        drawableResCurrent = ta.getResourceId(R.styleable.banner_icon_selected,R.mipmap.icon_point2);
        drawableResNormal = ta.getResourceId(R.styleable.banner_icon_unselected,R.mipmap.icon_point1);
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, R.dimen.x392));
    }

    public void setOnImageItemClickListener(BannerViewPager.OnImageItemClickListener onImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener;
    }

    public void initView(List<BannerBean> imgPic) {
        this.imgPic = imgPic;
        if (!imgPic.isEmpty()) {
            imgPic.size();
            if (imgPic.size() == 1) {
                BannerCornerImageViewBinding binding = BannerCornerImageViewBinding.inflate(LayoutInflater.from(getContext()));
                binding.cornerImage.setLayoutParams(matchParams);
                binding.cornerImage.setOnClickListener(this);
                Object imgPath = imgPic.get(0).getPath();
                Glide.with(getContext())
                        .load(imgPath)
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                        .into(binding.cornerImage);
                imageInfosList.add(new ImageInfo(imgPath, 1920, 1080));
                addView(binding.cornerImage);
                binding.cornerImage.setOnClickListener(this);
            } else {
                images.clear();
                for (int k = 0; k < imgPic.size(); k++) {
                    Object imgPath = imgPic.get(k).getPath();
                    imageInfosList.add(new ImageInfo(imgPath, 1920, 1080));
                    getImages(imgPath);
                }
                addHeader();
            }
        }
    }

    /**
     * 创建ViewPager的子item项
     */
    private void getImages(Object oldPath) {
        BannerCornerImageViewBinding binding = BannerCornerImageViewBinding.inflate(LayoutInflater.from(getContext()));
        binding.cornerImage.setLayoutParams(matchParams);
        binding.cornerImage.setOnClickListener(this);
        Glide.with(getContext())
                .load(oldPath)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(binding.cornerImage);
        images.add(binding.cornerImage);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * 填充布局
     */
    private void addHeader() {
        viewPager = new BannerViewPager(getContext(), autoBanner, drawableResCurrent, drawableResNormal);
        viewPager.setId(View.generateViewId());
        viewPager.setImages(images);
        viewPager.setOnImageItemClickListener(onImageItemClickListener);
        ConstraintLayout constraintLayout = new ConstraintLayout(getContext());
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));

        dotsLayout = new LinearLayout(getContext());
        dotsLayout.setId(View.generateViewId());
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        removeAllViews();
        initImageRounds();
        viewPager.setAdapter(new PictureAdapter(images));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        constraintLayout.addView(viewPager);
        constraintLayout.addView(dotsLayout);
        // 创建约束规则
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        // 设置 ImageView 充满 ConstraintLayout
        constraintSet.connect(
                viewPager.getId(),
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
        );
        constraintSet.connect(
                viewPager.getId(),
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
        );
        constraintSet.connect(
                viewPager.getId(),
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
        );

        constraintSet.connect(
                viewPager.getId(),
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
        );
        constraintSet.connect(
                dotsLayout.getId(),
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                DensityUtil.dp2px(getContext(), 8)
        );
        constraintSet.connect(
                dotsLayout.getId(),
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
        );
        constraintSet.connect(
                dotsLayout.getId(),
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
        );

        // 应用约束规则
        constraintSet.applyTo(constraintLayout);
        addView(constraintLayout, matchParams);
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
        dotsLayout.removeAllViews();
        /*
         *当轮播图大于1张时小圆点显示
         */
        if (images.size() > 1) {
            dotsLayout.setVisibility(View.VISIBLE);
        } else {
            dotsLayout.setVisibility(View.INVISIBLE);
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
            dotsLayout.addView(round, params);
        }
        viewPager.setDots(dots);
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
        dotsLayout.requestLayout();
        mCurrentPosition = itemPosition;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = widthMeasureSpec;
        height = heightMeasureSpec;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        float minWidth = radius;
        float maxTop = radius;
        float maxBottom = radius;
        float minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            Path path = new Path();
            //四个角：右上，右下，左下，左上
            path.moveTo(radius, 0);
            path.lineTo(width - radius, 0);
            path.quadTo(width, 0, width, radius);

            path.lineTo(width, height - radius);
            path.quadTo(width, height, width - radius, height);

            path.lineTo(radius, height);
            path.quadTo(0, height, 0, height - radius);

            path.lineTo(0, radius);
            path.quadTo(0, 0, radius, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                canvas.clipPath(path);
            } else {
                canvas.clipPath(path, Region.Op.XOR);
            }
        }

        super.onDraw(canvas);
    }
}
