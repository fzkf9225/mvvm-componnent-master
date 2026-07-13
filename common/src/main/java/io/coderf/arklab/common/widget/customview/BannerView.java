package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.activity.WebViewActivity;
import io.coderf.arklab.common.adapter.PictureAdapter;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.enums.AttachmentTypeEnum;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.common.StringUtil;
import io.coderf.arklab.common.widget.customview.inter.IBannerItem;
import io.coderf.arklab.common.widget.gallery.PreviewPhotoDialog;

/**
 * 自定义 Banner 轮播：支持圆点/文字指示器、自动轮播、链接跳转与大图预览。
 * 数据项实现 {@link IBannerItem}；可通过 {@link #setOnBannerItemClickListener} 覆盖默认点击行为。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/12/18 10:57
 */
public class BannerView<T extends IBannerItem> extends ConstraintLayout {
    protected ViewPager2 viewPager;
    /** 底部指示器容器（圆点 / 文字页码共用） */
    protected LinearLayout indicatorLayout;
    /**
     * 上一次索引位置
     */
    protected int lastPos = 0;
    /**
     * 预览大图
     */
    protected boolean previewLarger = true;
    /**
     * 自动轮播
     */
    protected boolean autoLoop = true;
    /**
     * 底部指示器位置
     */
    protected int indicatorPosition = IndicatorPosition.INNER_BOTTOM_CENTER;
    /**
     * 指示器样式
     */
    protected int indicatorStyle = IndicatorStyle.DOT;
    /**
     * 错误时占位图
     */
    protected @DrawableRes int placeholderImage;
    /**
     * banner数据
     */
    protected List<T> bannerList;
    /**
     * 宽高
     */
    protected float width, height;
    /**
     * 背景色
     */
    protected int bgColor = Color.TRANSPARENT;
    /**
     * 各大圆角大小
     */
    protected int leftTopRadius;
    protected int rightTopRadius;
    protected int rightBottomRadius;
    protected int leftBottomRadius;
    /**
     * 自动轮播间隔时间
     */
    protected int loopInterval = 3000;
    /**
     * 选中时圆点样式
     */
    protected Drawable drawableResCurrent;
    /**
     * 未选中时圆点样式
     */
    protected Drawable drawableResNormal;
    protected final Path mPath = new Path();
    protected Paint mPaint;
    protected float indicatorHeight = 0;
    protected float indicatorBottomMargin;
    protected float indicatorLeftMargin;
    protected float indicatorRightMargin;
    protected float dotPadding;
    protected float indicatorTextSize;
    protected int indicatorCurrentTextColor;
    protected int indicatorTotalTextColor;
    protected int indicatorSeparatorTextColor;
    protected String indicatorSeparatorText = "/";
    /** 文字指示器子 View，动态创建 */
    @Nullable
    protected TextView tvCurrentPage;
    @Nullable
    protected TextView tvSeparator;
    @Nullable
    protected TextView tvTotalPage;
    protected final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    protected OnBannerItemClickListener<T> onBannerItemClickListener;
    @Nullable
    protected OnBannerPreviewListener<T> onBannerPreviewListener;

    public BannerView<T> setOnBannerItemClickListener(@Nullable OnBannerItemClickListener<T> listener) {
        this.onBannerItemClickListener = listener;
        return this;
    }

    public BannerView<T> setOnBannerPreviewListener(@Nullable OnBannerPreviewListener<T> listener) {
        this.onBannerPreviewListener = listener;
        return this;
    }

    public final static class IndicatorPosition {
        public final static int INNER_BOTTOM_CENTER = 0;
        public final static int INNER_BOTTOM_LEFT = 1;
        public final static int INNER_BOTTOM_RIGHT = 2;
        public final static int OUTER_CENTER = 3;
        public final static int OUTER_BOTTOM_LEFT = 4;
        public final static int OUTER_BOTTOM_RIGHT = 5;
    }

    /** @deprecated 使用 {@link IndicatorPosition} */
    @Deprecated
    public final static class DotPosition {
        public final static int INNER_BOTTOM_CENTER = IndicatorPosition.INNER_BOTTOM_CENTER;
        public final static int INNER_BOTTOM_LEFT = IndicatorPosition.INNER_BOTTOM_LEFT;
        public final static int INNER_BOTTOM_RIGHT = IndicatorPosition.INNER_BOTTOM_RIGHT;
        public final static int OUTER_CENTER = IndicatorPosition.OUTER_CENTER;
        public final static int OUTER_BOTTOM_LEFT = IndicatorPosition.OUTER_BOTTOM_LEFT;
        public final static int OUTER_BOTTOM_RIGHT = IndicatorPosition.OUTER_BOTTOM_RIGHT;
    }

    public final static class IndicatorStyle {
        public final static int DOT = 0;
        public final static int TEXT = 1;
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
        indicatorCurrentTextColor = ContextCompat.getColor(context, R.color.white);
        indicatorTotalTextColor = ContextCompat.getColor(context, R.color.white);
        indicatorSeparatorTextColor = indicatorTotalTextColor;
        drawableResCurrent = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.white),
                DensityUtil.dp2px(context, 6));
        drawableResNormal = DrawableUtil.createCircleDrawable(ContextCompat.getColor(context, R.color.gray),
                DensityUtil.dp2px(context, 6));
        if (ta == null) {
            indicatorHeight = DensityUtil.dp2px(context, 30f);
            indicatorBottomMargin = DensityUtil.dp2px(context, 12f);
            indicatorLeftMargin = DensityUtil.dp2px(context, 12f);
            indicatorRightMargin = DensityUtil.dp2px(context, 12f);
            dotPadding = DensityUtil.dp2px(context, 8f);
            indicatorTextSize = DensityUtil.sp2px(context, 14f);
            initLayout();
            return;
        }
        bgColor = ta.getColor(R.styleable.BannerView_bgColor, Color.TRANSPARENT);
        placeholderImage = ta.getResourceId(R.styleable.BannerView_bannerPlaceholderImage, R.mipmap.ic_default_image);
        previewLarger = ta.getBoolean(R.styleable.BannerView_previewLarger, false);
        autoLoop = ta.getBoolean(R.styleable.BannerView_autoLoop, true);
        loopInterval = ta.getInt(R.styleable.BannerView_loopInterval, 3000);
        indicatorPosition = ta.getInt(R.styleable.BannerView_dotPosition, IndicatorPosition.INNER_BOTTOM_CENTER);
        indicatorStyle = ta.getInt(R.styleable.BannerView_indicatorStyle, IndicatorStyle.DOT);

        leftTopRadius = ta.getDimensionPixelSize(R.styleable.BannerView_leftTopRadius, 0);
        rightTopRadius = ta.getDimensionPixelSize(R.styleable.BannerView_rightTopRadius, 0);
        rightBottomRadius = ta.getDimensionPixelSize(R.styleable.BannerView_rightBottomRadius, 0);
        leftBottomRadius = ta.getDimensionPixelSize(R.styleable.BannerView_leftBottomRadius, 0);

        indicatorHeight = ta.getDimension(R.styleable.BannerView_dotHeight, DensityUtil.dp2px(context, 30));
        indicatorBottomMargin = ta.getDimension(R.styleable.BannerView_dotBottomMargin, DensityUtil.dp2px(context, 12));
        indicatorLeftMargin = ta.getDimension(R.styleable.BannerView_dotLeftMargin, DensityUtil.dp2px(context, 12));
        indicatorRightMargin = ta.getDimension(R.styleable.BannerView_dotRightMargin, DensityUtil.dp2px(context, 12));
        dotPadding = ta.getDimension(R.styleable.BannerView_dotPadding, DensityUtil.dp2px(context, 8));
        indicatorTextSize = ta.getDimension(
                R.styleable.BannerView_indicatorTextSize,
                DensityUtil.sp2px(context, 14f)
        );
        indicatorCurrentTextColor = ta.getColor(
                R.styleable.BannerView_indicatorCurrentTextColor,
                ContextCompat.getColor(context, R.color.white)
        );
        indicatorTotalTextColor = ta.getColor(
                R.styleable.BannerView_indicatorTotalTextColor,
                ContextCompat.getColor(context, R.color.white)
        );
        if (ta.hasValue(R.styleable.BannerView_indicatorSeparatorTextColor)) {
            indicatorSeparatorTextColor = ta.getColor(
                    R.styleable.BannerView_indicatorSeparatorTextColor,
                    indicatorTotalTextColor
            );
        } else {
            indicatorSeparatorTextColor = indicatorTotalTextColor;
        }
        String separator = ta.getString(R.styleable.BannerView_indicatorSeparatorText);
        if (!StringUtil.isEmpty(separator)) {
            indicatorSeparatorText = separator;
        }

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
        viewPager = new ViewPager2(getContext());
        viewPager.setId(View.generateViewId());
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
        ConstraintLayout.LayoutParams viewPagerLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        indicatorLayout = new LinearLayout(getContext());
        indicatorLayout.setId(View.generateViewId());
        indicatorLayout.setBackgroundColor(Color.TRANSPARENT);
        indicatorLayout.setOrientation(LinearLayout.HORIZONTAL);
        indicatorLayout.setGravity(Gravity.CENTER_VERTICAL);
        ConstraintLayout.LayoutParams indicatorLayoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                isTextIndicator() ? ConstraintLayout.LayoutParams.WRAP_CONTENT : (int) indicatorHeight
        );
        indicatorLayoutParams.leftMargin = (int) indicatorLeftMargin;
        indicatorLayoutParams.rightMargin = (int) indicatorRightMargin;
        indicatorLayoutParams.bottomMargin = (int) indicatorBottomMargin;

        viewPagerLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        viewPagerLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        viewPagerLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        if (indicatorIsInner()) {
            viewPagerLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            indicatorLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            applyIndicatorHorizontalConstraint(indicatorLayoutParams);
        } else {
            viewPagerLayoutParams.height = 0;
            viewPagerLayoutParams.verticalWeight = 1;
            viewPagerLayoutParams.bottomToTop = indicatorLayout.getId();
            indicatorLayoutParams.topToBottom = viewPager.getId();
            indicatorLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            applyIndicatorHorizontalConstraint(indicatorLayoutParams);
        }
        removeAllViews();
        addView(viewPager, viewPagerLayoutParams);
        addView(indicatorLayout, indicatorLayoutParams);
        if (bgColor != Color.TRANSPARENT) {
            setBackgroundColor(bgColor);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void applyIndicatorHorizontalConstraint(ConstraintLayout.LayoutParams indicatorLayoutParams) {
        if (IndicatorPosition.INNER_BOTTOM_LEFT == indicatorPosition
                || IndicatorPosition.OUTER_BOTTOM_LEFT == indicatorPosition) {
            indicatorLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            indicatorLayoutParams.horizontalBias = 0f;
        } else if (IndicatorPosition.INNER_BOTTOM_RIGHT == indicatorPosition
                || IndicatorPosition.OUTER_BOTTOM_RIGHT == indicatorPosition) {
            indicatorLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            indicatorLayoutParams.horizontalBias = 1f;
        } else {
            indicatorLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            indicatorLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            indicatorLayoutParams.horizontalBias = 0.5f;
        }
    }

    private boolean indicatorIsInner() {
        return IndicatorPosition.INNER_BOTTOM_CENTER == indicatorPosition
                || IndicatorPosition.INNER_BOTTOM_LEFT == indicatorPosition
                || IndicatorPosition.INNER_BOTTOM_RIGHT == indicatorPosition;
    }

    private boolean isTextIndicator() {
        return IndicatorStyle.TEXT == indicatorStyle;
    }

    private boolean hasRoundCorner() {
        return leftTopRadius > 0 || rightTopRadius > 0 || rightBottomRadius > 0 || leftBottomRadius > 0;
    }

    public void initView(@NotNull List<T> bannerList) {
        initView(bannerList, null);
    }

    public void initView(@NotNull List<T> bannerList, PictureAdapter.OnItemClickListener onItemClickListener) {
        this.bannerList = bannerList;
        if (this.bannerList.isEmpty()) {
            return;
        }
        initIndicator();
        viewPager.setAdapter(new PictureAdapter<>(this.bannerList, placeholderImage)
                .setOnItemClickListener(onItemClickListener == null ? onDefaultImageItemClickListener : onItemClickListener));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false);
        if (autoLoop) {
            startLoop();
        }
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public LinearLayout getIndicatorLayout() {
        return indicatorLayout;
    }

    /** @deprecated 使用 {@link #getIndicatorLayout()} */
    @Deprecated
    public LinearLayout getDotsLayout() {
        return indicatorLayout;
    }

    public void setRoundDots(Drawable drawableResCurrent, Drawable drawableResNormal) {
        this.drawableResCurrent = drawableResCurrent;
        this.drawableResNormal = drawableResNormal;
    }

    public BannerView<T> setIndicatorStyle(int indicatorStyle) {
        this.indicatorStyle = indicatorStyle;
        return this;
    }

    public BannerView<T> setIndicatorPosition(int indicatorPosition) {
        this.indicatorPosition = indicatorPosition;
        return this;
    }

    public BannerView<T> setTextIndicatorStyle(
            float textSizePx,
            @ColorInt int currentTextColor,
            @ColorInt int totalTextColor
    ) {
        this.indicatorTextSize = textSizePx;
        this.indicatorCurrentTextColor = currentTextColor;
        this.indicatorTotalTextColor = totalTextColor;
        this.indicatorSeparatorTextColor = totalTextColor;
        return this;
    }

    public BannerView<T> setTextIndicatorStyle(
            float textSizePx,
            @ColorInt int currentTextColor,
            @ColorInt int totalTextColor,
            @ColorInt int separatorTextColor,
            @Nullable String separator
    ) {
        this.indicatorTextSize = textSizePx;
        this.indicatorCurrentTextColor = currentTextColor;
        this.indicatorTotalTextColor = totalTextColor;
        this.indicatorSeparatorTextColor = separatorTextColor;
        if (!StringUtil.isEmpty(separator)) {
            this.indicatorSeparatorText = separator;
        }
        return this;
    }

    public BannerView<T> setIndicatorMargins(float bottomMargin, float leftMargin, float rightMargin) {
        this.indicatorBottomMargin = bottomMargin;
        this.indicatorLeftMargin = leftMargin;
        this.indicatorRightMargin = rightMargin;
        return this;
    }

    private void initIndicator() {
        indicatorLayout.removeAllViews();
        tvCurrentPage = null;
        tvSeparator = null;
        tvTotalPage = null;
        if (this.bannerList.size() <= 1) {
            indicatorLayout.setVisibility(View.INVISIBLE);
            return;
        }
        indicatorLayout.setVisibility(View.VISIBLE);
        lastPos = 0;
        if (isTextIndicator()) {
            initTextIndicator();
        } else {
            initDotIndicator();
        }
    }

    private void initDotIndicator() {
        IntStream.range(0, this.bannerList.size()).forEach(i -> {
            ImageView round = new ImageView(getContext());
            round.setBackground(i == 0 ? drawableResCurrent : drawableResNormal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = (int) dotPadding;
            indicatorLayout.addView(round, params);
        });
    }

    private void initTextIndicator() {
        tvCurrentPage = createIndicatorTextView(indicatorCurrentTextColor);
        tvSeparator = createIndicatorTextView(indicatorSeparatorTextColor);
        tvTotalPage = createIndicatorTextView(indicatorTotalTextColor);

        tvCurrentPage.setText("1");
        tvSeparator.setText(indicatorSeparatorText);
        tvTotalPage.setText(String.valueOf(bannerList.size()));

        indicatorLayout.addView(tvCurrentPage);
        indicatorLayout.addView(tvSeparator);
        indicatorLayout.addView(tvTotalPage);
    }

    private TextView createIndicatorTextView(@ColorInt int textColor) {
        TextView textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, indicatorTextSize);
        textView.setTextColor(textColor);
        textView.setIncludeFontPadding(false);
        textView.setBackgroundColor(Color.TRANSPARENT);
        return textView;
    }

    private void updateTextIndicator(int realPos) {
        if (tvCurrentPage == null || tvTotalPage == null || bannerList == null) {
            return;
        }
        tvCurrentPage.setText(String.valueOf(realPos + 1));
        tvTotalPage.setText(String.valueOf(bannerList.size()));
    }

    public void startLoop() {
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

    protected final PictureAdapter.OnItemClickListener onDefaultImageItemClickListener = position -> {
        if (bannerList == null || position < 0 || position >= bannerList.size()) {
            return;
        }
        T item = bannerList.get(position);
        if (onBannerItemClickListener != null && onBannerItemClickListener.onBannerClick(this, item, position)) {
            return;
        }
        handleDefaultBannerClick(item, position);
    };

    private void handleDefaultBannerClick(@NonNull T item, int position) {
        if (!StringUtil.isEmpty(item.getLinkUrl())) {
            if ("#".equals(item.getLinkUrl())) {
                return;
            }
            if (item.getLinkUrl() == null) {
                return;
            }
            if (item.isLinkInside()) {
                WebViewActivity.show(getContext(), item.getLinkUrl(), "");
            } else {
                Uri uri = Uri.parse(item.getLinkUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent);
            }
            return;
        }
        if (previewLarger) {
            if (onBannerPreviewListener != null) {
                onBannerPreviewListener.onBannerPreview(this, bannerList, position);
            } else {
                showDefaultPreview(position);
            }
        }
    }

    private void showDefaultPreview(int position) {
        List<AttachmentBean> list = this.bannerList.stream().map(item -> {
            AttachmentBean attachmentBean = new AttachmentBean();
            attachmentBean.setFileType(AttachmentTypeEnum.IMAGE.typeValue);
            if (item.getBannerUrl() instanceof Integer resId) {
                attachmentBean.setPath(DrawableUtil.resourceToBase64(Config.getInstance().getApplication(), resId));
                attachmentBean.setRelativePath(DrawableUtil.resourceToBase64(Config.getInstance().getApplication(), resId));
                if (Config.getInstance().getApplication() != null) {
                    attachmentBean.setFileName(getContext().getResources().getResourceEntryName(resId));
                }
            } else if (item.getBannerUrl() instanceof Uri uri) {
                attachmentBean.setPath(uri == null ? null : uri.toString());
                attachmentBean.setRelativePath(uri == null ? null : uri.toString());
                attachmentBean.setFileName(FileUtil.getFileName(uri == null ? null : uri.toString()));
            } else {
                attachmentBean.setPath(item.getBannerUrl() == null ? null : item.getBannerUrl().toString());
                attachmentBean.setRelativePath(item.getBannerUrl() == null ? null : item.getBannerUrl().toString());
                attachmentBean.setFileName(FileUtil.getFileName(item.getBannerUrl() == null ? null : item.getBannerUrl().toString()));
            }
            return attachmentBean;
        }).collect(Collectors.toList());
        new PreviewPhotoDialog(getContext(), list, position).show();
    }

    protected final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (bannerList == null || bannerList.isEmpty()) {
                return;
            }
            int realPos = position % bannerList.size();
            if (isTextIndicator()) {
                updateTextIndicator(realPos);
            } else {
                if (lastPos >= 0 && lastPos < indicatorLayout.getChildCount() && lastPos != realPos) {
                    indicatorLayout.getChildAt(lastPos).setBackground(drawableResNormal);
                }
                if (realPos < indicatorLayout.getChildCount()) {
                    indicatorLayout.getChildAt(realPos).setBackground(drawableResCurrent);
                }
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

    private void buildRoundPath() {
        mPath.reset();
        mPath.moveTo(leftTopRadius, 0);
        mPath.lineTo(width - rightTopRadius, 0);
        mPath.quadTo(width, 0, width, rightTopRadius);
        mPath.lineTo(width, height - rightBottomRadius);
        mPath.quadTo(width, height, width - rightBottomRadius, height);
        mPath.lineTo(leftBottomRadius, height);
        mPath.quadTo(0, height, 0, height - leftBottomRadius);
        mPath.lineTo(0, leftTopRadius);
        mPath.quadTo(0, 0, leftTopRadius, 0);
        mPath.close();
    }

    private boolean shouldClipRound() {
        if (!hasRoundCorner() || width <= 0 || height <= 0) {
            return false;
        }
        int maxLeft = Math.max(leftTopRadius, leftBottomRadius);
        int maxRight = Math.max(rightTopRadius, rightBottomRadius);
        int minWidth = maxLeft + maxRight;
        int maxTop = Math.max(leftTopRadius, rightTopRadius);
        int maxBottom = Math.max(leftBottomRadius, rightBottomRadius);
        int minHeight = maxTop + maxBottom;
        return width >= minWidth && height > minHeight;
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (shouldClipRound()) {
            buildRoundPath();
            int save = canvas.save();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                canvas.clipPath(mPath);
            } else {
                canvas.clipPath(mPath, Region.Op.INTERSECT);
            }
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (bgColor == Color.TRANSPARENT || !shouldClipRound()) {
            super.onDraw(canvas);
            return;
        }
        buildRoundPath();
        canvas.drawPath(mPath, mPaint);
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
        if (viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        }
    }

    /**
     * Banner 项点击回调；返回 true 表示已消费，不再执行默认跳转/预览逻辑。
     */
    public interface OnBannerItemClickListener<T extends IBannerItem> {
        boolean onBannerClick(@NonNull BannerView<T> bannerView, @NonNull T item, int position);
    }

    /**
     * Banner 大图预览回调，用于替换默认 {@link PreviewPhotoDialog} 行为。
     */
    public interface OnBannerPreviewListener<T extends IBannerItem> {
        void onBannerPreview(@NonNull BannerView<T> bannerView, @NonNull List<T> items, int position);
    }
}
