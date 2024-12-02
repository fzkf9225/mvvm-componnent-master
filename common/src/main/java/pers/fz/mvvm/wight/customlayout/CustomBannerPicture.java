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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.PictureAdapter;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.util.common.CommonUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.wight.picdialog.PicShowDialog;

/**
 * Created by fz on 2018/3/15.
 * 自定义banner轮播图
 */

public class CustomBannerPicture extends ConstraintLayout implements View.OnClickListener, BannerViewPager.OnImageItemClickListener {
    private final List<ImageView> images = new ArrayList<>();
    private BannerViewPager viewPager;
    private LinearLayout dotsLayout;
    private boolean canBrowse = false, autoBanner = true, canDownload = true;
    private int dotPosition = DotPosition.INNER_BOTTOM_CENTER;
    private @DrawableRes int placeholderImage;
    private final List<Object> imageInfosList = new ArrayList<>();
    private int mCurrentPosition = 0;
    private List<BannerBean> bannerList;
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
    private float dotHeight = 0, dotBottomMargin, dotLeftMargin, dotRightMargin, dotPadding;

    public final static class DotPosition {
        public final static int INNER_BOTTOM_CENTER = 0;
        public final static int INNER_BOTTOM_LEFT = 1;
        public final static int INNER_BOTTOM_RIGHT = 2;
        public final static int OUTER_CENTER = 3;
        public final static int OUTER_BOTTOM_LEFT = 4;
        public final static int OUTER_BOTTOM_RIGHT = 5;

    }

    public CustomBannerPicture(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomBannerPicture(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        if (attrs == null) {
            dotHeight = DensityUtil.dp2px(context, 30f);
            dotBottomMargin = DensityUtil.dp2px(context, 12f);
            dotLeftMargin = DensityUtil.dp2px(context, 12f);
            dotRightMargin = DensityUtil.dp2px(context, 12f);
            dotPadding = DensityUtil.dp2px(context, 8f);
            initLayout();
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.banner);
        bgColor = ta.getColor(R.styleable.banner_banner_bg_color, Color.WHITE);
        placeholderImage = ta.getResourceId(R.styleable.banner_banner_placeholder_image, R.mipmap.ic_default_image);
        canBrowse = ta.getBoolean(R.styleable.banner_can_browse, false);
        autoBanner = ta.getBoolean(R.styleable.banner_auto_banner, false);
        canDownload = ta.getBoolean(R.styleable.banner_can_download, false);

        dotPosition = ta.getInt(R.styleable.banner_dot_position, DotPosition.INNER_BOTTOM_CENTER);

        leftTopRadius = ta.getDimensionPixelSize(R.styleable.banner_banner_left_top_radius, 0);
        rightTopRadius = ta.getDimensionPixelSize(R.styleable.banner_banner_right_top_radius, 0);
        rightBottomRadius = ta.getDimensionPixelSize(R.styleable.banner_banner_right_bottom_radius, 0);
        leftBottomRadius = ta.getDimensionPixelSize(R.styleable.banner_banner_left_bottom_radius, 0);

        dotHeight = ta.getDimension(R.styleable.banner_dot_height, DensityUtil.dp2px(context, 30));
        dotBottomMargin = ta.getDimension(R.styleable.banner_dot_bottom_margin, DensityUtil.dp2px(context, 12));
        dotLeftMargin = ta.getDimension(R.styleable.banner_dot_left_margin, DensityUtil.dp2px(context, 12));
        dotRightMargin = ta.getDimension(R.styleable.banner_dot_right_margin, DensityUtil.dp2px(context, 12));
        dotPadding = ta.getDimension(R.styleable.banner_dot_padding, DensityUtil.dp2px(context, 8));

        drawableResCurrent = ta.getResourceId(R.styleable.banner_icon_selected, R.mipmap.icon_point2);
        drawableResNormal = ta.getResourceId(R.styleable.banner_icon_unselected, R.mipmap.icon_point1);
        mPaint.setColor(bgColor);
        ta.recycle();
        initLayout();
    }

    private void initLayout() {
        setLayoutParams(new Constraints.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));
        //初始化ViewPager
        viewPager = new BannerViewPager(getContext(), autoBanner, drawableResCurrent, drawableResNormal);
        viewPager.setId(View.generateViewId());
        viewPager.setOnImageItemClickListener(onImageItemClickListener);
        ConstraintLayout.LayoutParams viewPagerLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT);

        //初始化指针
        dotsLayout = new LinearLayout(getContext());
        dotsLayout.setId(View.generateViewId());
        dotsLayout.setVerticalGravity(Gravity.BOTTOM);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        ConstraintLayout.LayoutParams dotLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                (int) dotHeight
        );
        dotLayoutParams.leftMargin = (int) dotLeftMargin;
        dotLayoutParams.rightMargin = (int) dotRightMargin;
        dotLayoutParams.bottomMargin = (int) dotBottomMargin;

        //viewPager的定位
        viewPagerLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        viewPagerLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        viewPagerLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        if (dotIsInner()) {
            viewPagerLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            //dotsLayout定位
            dotLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            if (DotPosition.INNER_BOTTOM_LEFT == dotPosition) {
                dotLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            } else if (DotPosition.INNER_BOTTOM_RIGHT == dotPosition) {
                dotLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            } else {
                dotLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                dotLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            }
        } else {
            viewPagerLayoutParams.height = 0;
            viewPagerLayoutParams.verticalWeight = 1;
            viewPagerLayoutParams.bottomToTop = dotsLayout.getId();
            //dotsLayout定位
            dotLayoutParams.topToBottom = viewPager.getId();
            dotLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            if (DotPosition.OUTER_BOTTOM_LEFT == dotPosition) {
                dotLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            } else if (DotPosition.OUTER_BOTTOM_RIGHT == dotPosition) {
                dotLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            } else {
                dotLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                dotLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            }
        }
        //设置指针和ViewPager定位
        removeAllViews();
        addView(viewPager, viewPagerLayoutParams);
        addView(dotsLayout, dotLayoutParams);
        setBackgroundColor(bgColor);
    }

    public void setOnImageItemClickListener(BannerViewPager.OnImageItemClickListener onImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener;
    }

    private boolean dotIsInner() {
        return DotPosition.INNER_BOTTOM_CENTER == dotPosition || DotPosition.INNER_BOTTOM_LEFT == dotPosition || DotPosition.INNER_BOTTOM_RIGHT == dotPosition;
    }

    public void initView(@NotNull List<BannerBean> bannerList) {
        this.bannerList = bannerList;
        images.clear();
        for (BannerBean bannerBean : this.bannerList) {
            Object imgPath = bannerBean.getPath();
            if (dotIsInner()) {
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setOnClickListener(this);
                Glide.with(getContext())
                        .load(imgPath)
                        .apply(new RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                        .into(imageView);
                images.add(imageView);
            } else {
                CornerImageView imageView = new CornerImageView(getContext());
                imageView.setLeftBottomRadius(leftBottomRadius);
                imageView.setLeftTopRadius(leftTopRadius);
                imageView.setRightTopRadius(rightTopRadius);
                imageView.setRightBottomRadius(rightBottomRadius);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setOnClickListener(this);
                Glide.with(getContext())
                        .load(imgPath)
                        .apply(new RequestOptions().placeholder(placeholderImage).error(placeholderImage))
                        .into(imageView);
                images.add(imageView);
            }
            imageInfosList.add(imgPath);
        }
        viewPager.setImages(images);
        initImageRounds();
        viewPager.setAdapter(new PictureAdapter(images));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
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
        /*
         * 默认让第一张图片显示深颜色的圆点
         */
        IntStream.range(0, images.size()).forEach(i -> {
            ImageView round = new ImageView(getContext());
            if (i == 0) {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResCurrent));
            } else {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
            }
            dots.add(round);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = (int) dotPadding;
            dotsLayout.addView(round, params);
        });
        viewPager.setDots(dots);
    }

    @Override
    public void onClick(View v) {
        if (imageInfosList == null || imageInfosList.isEmpty()) {
            return;
        }
        if (!StringUtil.isEmpty(bannerList.get(mCurrentPosition).getLinkPath())) {
            //排除默认的“#”号空链接
            if ("#".equals(bannerList.get(mCurrentPosition).getLinkPath())) {
                return;
            }
            CommonUtil.toBrowser(getContext(), bannerList.get(mCurrentPosition).getLinkPath(), bannerList.get(mCurrentPosition).isLinkInside());
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!dotIsInner()) {
            super.onDraw(canvas);
            return;
        }
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
            canvas.drawPath(mPath, mPaint);
        }
        super.onDraw(canvas);
    }
}
