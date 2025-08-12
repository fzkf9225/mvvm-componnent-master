package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.PictureAdapter;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.util.common.CommonUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.widget.gallery.PreviewPhotoDialog;

/**
 * Created by fz on 2024/12/18 10:57
 * describe：自定义banner轮播图
 */

public class BannerView<T extends BannerBean> extends ConstraintLayout {
    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    /**
     * 上一次索引位置
     */
    private int lastPos = -1;
    /**
     * 预览大图
     */
    private boolean previewLarger = true;
    /**
     * 自动轮播
     */
    private boolean autoLoop = true;
    /**
     * 底部圆点位置
     */
    private int dotPosition = DotPosition.INNER_BOTTOM_CENTER;
    /**
     * 错误时占位图
     */
    private @DrawableRes int placeholderImage;
    /**
     * banner数据
     */
    private List<T> bannerList;
    /**
     * 宽高
     */
    private float width, height;
    /**
     * 背景色
     */
    private int bgColor = Color.WHITE;
    /**
     * 各大圆角大小
     */
    private int leftTopRadius;
    private int rightTopRadius;
    private int rightBottomRadius;
    private int leftBottomRadius;
    /**
     * 自动轮播间隔时间
     */
    private int loopInterval = 3000;
    /**
     * 选中时圆点样式
     */
    private Drawable drawableResCurrent;
    /**
     * 未选中时圆点样式
     */
    private Drawable drawableResNormal;
    private final Path mPath = new Path();
    private Paint mPaint;
    private float dotHeight = 0, dotBottomMargin, dotLeftMargin, dotRightMargin, dotPadding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public final static class DotPosition {
        public final static int INNER_BOTTOM_CENTER = 0;
        public final static int INNER_BOTTOM_LEFT = 1;
        public final static int INNER_BOTTOM_RIGHT = 2;
        public final static int OUTER_CENTER = 3;
        public final static int OUTER_BOTTOM_LEFT = 4;
        public final static int OUTER_BOTTOM_RIGHT = 5;
    }

    public BannerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, context.obtainStyledAttributes(attrs, R.styleable.BannerView, 0, 0));
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, context.obtainStyledAttributes(attrs, R.styleable.BannerView, defStyleAttr, 0));
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, context.obtainStyledAttributes(attrs, R.styleable.BannerView, defStyleAttr, defStyleRes));
    }

    private void init(Context context, TypedArray ta) {
        mPaint = new Paint();
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        drawableResCurrent = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.white),
                DensityUtil.dp2px(context, 6));
        drawableResNormal = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.gray),
                DensityUtil.dp2px(context, 6));
        if (ta == null) {
            dotHeight = DensityUtil.dp2px(context, 30f);
            dotBottomMargin = DensityUtil.dp2px(context, 12f);
            dotLeftMargin = DensityUtil.dp2px(context, 12f);
            dotRightMargin = DensityUtil.dp2px(context, 12f);
            dotPadding = DensityUtil.dp2px(context, 8f);
            initLayout();
            return;
        }
        bgColor = ta.getColor(R.styleable.BannerView_bgColor, Color.WHITE);
        placeholderImage = ta.getResourceId(R.styleable.BannerView_bannerPlaceholderImage, R.mipmap.ic_default_image);
        previewLarger = ta.getBoolean(R.styleable.BannerView_previewLarger, false);
        autoLoop = ta.getBoolean(R.styleable.BannerView_autoLoop, true);
        loopInterval = ta.getInt(R.styleable.BannerView_loopInterval, 3000);
        dotPosition = ta.getInt(R.styleable.BannerView_dotPosition, DotPosition.INNER_BOTTOM_CENTER);

        leftTopRadius = ta.getDimensionPixelSize(R.styleable.BannerView_leftTopRadius, 0);
        rightTopRadius = ta.getDimensionPixelSize(R.styleable.BannerView_rightTopRadius, 0);
        rightBottomRadius = ta.getDimensionPixelSize(R.styleable.BannerView_rightBottomRadius, 0);
        leftBottomRadius = ta.getDimensionPixelSize(R.styleable.BannerView_leftBottomRadius, 0);

        dotHeight = ta.getDimension(R.styleable.BannerView_dotHeight, DensityUtil.dp2px(context, 30));
        dotBottomMargin = ta.getDimension(R.styleable.BannerView_dotBottomMargin, DensityUtil.dp2px(context, 12));
        dotLeftMargin = ta.getDimension(R.styleable.BannerView_dotLeftMargin, DensityUtil.dp2px(context, 12));
        dotRightMargin = ta.getDimension(R.styleable.BannerView_dotRightMargin, DensityUtil.dp2px(context, 12));
        dotPadding = ta.getDimension(R.styleable.BannerView_dotPadding, DensityUtil.dp2px(context, 8));

        drawableResCurrent = ta.getDrawable(R.styleable.BannerView_iconSelected);
        drawableResNormal = ta.getDrawable(R.styleable.BannerView_iconUnselected);
        if (drawableResCurrent == null) {
            drawableResCurrent = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.white),
                    DensityUtil.dp2px(context, 6));
        }
        if (drawableResNormal == null) {
            drawableResNormal = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.gray),
                    DensityUtil.dp2px(context, 6));
        }
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
        viewPager = new ViewPager2(getContext());
        viewPager.setId(View.generateViewId());
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
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

    private boolean dotIsInner() {
        return DotPosition.INNER_BOTTOM_CENTER == dotPosition || DotPosition.INNER_BOTTOM_LEFT == dotPosition || DotPosition.INNER_BOTTOM_RIGHT == dotPosition;
    }

    public void initView(@NotNull List<T> bannerList) {
        initView(bannerList, null);
    }

    public void initView(@NotNull List<T> bannerList, PictureAdapter.OnItemClickListener onItemClickListener) {
        this.bannerList = bannerList;
        if (this.bannerList.isEmpty()) {
            return;
        }
        initImageRounds();
        viewPager.setAdapter(new PictureAdapter<>(this.bannerList, placeholderImage)
                .setOnItemClickListener(onItemClickListener == null ? onDefaultImageItemClickListener : onItemClickListener));
        // 设置初始位置到中间，Viewpager2不支持无线循环，所以设置他的最大页为Integer.MAX_VALUE实现
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false);
        if (autoLoop) {
            startLoop();
        }
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public void setRoundDots(Drawable drawableResCurrent, Drawable drawableResNormal) {
        this.drawableResCurrent = drawableResCurrent;
        this.drawableResNormal = drawableResNormal;
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private void initImageRounds() {
        dotsLayout.removeAllViews();
        /*
         *当轮播图大于1张时小圆点显示
         */
        if (this.bannerList.size() > 1) {
            dotsLayout.setVisibility(View.VISIBLE);
        } else {
            dotsLayout.setVisibility(View.INVISIBLE);
        }
        lastPos = 0;
        /*
         * 默认让第一张图片显示深颜色的圆点
         */
        IntStream.range(0, this.bannerList.size()).forEach(i -> {
            ImageView round = new ImageView(getContext());
            if (i == 0) {
                round.setBackground(drawableResCurrent);
            } else {
                round.setBackground(drawableResNormal);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = (int) dotPadding;
            dotsLayout.addView(round, params);
        });
    }

    public void startLoop() {
        //防止重复调用启动轮播，这样会越来越快
        handler.removeCallbacks(loopRunnable);
        handler.postDelayed(loopRunnable, loopInterval);
    }

    public void stopLoop() {
        handler.removeCallbacks(loopRunnable);
    }

    protected Runnable loopRunnable = new Runnable() {

        @Override
        public void run() {
            if (autoLoop && viewPager != null) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                handler.postDelayed(this, loopInterval);
            }
        }
    };

    /**
     * 默认的点击事件
     */
    protected final PictureAdapter.OnItemClickListener onDefaultImageItemClickListener = position -> {
        if (!StringUtil.isEmpty(bannerList.get(position).getLinkUrl())) {
            //排除默认的“#”号空链接
            if ("#".equals(bannerList.get(position).getLinkUrl())) {
                return;
            }
            CommonUtil.toBrowser(getContext(), bannerList.get(position).getLinkUrl(), bannerList.get(position).isLinkInside());
            return;
        }
        if (previewLarger) {
            List<Object> list = new ArrayList<>();
            this.bannerList.forEach(item -> list.add(item.getBannerUrl()));
            new PreviewPhotoDialog(getContext(), list, position).show();
        }
    };
    /**
     * 监听滑动实现底部点的显示
     */
    protected final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            int realPos = position % bannerList.size();
            dotsLayout.getChildAt(realPos).setBackground(drawableResCurrent);
            if (lastPos >= 0 && lastPos < dotsLayout.getChildCount() && lastPos != realPos) {
                dotsLayout.getChildAt(lastPos).setBackground(drawableResNormal);
            }
            lastPos = realPos;
        }
    };

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
    }
}
