package io.coderf.arklab.common.widget.gallery;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.utils.common.AttachmentUtil;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.dialog.ImageSaveDialogConfig;
import io.coderf.arklab.common.widget.gallery.adapter.PreviewInfoViewPagerAdapter;

/**
 * created by fz 2026/9/11
 * 信息大图预览：右上角定位、左右翻页、底部三行信息（名称/时间/定位）、右下角仿微信「查看全部」。
 * <p>不替代 {@link PreviewPhotoDialog}，两者可并存。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/11
 */
public class PreviewInfoPhotoDialog extends Dialog {

    public final static String TAG = PreviewInfoPhotoDialog.class.getSimpleName();

    private List<PreviewInfoBean> imageInfos = new ArrayList<>();
    private ViewPager2 viewPager;
    private ShapeableImageView ivLocation;
    private ShapeableImageView ivPrev;
    private ShapeableImageView ivNext;
    private ShapeableImageView ivViewAll;
    private LinearLayout llInfo;
    private MaterialTextView tvTitle;
    private MaterialTextView tvTime;
    private MaterialTextView tvLocation;
    private PreviewInfoViewPagerAdapter pageAdapter;
    private int position = 0;
    private boolean canSaveImage = true;
    @Nullable
    protected Drawable placeholderImage;
    @Nullable
    protected Drawable errorImage;
    @Nullable
    private PreviewGalleryZoomConfig zoomConfig;
    @Nullable
    private ImageSaveDialogConfig imageSaveDialogConfig;
    @NonNull
    private PreviewInfoConfig config = PreviewInfoConfig.defaults();
    @Nullable
    private OnLocationClickListener onLocationClickListener;
    @Nullable
    private OnViewAllClickListener onViewAllClickListener;

    public PreviewInfoPhotoDialog(@NonNull Context context) {
        this(context, R.style.PreviewPhotoDialog);
    }

    public PreviewInfoPhotoDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public PreviewInfoPhotoDialog(@NonNull Context context, boolean canSaveImage) {
        this(context);
        this.canSaveImage = canSaveImage;
    }

    public PreviewInfoPhotoDialog(@NonNull Context context, List<PreviewInfoBean> imageInfos, int position) {
        this(context);
        setInfoImages(imageInfos);
        this.position = position;
    }

    public PreviewInfoPhotoDialog(@NonNull Context context, List<PreviewInfoBean> imageInfos,
                                  boolean canSaveImage, int position) {
        this(context, imageInfos, position);
        this.canSaveImage = canSaveImage;
    }

    public PreviewInfoPhotoDialog currentPosition(int position) {
        this.position = position;
        return this;
    }

    public PreviewInfoPhotoDialog setInfoImages(@Nullable List<PreviewInfoBean> imageInfoList) {
        this.imageInfos = imageInfoList == null ? new ArrayList<>() : imageInfoList;
        return this;
    }

    public PreviewInfoPhotoDialog setImages(@Nullable List<AttachmentBean> imageInfoList) {
        return setInfoImages(PreviewInfoBean.fromAttachments(imageInfoList));
    }

    public boolean isCanSaveImage() {
        return canSaveImage;
    }

    public PreviewInfoPhotoDialog setCanSaveImage(boolean canSaveImage) {
        this.canSaveImage = canSaveImage;
        return this;
    }

    @Nullable
    public Drawable getErrorImage() {
        return errorImage;
    }

    @Nullable
    public Drawable getPlaceholderImage() {
        return placeholderImage;
    }

    public PreviewInfoPhotoDialog createImageInfo(String image) {
        return createImageInfo(List.of(image));
    }

    public PreviewInfoPhotoDialog createImageResInfo(@DrawableRes int imageRes) {
        return createImageResInfo(List.of(imageRes));
    }

    public PreviewInfoPhotoDialog createImageResInfo(@DrawableRes List<Integer> imageResList) {
        return setImages(AttachmentUtil.drawableResToAttachmentList(getContext(), imageResList, null, null));
    }

    public PreviewInfoPhotoDialog createImageInfo(String... image) {
        if (image == null) {
            return this;
        }
        return createImageInfo(Arrays.asList(image));
    }

    public PreviewInfoPhotoDialog createUriImageInfo(Uri... uri) {
        if (uri == null) {
            return this;
        }
        return createUriImageInfo(Arrays.asList(uri));
    }

    public PreviewInfoPhotoDialog createImageInfo(@Nullable List<String> images) {
        return setInfoImages(PreviewInfoBean.fromPaths(images));
    }

    public PreviewInfoPhotoDialog createUriImageInfo(@Nullable List<Uri> images) {
        if (images == null) {
            return this;
        }
        return setImages(AttachmentUtil.uriListToAttachmentList(images));
    }

    public PreviewInfoPhotoDialog setPlaceholderImage(@Nullable Drawable placeholderImage) {
        this.placeholderImage = placeholderImage;
        return this;
    }

    public PreviewInfoPhotoDialog setErrorImage(@Nullable Drawable errorImage) {
        this.errorImage = errorImage;
        return this;
    }

    /**
     * 设置本次预览的缩放配置，仅当前 Dialog 生效；未设置时使用全局配置。
     */
    public PreviewInfoPhotoDialog setZoomConfig(@Nullable PreviewGalleryZoomConfig zoomConfig) {
        this.zoomConfig = zoomConfig;
        return this;
    }

    public PreviewGalleryZoomConfig getEffectiveZoomConfig() {
        return zoomConfig != null ? zoomConfig : PreviewGalleryConfig.getGlobalZoomConfig();
    }

    public PreviewInfoPhotoDialog setImageSaveDialogConfig(@Nullable ImageSaveDialogConfig imageSaveDialogConfig) {
        this.imageSaveDialogConfig = imageSaveDialogConfig;
        return this;
    }

    public ImageSaveDialogConfig getEffectiveImageSaveDialogConfig() {
        if (imageSaveDialogConfig != null) {
            return imageSaveDialogConfig;
        }
        ImageSaveDialogConfig global = PreviewGalleryConfig.getGlobalImageSaveDialogConfig();
        return global != null ? global : ImageSaveDialogConfig.empty();
    }

    public PreviewInfoPhotoDialog setConfig(@Nullable PreviewInfoConfig config) {
        this.config = config == null ? PreviewInfoConfig.defaults() : config;
        return this;
    }

    @NonNull
    public PreviewInfoConfig getConfig() {
        return config;
    }

    public PreviewInfoPhotoDialog setLocationEnabled(boolean enabled) {
        config.setLocationEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setViewAllEnabled(boolean enabled) {
        config.setViewAllEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setNavEnabled(boolean enabled) {
        config.setNavEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setTitleEnabled(boolean enabled) {
        config.setTitleEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setTimeEnabled(boolean enabled) {
        config.setTimeEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setLocationTextEnabled(boolean enabled) {
        config.setLocationTextEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setPrevCircleBackgroundEnabled(boolean enabled) {
        config.setPrevCircleBackgroundEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setNextCircleBackgroundEnabled(boolean enabled) {
        config.setNextCircleBackgroundEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setViewAllCircleBackgroundEnabled(boolean enabled) {
        config.setViewAllCircleBackgroundEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setNavCircleBackgroundEnabled(boolean enabled) {
        config.setNavCircleBackgroundEnabled(enabled);
        return this;
    }

    public PreviewInfoPhotoDialog setLocationIcon(@Nullable Drawable locationIcon) {
        config.setLocationIcon(locationIcon);
        return this;
    }

    public PreviewInfoPhotoDialog setLocationIcon(@DrawableRes int locationIconRes) {
        return setLocationIcon(ContextCompat.getDrawable(getContext(), locationIconRes));
    }

    public PreviewInfoPhotoDialog setPrevIcon(@Nullable Drawable prevIcon) {
        config.setPrevIcon(prevIcon);
        return this;
    }

    public PreviewInfoPhotoDialog setPrevIcon(@DrawableRes int prevIconRes) {
        return setPrevIcon(ContextCompat.getDrawable(getContext(), prevIconRes));
    }

    public PreviewInfoPhotoDialog setNextIcon(@Nullable Drawable nextIcon) {
        config.setNextIcon(nextIcon);
        return this;
    }

    public PreviewInfoPhotoDialog setNextIcon(@DrawableRes int nextIconRes) {
        return setNextIcon(ContextCompat.getDrawable(getContext(), nextIconRes));
    }

    public PreviewInfoPhotoDialog setViewAllIcon(@Nullable Drawable viewAllIcon) {
        config.setViewAllIcon(viewAllIcon);
        return this;
    }

    public PreviewInfoPhotoDialog setViewAllIcon(@DrawableRes int viewAllIconRes) {
        return setViewAllIcon(ContextCompat.getDrawable(getContext(), viewAllIconRes));
    }

    public PreviewInfoPhotoDialog setNavBackground(@Nullable Drawable navBackground) {
        config.setNavBackground(navBackground);
        return this;
    }

    public PreviewInfoPhotoDialog setTitleTextSizeSp(float titleTextSizeSp) {
        config.setTitleTextSizePx((float) DensityUtil.sp2px(getContext(), titleTextSizeSp));
        return this;
    }

    public PreviewInfoPhotoDialog setTitleTextColor(@ColorInt int titleTextColor) {
        config.setTitleTextColor(titleTextColor);
        return this;
    }

    public PreviewInfoPhotoDialog setTimeTextSizeSp(float timeTextSizeSp) {
        config.setTimeTextSizePx((float) DensityUtil.sp2px(getContext(), timeTextSizeSp));
        return this;
    }

    public PreviewInfoPhotoDialog setTimeTextColor(@ColorInt int timeTextColor) {
        config.setTimeTextColor(timeTextColor);
        return this;
    }

    public PreviewInfoPhotoDialog setLocationTextSizeSp(float locationTextSizeSp) {
        config.setLocationTextSizePx((float) DensityUtil.sp2px(getContext(), locationTextSizeSp));
        return this;
    }

    public PreviewInfoPhotoDialog setLocationTextColor(@ColorInt int locationTextColor) {
        config.setLocationTextColor(locationTextColor);
        return this;
    }

    public PreviewInfoPhotoDialog setTitleTimeSpacingDp(float spacingDp) {
        config.setTitleTimeSpacingPx(DensityUtil.dp2px(getContext(), spacingDp));
        return this;
    }

    public PreviewInfoPhotoDialog setTimeLocationSpacingDp(float spacingDp) {
        config.setTimeLocationSpacingPx(DensityUtil.dp2px(getContext(), spacingDp));
        return this;
    }

    public PreviewInfoPhotoDialog setLocationIconSizeDp(float sizeDp) {
        config.setLocationIconSizePx(DensityUtil.dp2px(getContext(), sizeDp));
        return this;
    }

    public PreviewInfoPhotoDialog setNavIconSizeDp(float sizeDp) {
        config.setNavIconSizePx(DensityUtil.dp2px(getContext(), sizeDp));
        return this;
    }

    public PreviewInfoPhotoDialog setViewAllIconSizeDp(float sizeDp) {
        config.setViewAllIconSizePx(DensityUtil.dp2px(getContext(), sizeDp));
        return this;
    }

    public PreviewInfoPhotoDialog setAlbumTitle(@Nullable String albumTitle) {
        config.setAlbumTitle(albumTitle);
        return this;
    }

    public PreviewInfoPhotoDialog setOnLocationClickListener(@Nullable OnLocationClickListener listener) {
        this.onLocationClickListener = listener;
        return this;
    }

    public PreviewInfoPhotoDialog setOnViewAllClickListener(@Nullable OnViewAllClickListener listener) {
        this.onViewAllClickListener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dialog_preview_info);
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
        View root = findViewById(R.id.cl_preview_info);
        viewPager = findViewById(R.id.preview_info_viewPager);
        ivLocation = findViewById(R.id.iv_preview_location);
        ivPrev = findViewById(R.id.iv_preview_prev);
        ivNext = findViewById(R.id.iv_preview_next);
        ivViewAll = findViewById(R.id.iv_preview_view_all);
        llInfo = findViewById(R.id.ll_preview_info);
        tvTitle = findViewById(R.id.tv_preview_title);
        tvTime = findViewById(R.id.tv_preview_time);
        tvLocation = findViewById(R.id.tv_preview_location);
        applyStyle(root);
        setCanceledOnTouchOutside(false);
        bindClicks();
        initPageAdapter();
        if (position < 0 || position >= imageInfos.size()) {
            position = 0;
        }
        if (!imageInfos.isEmpty()) {
            viewPager.setCurrentItem(position, false);
        }
        bindCurrentInfo();
        updateNavVisibility();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                position = pos;
                bindCurrentInfo();
                updateNavVisibility();
            }
        });
        applyWindowInsets(root);
    }

    private void applyStyle(@Nullable View root) {
        int bgColor = config.getBackgroundColor() != null
                ? config.getBackgroundColor()
                : PreviewInfoConfig.DEFAULT_BACKGROUND_COLOR;
        if (root != null) {
            root.setBackgroundColor(bgColor);
        }
        ivLocation.setVisibility(config.isLocationEnabled() ? View.VISIBLE : View.GONE);
        ivViewAll.setVisibility(config.isViewAllEnabled() ? View.VISIBLE : View.GONE);
        ConstraintLayout.LayoutParams pagerLp = (ConstraintLayout.LayoutParams) viewPager.getLayoutParams();
        if (config.isLocationEnabled()) {
            pagerLp.topToBottom = R.id.iv_preview_location;
            pagerLp.topToTop = ConstraintLayout.LayoutParams.UNSET;
        } else {
            pagerLp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            pagerLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        }
        viewPager.setLayoutParams(pagerLp);
        if (config.getLocationIcon() != null) {
            ivLocation.setImageDrawable(config.getLocationIcon());
        }
        if (config.getPrevIcon() != null) {
            ivPrev.setImageDrawable(config.getPrevIcon());
        }
        if (config.getNextIcon() != null) {
            ivNext.setImageDrawable(config.getNextIcon());
        }
        if (config.getViewAllIcon() != null) {
            ivViewAll.setImageDrawable(config.getViewAllIcon());
        }
        applyCircleIconBackground(ivPrev, config.isPrevCircleBackgroundEnabled());
        applyCircleIconBackground(ivNext, config.isNextCircleBackgroundEnabled());
        applyCircleIconBackground(ivViewAll, config.isViewAllCircleBackgroundEnabled());
        applyIconSize(ivLocation, config.getLocationIconSizePx());
        applyIconSize(ivPrev, config.getNavIconSizePx());
        applyIconSize(ivNext, config.getNavIconSizePx());
        applyIconSize(ivViewAll, config.getViewAllIconSizePx());

        applyTextStyle(tvTitle,
                config.getTitleTextSizePx() != null
                        ? config.getTitleTextSizePx()
                        : DensityUtil.sp2px(getContext(), 16f),
                config.getTitleTextColor() != null
                        ? config.getTitleTextColor()
                        : PreviewInfoConfig.DEFAULT_TITLE_TEXT_COLOR);
        applyTextStyle(tvTime,
                config.getTimeTextSizePx() != null
                        ? config.getTimeTextSizePx()
                        : DensityUtil.sp2px(getContext(), 13f),
                config.getTimeTextColor() != null
                        ? config.getTimeTextColor()
                        : PreviewInfoConfig.DEFAULT_SUB_TEXT_COLOR);
        applyTextStyle(tvLocation,
                config.getLocationTextSizePx() != null
                        ? config.getLocationTextSizePx()
                        : DensityUtil.sp2px(getContext(), 13f),
                config.getLocationTextColor() != null
                        ? config.getLocationTextColor()
                        : PreviewInfoConfig.DEFAULT_SUB_TEXT_COLOR);

        int titleTimeSpacing = config.getTitleTimeSpacingPx() != null
                ? config.getTitleTimeSpacingPx()
                : DensityUtil.dp2px(getContext(), 6f);
        int timeLocationSpacing = config.getTimeLocationSpacingPx() != null
                ? config.getTimeLocationSpacingPx()
                : DensityUtil.dp2px(getContext(), 4f);
        applyTopMargin(tvTime, titleTimeSpacing);
        applyTopMargin(tvLocation, timeLocationSpacing);

        int paddingStart = config.getInfoPaddingStartPx() != null
                ? config.getInfoPaddingStartPx()
                : DensityUtil.dp2px(getContext(), 16f);
        int paddingTop = config.getInfoPaddingTopPx() != null
                ? config.getInfoPaddingTopPx()
                : DensityUtil.dp2px(getContext(), 12f);
        int paddingEnd = config.getInfoPaddingEndPx() != null
                ? config.getInfoPaddingEndPx()
                : DensityUtil.dp2px(getContext(), 72f);
        int paddingBottom = config.getInfoPaddingBottomPx() != null
                ? config.getInfoPaddingBottomPx()
                : DensityUtil.dp2px(getContext(), 24f);
        llInfo.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
    }

    /**
     * ShapeableImageView 默认按矩形绘制 background，必须用圆形 ShapeAppearance +
     * oval background 的 Outline 才能把底裁成圆。仅当前预览页生效。
     */
    private void applyCircleIconBackground(ShapeableImageView view, boolean enabled) {
        if (!enabled) {
            view.setBackground(null);
            view.setClipToOutline(false);
            return;
        }
        Drawable background = config.getNavBackground();
        if (background != null && background.getConstantState() != null) {
            view.setBackground(background.getConstantState().newDrawable().mutate());
        } else if (background != null) {
            view.setBackground(background.mutate());
        } else {
            view.setBackgroundResource(R.drawable.bg_preview_nav_circle);
        }
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        view.setClipToOutline(true);
    }

    private void applyTextStyle(MaterialTextView textView, float textSizePx, @ColorInt int textColor) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        textView.setTextColor(textColor);
    }

    private void applyIconSize(View view, @Nullable Integer sizePx) {
        if (sizePx == null || sizePx <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            lp.width = sizePx;
            lp.height = sizePx;
            view.setLayoutParams(lp);
        }
    }

    private void applyTopMargin(View view, int topMarginPx) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.topMargin = topMarginPx;
            view.setLayoutParams(lp);
        }
    }

    private void bindClicks() {
        ivLocation.setOnClickListener(v -> {
            if (onLocationClickListener == null || imageInfos.isEmpty()) {
                return;
            }
            onLocationClickListener.onLocationClick(this, currentItem(), position);
        });
        ivPrev.setOnClickListener(v -> {
            if (position > 0) {
                viewPager.setCurrentItem(position - 1, true);
            }
        });
        ivNext.setOnClickListener(v -> {
            if (position < imageInfos.size() - 1) {
                viewPager.setCurrentItem(position + 1, true);
            }
        });
        ivViewAll.setOnClickListener(v -> {
            if (imageInfos.isEmpty()) {
                return;
            }
            if (onViewAllClickListener != null) {
                onViewAllClickListener.onViewAllClick(this, imageInfos, position);
                return;
            }
            openDefaultAlbum();
        });
    }

    private void openDefaultAlbum() {
        new PreviewAlbumDialog(getContext())
                .setImages(imageInfos)
                .setCurrentPosition(position)
                .setAlbumTitle(config.getAlbumTitle())
                .setPlaceholderImage(placeholderImage)
                .setErrorImage(errorImage)
                .setOnItemClickListener((dialog, item, pos) -> {
                    dialog.dismiss();
                    if (pos >= 0 && pos < imageInfos.size()) {
                        viewPager.setCurrentItem(pos, false);
                    }
                })
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initPageAdapter() {
        if (pageAdapter == null) {
            pageAdapter = new PreviewInfoViewPagerAdapter(this);
            pageAdapter.setList(imageInfos);
            viewPager.setAdapter(pageAdapter);
        } else {
            pageAdapter.setList(imageInfos);
            pageAdapter.notifyDataSetChanged();
        }
        if (!imageInfos.isEmpty()) {
            viewPager.setOffscreenPageLimit(Math.max(1, Math.min(imageInfos.size(), 4)));
        }
    }

    private void bindCurrentInfo() {
        PreviewInfoBean item = currentItem();
        if (item == null) {
            tvTitle.setVisibility(View.GONE);
            tvTime.setVisibility(View.GONE);
            tvLocation.setVisibility(View.GONE);
            llInfo.setVisibility(View.GONE);
            return;
        }
        bindLine(tvTitle, config.isTitleEnabled() ? item.resolveTitle() : null);
        bindLine(tvTime, config.isTimeEnabled() ? item.resolveTimeText() : null);
        bindLine(tvLocation, config.isLocationTextEnabled() ? item.resolveLocationText() : null);
        boolean hasInfo = tvTitle.getVisibility() == View.VISIBLE
                || tvTime.getVisibility() == View.VISIBLE
                || tvLocation.getVisibility() == View.VISIBLE;
        llInfo.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
    }

    private void bindLine(MaterialTextView textView, @Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
            textView.setText("");
            return;
        }
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
    }

    private void updateNavVisibility() {
        boolean showNav = config.isNavEnabled() && imageInfos.size() > 1;
        ivPrev.setVisibility(showNav && position > 0 ? View.VISIBLE : View.GONE);
        ivNext.setVisibility(showNav && position < imageInfos.size() - 1 ? View.VISIBLE : View.GONE);
    }

    @Nullable
    private PreviewInfoBean currentItem() {
        if (position < 0 || position >= imageInfos.size()) {
            return null;
        }
        return imageInfos.get(position);
    }

    private void applyWindowInsets(@NonNull View root) {
        final int locationTop = ((ViewGroup.MarginLayoutParams) ivLocation.getLayoutParams()).topMargin;
        final int locationEnd = ((ViewGroup.MarginLayoutParams) ivLocation.getLayoutParams()).getMarginEnd();
        final int pagerTop = ((ViewGroup.MarginLayoutParams) viewPager.getLayoutParams()).topMargin;
        final int viewAllEnd = ((ViewGroup.MarginLayoutParams) ivViewAll.getLayoutParams()).getMarginEnd();
        final int viewAllBottom = ((ViewGroup.MarginLayoutParams) ivViewAll.getLayoutParams()).bottomMargin;
        final int infoPadStart = llInfo.getPaddingStart();
        final int infoPadTop = llInfo.getPaddingTop();
        final int infoPadEnd = llInfo.getPaddingEnd();
        final int infoPadBottom = llInfo.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams locationLp = (ViewGroup.MarginLayoutParams) ivLocation.getLayoutParams();
            locationLp.topMargin = locationTop + bars.top;
            locationLp.setMarginEnd(locationEnd);
            ivLocation.setLayoutParams(locationLp);
            ViewGroup.MarginLayoutParams pagerLp = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
            pagerLp.topMargin = config.isLocationEnabled() ? pagerTop : pagerTop + bars.top;
            viewPager.setLayoutParams(pagerLp);
            ViewGroup.MarginLayoutParams viewAllLp = (ViewGroup.MarginLayoutParams) ivViewAll.getLayoutParams();
            viewAllLp.setMarginEnd(viewAllEnd);
            viewAllLp.bottomMargin = viewAllBottom + bars.bottom;
            ivViewAll.setLayoutParams(viewAllLp);
            llInfo.setPadding(infoPadStart, infoPadTop, infoPadEnd, infoPadBottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    /**
     * 右上角定位图标点击。
     */
    public interface OnLocationClickListener {
        void onLocationClick(@NonNull PreviewInfoPhotoDialog dialog, @Nullable PreviewInfoBean item, int position);
    }

    /**
     * 右下角「查看全部」点击。未设置时打开内置仿微信相册。
     */
    public interface OnViewAllClickListener {
        void onViewAllClick(@NonNull PreviewInfoPhotoDialog dialog,
                            @NonNull List<PreviewInfoBean> items,
                            int position);
    }
}
