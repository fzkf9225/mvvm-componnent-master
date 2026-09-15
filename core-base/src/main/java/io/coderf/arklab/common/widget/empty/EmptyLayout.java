package io.coderf.arklab.common.widget.empty;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.google.android.material.loadingindicator.LoadingIndicator;

import io.coderf.arklab.common.utils.theme.ThemeAttrs;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 可配置的空白占位 View，支持加载中、加载失败、无数据等状态。
 * <p>
 * 加载态支持两种样式（{@link LoadingStyle}）：
 * <ul>
 *     <li>{@link LoadingStyle#DEFAULT} — 居中 {@link LoadingIndicator}（Material 1.13+）</li>
 *     <li>{@link LoadingStyle#SKELETON} — 骨架屏 Shimmer 占位（可通过 XML {@code app:loadingStyle="skeleton"} 或代码开启）</li>
 * </ul>
 * 默认加载文案若以 {@code .} 或省略号 {@code …} 结尾，会按点号个数循环播放
 * （{@code app:loadingDotAnimEnabled}，默认开启；尾部无点号时不播放）。
 * <p>
 * 空态图标、文案、颜色、字号、字重默认来自 {@link EmptyLayoutConfig}
 * （经 {@link Config#getEmptyLayoutConfig()}）。
 * 优先级：页面 XML / setter &gt; 全局配置 &gt; 框架内置。
 *
 * @update 2026/7/13 11:00
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/12/9 16:30
 */
public class EmptyLayout extends ConstraintLayout {

    /**
     * 首刷/重试加载时的展示样式。
     */
    public enum LoadingStyle {
        /** 居中 Loading 转圈（默认） */
        DEFAULT(0),
        /** 骨架屏 Shimmer 占位行 */
        SKELETON(1);

        private final int value;

        LoadingStyle(int value) {
            this.value = value;
        }

        public static LoadingStyle fromValue(int value) {
            return value == SKELETON.value ? SKELETON : DEFAULT;
        }
    }

    public enum State {
        HIDE_LAYOUT,           // 隐藏布局
        LOADING_ERROR,         // 错误
        NETWORK_LOADING,       // 加载中
        NO_DATA,               // 暂无数据
        NO_DATA_ENABLE_CLICK,  // 暂无数据，点击重试
        NETWORK_LOADING_REFRESH // 加载中，点击重试
    }

    // 当前状态
    private State currentState = State.HIDE_LAYOUT;

    // 点击监听
    private OnEmptyLayoutClickListener onEmptyLayoutClickListener;
    private boolean clickEnable = true;

    // 自定义内容
    private String customNoDataContent = "";

    // ==================== 图片资源（默认取全局 EmptyLayoutConfig，未配置则为框架内置） ====================
    @DrawableRes private int errorImage;
    @DrawableRes private int loadingImage;
    @DrawableRes private int noDataImage;
    /** 可点击重试态使用刷新图标；加载中静态图参见 {@link #loadingImage}（沙漏） */
    @DrawableRes private int clickableNoDataImage;

    // ==================== 文字资源（默认取全局 EmptyLayoutConfig） ====================
    @StringRes private int errorText;
    @StringRes private int loadingText;
    @StringRes private int noDataText;
    @StringRes private int clickableNoDataText;

    // ==================== 文字颜色 ====================
    private int errorTextColor;
    private int loadingTextColor;
    private int noDataTextColor;
    private int clickableNoDataTextColor;

    // ==================== 文字大小 ====================
    private float errorTextSize;
    private float loadingTextSize;
    private float noDataTextSize;
    private float clickableNoDataTextSize;

    // ==================== 文字样式 ====================
    private int errorTextStyle;      // 0-正常 1-加粗
    private int loadingTextStyle;
    private int noDataTextStyle;
    private int clickableNoDataTextStyle;

    // ==================== 布局对齐方式 ====================
    /**
     * 整体内容对齐方式
     * 0: 居中对齐
     * 1: 顶部对齐
     * 2: 底部对齐
     */
    private int contentGravity = 0;

    /**
     * 图片和文字的排列方式
     * 0: 图片在上，文字在下
     * 1: 文字在上，图片在下
     */
    private int imageTextArrangement = 0;

    // ==================== 间距设置 ====================
    private float contentStartMargin;   // 内容左侧边距
    private float contentEndMargin;     // 内容右侧边距
    private float contentTopMargin;      // 内容顶部边距（用于顶部对齐）
    private float contentBottomMargin;   // 内容底部边距（用于底部对齐）
    private float imageTextMargin;       // 图片和文字之间的间距

    // ==================== 图片尺寸 ====================
    private float imageWidth;
    private float imageHeight;
    private float errorImageWidth;
    private float errorImageHeight;
    private float loadingImageWidth;
    private float loadingImageHeight;
    private float noDataImageWidth;
    private float noDataImageHeight;
    private float clickableNoDataImageWidth;
    private float clickableNoDataImageHeight;

    // ==================== Loading尺寸 ====================
    private float loadingWidth;
    private float loadingHeight;

    // ==================== Loading 样式 ====================
    /** 加载态展示方式，默认 {@link LoadingStyle#DEFAULT}，不影响旧项目 */
    private LoadingStyle loadingStyle = LoadingStyle.DEFAULT;
    private float skeletonPaddingPx;

    // ==================== 加载文案尾部点号动画 ====================
    /** 水平省略号，等价于三个 {@code .} */
    private static final char HORIZONTAL_ELLIPSIS = '\u2026';
    private static final long LOADING_DOT_INTERVAL_MS = 400L;
    /** 加载文案尾部的 {@code .} / {@code …} 是否循环播放，默认开启 */
    private boolean loadingDotAnimEnabled = true;
    private String loadingTextPrefix = "";
    private int loadingDotMaxCount;
    private int loadingDotCurrentCount;
    private final Runnable loadingDotTicker = this::tickLoadingDots;

    // ==================== 控件引用 ====================
    private LinearLayout containerView;  // 中间容器层（Loading / 图片 / 文字）
    private SkeletonShimmerPanel skeletonPanel; // 骨架屏层，按需显示
    private ShapeableImageView ivImage;
    private LoadingIndicator loadingView;
    private MaterialTextView tvText;
    /** 进入骨架屏加载前备份的背景；退出时还原，避免把 XML / setter 背景清成透明。 */
    @Nullable
    private Drawable overlayBackupBackground;
    private boolean skeletonOverlayApplied;

    public EmptyLayout(@NonNull Context context) {
        this(context, null);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        initViews(attrs);
        setupClickListener();
    }

    private void initAttr(AttributeSet attrs) {
        applyGlobalDefaults();
        // 布局类默认值（尚未纳入 EmptyLayoutConfig，避免改变间距/对齐行为）
        contentGravity = 0;
        imageTextArrangement = 0;

        contentStartMargin = 0;
        contentEndMargin = 0;
        contentTopMargin = DensityUtil.dp2px(getContext(), 20);
        contentBottomMargin = DensityUtil.dp2px(getContext(), 20);
        imageTextMargin = DensityUtil.dp2px(getContext(), 16);

        loadingWidth = DensityUtil.dp2px(getContext(), 32);
        loadingHeight = DensityUtil.dp2px(getContext(), 32);
        skeletonPaddingPx = DensityUtil.dp2px(getContext(), 16);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmptyLayout);

            // 图片资源
            errorImage = a.getResourceId(R.styleable.EmptyLayout_errorImage, errorImage);
            loadingImage = a.getResourceId(R.styleable.EmptyLayout_loadingImage, loadingImage);
            noDataImage = a.getResourceId(R.styleable.EmptyLayout_noDataImage, noDataImage);
            clickableNoDataImage = a.getResourceId(R.styleable.EmptyLayout_clickableNoDataImage, clickableNoDataImage);

            // 文字资源
            errorText = a.getResourceId(R.styleable.EmptyLayout_errorText, errorText);
            loadingText = a.getResourceId(R.styleable.EmptyLayout_loadingText, loadingText);
            noDataText = a.getResourceId(R.styleable.EmptyLayout_noDataText, noDataText);
            clickableNoDataText = a.getResourceId(R.styleable.EmptyLayout_clickableNoDataText, clickableNoDataText);

            // 文字颜色
            errorTextColor = a.getColor(R.styleable.EmptyLayout_errorTextColor, errorTextColor);
            loadingTextColor = a.getColor(R.styleable.EmptyLayout_loadingTextColor, loadingTextColor);
            noDataTextColor = a.getColor(R.styleable.EmptyLayout_noDataTextColor, noDataTextColor);
            clickableNoDataTextColor = a.getColor(R.styleable.EmptyLayout_clickableNoDataTextColor, clickableNoDataTextColor);

            // 文字大小
            errorTextSize = a.getDimension(R.styleable.EmptyLayout_errorTextSize, errorTextSize);
            loadingTextSize = a.getDimension(R.styleable.EmptyLayout_loadingTextSize, loadingTextSize);
            noDataTextSize = a.getDimension(R.styleable.EmptyLayout_noDataTextSize, noDataTextSize);
            clickableNoDataTextSize = a.getDimension(R.styleable.EmptyLayout_clickableNoDataTextSize, clickableNoDataTextSize);

            // 文字样式
            errorTextStyle = a.getInt(R.styleable.EmptyLayout_errorTextStyle, errorTextStyle);
            loadingTextStyle = a.getInt(R.styleable.EmptyLayout_loadingTextStyle, loadingTextStyle);
            noDataTextStyle = a.getInt(R.styleable.EmptyLayout_noDataTextStyle, noDataTextStyle);
            clickableNoDataTextStyle = a.getInt(R.styleable.EmptyLayout_clickableNoDataTextStyle, clickableNoDataTextStyle);

            // 布局对齐
            contentGravity = a.getInt(R.styleable.EmptyLayout_contentGravity, contentGravity);
            imageTextArrangement = a.getInt(R.styleable.EmptyLayout_imageTextArrangement, imageTextArrangement);

            // 间距
            contentStartMargin = a.getDimension(R.styleable.EmptyLayout_contentStartMargin, contentStartMargin);
            contentEndMargin = a.getDimension(R.styleable.EmptyLayout_contentEndMargin, contentEndMargin);
            contentTopMargin = a.getDimension(R.styleable.EmptyLayout_contentTopMargin, contentTopMargin);
            contentBottomMargin = a.getDimension(R.styleable.EmptyLayout_contentBottomMargin, contentBottomMargin);
            imageTextMargin = a.getDimension(R.styleable.EmptyLayout_imageTextMargin, imageTextMargin);

            // 图片尺寸
            imageWidth = a.getDimension(R.styleable.EmptyLayout_imageWidth, imageWidth);
            imageHeight = a.getDimension(R.styleable.EmptyLayout_imageHeight, imageHeight);
            errorImageWidth = a.getDimension(R.styleable.EmptyLayout_errorImageWidth, errorImageWidth);
            errorImageHeight = a.getDimension(R.styleable.EmptyLayout_errorImageHeight, errorImageHeight);
            loadingImageWidth = a.getDimension(R.styleable.EmptyLayout_loadingImageWidth, loadingImageWidth);
            loadingImageHeight = a.getDimension(R.styleable.EmptyLayout_loadingImageHeight, loadingImageHeight);
            noDataImageWidth = a.getDimension(R.styleable.EmptyLayout_noDataImageWidth, noDataImageWidth);
            noDataImageHeight = a.getDimension(R.styleable.EmptyLayout_noDataImageHeight, noDataImageHeight);
            clickableNoDataImageWidth = a.getDimension(
                    R.styleable.EmptyLayout_clickableNoDataImageWidth, clickableNoDataImageWidth);
            clickableNoDataImageHeight = a.getDimension(
                    R.styleable.EmptyLayout_clickableNoDataImageHeight, clickableNoDataImageHeight);
            loadingWidth = a.getDimension(R.styleable.EmptyLayout_loadingWidth, loadingWidth);
            loadingHeight = a.getDimension(R.styleable.EmptyLayout_loadingHeight, loadingHeight);

            loadingStyle = LoadingStyle.fromValue(
                    a.getInt(R.styleable.EmptyLayout_loadingStyle, LoadingStyle.DEFAULT.value));
            skeletonPaddingPx = a.getDimension(R.styleable.EmptyLayout_skeletonPadding, skeletonPaddingPx);
            loadingDotAnimEnabled = a.getBoolean(
                    R.styleable.EmptyLayout_loadingDotAnimEnabled, loadingDotAnimEnabled);

            a.recycle();
        }
    }

    /**
     * 从进程级 {@link EmptyLayoutConfig} 填入图标 / 文案 / 颜色 / 字号 / 字重默认值。
     * XML / setter 随后可覆盖；未改全局配置时与原先框架内置默认一致。
     */
    private void applyGlobalDefaults() {
        applyConfigValues(Config.getInstance().getEmptyLayoutConfig());
    }

    /**
     * 将配置应用到本实例字段。不改变当前已展示状态，除非调用方随后 {@link #setState(State)}。
     */
    private void applyConfigValues(@NonNull EmptyLayoutConfig config) {
        errorImage = config.getErrorImageRes();
        loadingImage = config.getLoadingImageRes();
        noDataImage = config.getNoDataImageRes();
        clickableNoDataImage = config.getClickableNoDataImageRes();

        errorText = config.getErrorTextRes();
        loadingText = config.getLoadingTextRes();
        noDataText = config.getNoDataTextRes();
        clickableNoDataText = config.getClickableNoDataTextRes();

        int themeTextColor = ThemeAttrs.onSurfaceVariant(getContext());
        errorTextColor = resolveConfiguredColor(config.getErrorTextColor(), themeTextColor);
        loadingTextColor = resolveConfiguredColor(config.getLoadingTextColor(), themeTextColor);
        noDataTextColor = resolveConfiguredColor(config.getNoDataTextColor(), themeTextColor);
        clickableNoDataTextColor = resolveConfiguredColor(
                config.getClickableNoDataTextColor(), themeTextColor);

        errorTextSize = DensityUtil.sp2px(getContext(), config.getErrorTextSizeSp());
        loadingTextSize = DensityUtil.sp2px(getContext(), config.getLoadingTextSizeSp());
        noDataTextSize = DensityUtil.sp2px(getContext(), config.getNoDataTextSizeSp());
        clickableNoDataTextSize = DensityUtil.sp2px(getContext(), config.getClickableNoDataTextSizeSp());

        errorTextStyle = config.getErrorTextStyle();
        loadingTextStyle = config.getLoadingTextStyle();
        noDataTextStyle = config.getNoDataTextStyle();
        clickableNoDataTextStyle = config.getClickableNoDataTextStyle();

        loadingDotAnimEnabled = config.isLoadingDotAnimEnabled();
    }

    private static int resolveConfiguredColor(int configuredColor, int themeFallback) {
        return configuredColor != 0 ? configuredColor : themeFallback;
    }

    /**
     * 在 {@link #initViews()} 创建骨架屏后，应用 XML 中的骨架屏细项配置。
     */
    private void applySkeletonAttributesFromXml(@Nullable AttributeSet attrs) {
        if (attrs == null || skeletonPanel == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmptyLayout);
        skeletonPanel.readFromEmptyLayoutStyleable(a);
        a.recycle();
    }

    public MaterialTextView getTvText() {
        return tvText;
    }

    public LoadingIndicator getLoadingView() {
        return loadingView;
    }

    public ShapeableImageView getIvImage() {
        return ivImage;
    }

    public LinearLayout getContainerView() {
        return containerView;
    }

    /**
     * 骨架屏面板，仅在 {@link LoadingStyle#SKELETON} 时展示；也可用于细粒度样式调整。
     */
    @Nullable
    public SkeletonShimmerPanel getSkeletonPanel() {
        return skeletonPanel;
    }

    private void initViews(@Nullable AttributeSet attrs) {
        // 创建容器 View（LinearLayout）
        createContainer();

        // 创建图片 View
        createImageView();

        // 创建 LoadingView
        createLoadingView();

        // 创建文字 View
        createTextView();

        // 将所有 View 添加到容器中
        setupContainer();

        // 骨架屏层（默认 GONE，不影响旧布局）
        createSkeletonPanel();
        applySkeletonAttributesFromXml(attrs);

        // 应用布局
        applyConstraints();
    }

    private void createContainer() {
        containerView = new LinearLayout(getContext());
        containerView.setId(View.generateViewId());
        containerView.setOrientation(LinearLayout.VERTICAL);
        containerView.setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        addView(containerView, params);
    }

    private void createImageView() {
        ivImage = new ShapeableImageView(getContext());
        ivImage.setId(View.generateViewId());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth > 0 ? (int) imageWidth : LinearLayout.LayoutParams.WRAP_CONTENT,
                imageHeight > 0 ? (int) imageHeight : LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;

        ivImage.setVisibility(View.GONE);
        ivImage.setLayoutParams(params);
    }

    private void createLoadingView() {
        loadingView = new LoadingIndicator(getContext());
        loadingView.setId(View.generateViewId());
        loadingView.setIndicatorColor(ThemeAttrs.primary(getContext()));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                loadingWidth > 0 ? (int) loadingWidth : LinearLayout.LayoutParams.WRAP_CONTENT,
                loadingHeight > 0 ? (int) loadingHeight : LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;

        loadingView.setVisibility(View.GONE);
        loadingView.setLayoutParams(params);
    }

    private void createTextView() {
        tvText = new MaterialTextView(getContext());
        tvText.setId(View.generateViewId());
        tvText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.leftMargin = (int) contentStartMargin;
        params.rightMargin = (int) contentEndMargin;

        tvText.setLayoutParams(params);
    }

    private void setupContainer() {
        // 根据排列方式添加View到容器
        if (imageTextArrangement == 0) {
            // 图片在上，文字在下
            containerView.addView(ivImage);
            containerView.addView(loadingView);
            containerView.addView(tvText);

            // 设置间距
            LinearLayout.LayoutParams imageParams = (LinearLayout.LayoutParams) ivImage.getLayoutParams();
            imageParams.bottomMargin = (int) imageTextMargin;

            LinearLayout.LayoutParams loadingParams = (LinearLayout.LayoutParams) loadingView.getLayoutParams();
            loadingParams.bottomMargin = (int) imageTextMargin;
        } else {
            // 文字在上，图片在下
            containerView.addView(tvText);
            containerView.addView(ivImage);
            containerView.addView(loadingView);

            // 设置间距
            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) tvText.getLayoutParams();
            textParams.bottomMargin = (int) imageTextMargin;

            LinearLayout.LayoutParams loadingParams = (LinearLayout.LayoutParams) loadingView.getLayoutParams();
            loadingParams.topMargin = (int) imageTextMargin; // loading覆盖在图片上，不需要额外间距
        }
    }

    /**
     * 全屏骨架屏层：与居中 Loading 容器互斥展示，懒创建后常驻以便复用行配置。
     */
    private void createSkeletonPanel() {
        skeletonPanel = new SkeletonShimmerPanel(getContext());
        skeletonPanel.setId(View.generateViewId());
        skeletonPanel.setVisibility(GONE);
        int padding = (int) skeletonPaddingPx;
        skeletonPanel.setPadding(padding, padding, padding, padding);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_CONSTRAINT,
                LayoutParams.MATCH_CONSTRAINT
        );
        addView(skeletonPanel, params);
    }

    private void applyConstraints() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        // 设置容器View的水平居中
        constraintSet.connect(containerView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (int) contentStartMargin);
        constraintSet.connect(containerView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (int) contentEndMargin);

        // 骨架屏铺满父布局
        if (skeletonPanel != null) {
            constraintSet.connect(skeletonPanel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(skeletonPanel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(skeletonPanel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(skeletonPanel.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }

        // 根据内容对齐方式设置容器的垂直位置
        switch (contentGravity) {
            case 1: // 顶部对齐
                constraintSet.connect(containerView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) contentTopMargin);
                break;
            case 2: // 底部对齐
                constraintSet.connect(containerView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (int) contentBottomMargin);
                break;
            default: // 居中对齐
                constraintSet.connect(containerView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(containerView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                break;
        }

        constraintSet.applyTo(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isLoading() && loadingStyle == LoadingStyle.DEFAULT
                && tvText != null && tvText.getVisibility() == VISIBLE) {
            applyLoadingTextAndMaybeAnimate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoadingDotAnim();
        super.onDetachedFromWindow();
    }

    private void setupClickListener() {
        setOnClickListener(v -> {
            if (clickEnable && onEmptyLayoutClickListener != null) {
                onEmptyLayoutClickListener.onEmptyLayoutClick(v);
            }
        });
    }

    // ==================== 公共方法 ====================

    public void setState(State state) {
        this.currentState = state;

        if (state == State.HIDE_LAYOUT) {
            hideSkeletonPanel();
            clearSkeletonLoadingOverlay();
            hideClassicLoadingContent();
            setVisibility(View.GONE);
            return;
        }

        setVisibility(View.VISIBLE);
        updateViewForState(state);
    }

    /** 停止并隐藏骨架屏（非 SKELETON 加载态、隐藏、错误、空态时调用）。 */
    private void hideSkeletonPanel() {
        if (skeletonPanel != null) {
            skeletonPanel.hideSkeleton();
        }
    }

    /** 骨架屏加载时铺满不透明背景，避免重试态透出下层列表/文案。 */
    private void applySkeletonLoadingOverlay() {
        if (!skeletonOverlayApplied) {
            overlayBackupBackground = getBackground();
            skeletonOverlayApplied = true;
        }
        setBackgroundColor(ThemeAttrs.surface(getContext()));
    }

    private void clearSkeletonLoadingOverlay() {
        if (!skeletonOverlayApplied) {
            return;
        }
        skeletonOverlayApplied = false;
        setBackground(overlayBackupBackground);
        overlayBackupBackground = null;
    }

    /**
     * 按状态解析图标宽/高：态专属 > 通用 imageWidth/Height > wrap_content。
     */
    private int resolveImageDimension(float specificSize, float fallbackSize) {
        if (specificSize > 0f) {
            return (int) specificSize;
        }
        if (fallbackSize > 0f) {
            return (int) fallbackSize;
        }
        return LinearLayout.LayoutParams.WRAP_CONTENT;
    }

    private void applyImageViewSize(float widthPx, float heightPx) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ivImage.getLayoutParams();
        lp.width = resolveImageDimension(widthPx, imageWidth);
        lp.height = resolveImageDimension(heightPx, imageHeight);
        ivImage.setLayoutParams(lp);
    }

    /** 隐藏转圈/图片/提示文字，骨架屏加载态不与经典 Loading 文案叠加。 */
    private void hideClassicLoadingContent() {
        resetLoadingDotAnim();
        loadingView.setVisibility(GONE);
        ivImage.setVisibility(GONE);
        tvText.setVisibility(GONE);
    }

    /** 是否正在使用骨架屏展示加载态。 */
    public boolean isSkeletonLoadingActive() {
        return loadingStyle == LoadingStyle.SKELETON
                && isLoading()
                && skeletonPanel != null
                && skeletonPanel.getVisibility() == View.VISIBLE;
    }

    private void updateViewForState(State state) {
        switch (state) {
            case LOADING_ERROR:
                setupErrorState();
                break;
            case NETWORK_LOADING:
            case NETWORK_LOADING_REFRESH:
                setupLoadingState(state);
                break;
            case NO_DATA:
                setupNoDataState(false);
                break;
            case NO_DATA_ENABLE_CLICK:
                setupNoDataState(true);
                break;
            default:
                break;
        }
    }

    private void setupErrorState() {
        resetLoadingDotAnim();
        clearSkeletonLoadingOverlay();
        hideSkeletonPanel();
        containerView.setVisibility(VISIBLE);
        applyImageViewSize(errorImageWidth, errorImageHeight);
        ivImage.setVisibility(View.VISIBLE);
        ivImage.setImageResource(errorImage);
        loadingView.setVisibility(View.GONE);

        tvText.setVisibility(VISIBLE);
        tvText.setText(errorText);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, errorTextSize);
        tvText.setTextColor(errorTextColor);
        tvText.setTypeface(tvText.getTypeface(), errorTextStyle == 1 ? Typeface.BOLD : Typeface.NORMAL);

        clickEnable = true;
    }

    private void setupLoadingState(State state) {
        if (loadingStyle == LoadingStyle.SKELETON) {
            hideClassicLoadingContent();
            applySkeletonLoadingOverlay();
            containerView.setVisibility(GONE);
            skeletonPanel.showSkeleton();
            clickEnable = state == State.NETWORK_LOADING_REFRESH;
            return;
        }

        clearSkeletonLoadingOverlay();
        hideSkeletonPanel();
        containerView.setVisibility(VISIBLE);
        ivImage.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        tvText.setVisibility(VISIBLE);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, loadingTextSize);
        tvText.setTextColor(loadingTextColor);
        tvText.setTypeface(tvText.getTypeface(), loadingTextStyle == 1 ? Typeface.BOLD : Typeface.NORMAL);
        applyLoadingTextAndMaybeAnimate();

        clickEnable = state == State.NETWORK_LOADING_REFRESH;
    }

    private void setupNoDataState(boolean clickable) {
        resetLoadingDotAnim();
        clearSkeletonLoadingOverlay();
        hideSkeletonPanel();
        containerView.setVisibility(VISIBLE);
        if (clickable) {
            applyImageViewSize(clickableNoDataImageWidth, clickableNoDataImageHeight);
            ivImage.setImageResource(clickableNoDataImage);
        } else {
            applyImageViewSize(noDataImageWidth, noDataImageHeight);
            ivImage.setImageResource(noDataImage);
        }
        ivImage.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);

        tvText.setVisibility(VISIBLE);
        if (!customNoDataContent.isEmpty()) {
            tvText.setText(customNoDataContent);
        } else {
            tvText.setText(clickable ? clickableNoDataText : noDataText);
        }

        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                clickable ? clickableNoDataTextSize : noDataTextSize);
        tvText.setTextColor(clickable ? clickableNoDataTextColor : noDataTextColor);
        tvText.setTypeface(tvText.getTypeface(),
                (clickable ? clickableNoDataTextStyle : noDataTextStyle) == 1 ? Typeface.BOLD : Typeface.NORMAL);

        clickEnable = true;
    }

    /**
     * 解析加载文案尾部的 {@code .} / {@code …}，有点号且开关开启时循环播放：
     * {@code .} → {@code ..} → {@code ...}（按原文点号个数）→ 再从头。
     * 尾部无点号则静态展示原文。
     */
    private void applyLoadingTextAndMaybeAnimate() {
        stopLoadingDotAnim();
        loadingDotMaxCount = 0;
        loadingDotCurrentCount = 0;
        loadingTextPrefix = "";
        if (tvText != null) {
            tvText.setMinWidth(0);
        }

        String text = getContext().getString(loadingText);
        if (text == null) {
            text = "";
        }
        if (!loadingDotAnimEnabled) {
            tvText.setText(text);
            return;
        }

        int end = text.length();
        int maxDots = 0;
        while (end > 0) {
            char c = text.charAt(end - 1);
            if (c == '.') {
                maxDots++;
                end--;
            } else if (c == HORIZONTAL_ELLIPSIS) {
                maxDots += 3;
                end--;
            } else {
                break;
            }
        }
        if (maxDots <= 0) {
            tvText.setText(text);
            return;
        }

        loadingTextPrefix = text.substring(0, end);
        loadingDotMaxCount = maxDots;
        loadingDotCurrentCount = 1;
        float fullWidth = tvText.getPaint().measureText(loadingTextPrefix + ".".repeat(maxDots));
        tvText.setMinWidth((int) Math.ceil(fullWidth));
        tvText.setText(loadingTextPrefix + ".".repeat(loadingDotCurrentCount));
        scheduleNextLoadingDot();
    }

    private void tickLoadingDots() {
        if (!canRunLoadingDotAnim()) {
            stopLoadingDotAnim();
            return;
        }
        loadingDotCurrentCount++;
        if (loadingDotCurrentCount > loadingDotMaxCount) {
            loadingDotCurrentCount = 1;
        }
        tvText.setText(loadingTextPrefix + ".".repeat(loadingDotCurrentCount));
        scheduleNextLoadingDot();
    }

    private void scheduleNextLoadingDot() {
        if (!canRunLoadingDotAnim()) {
            return;
        }
        tvText.removeCallbacks(loadingDotTicker);
        tvText.postDelayed(loadingDotTicker, LOADING_DOT_INTERVAL_MS);
    }

    private boolean canRunLoadingDotAnim() {
        return loadingDotAnimEnabled
                && loadingDotMaxCount > 0
                && isLoading()
                && loadingStyle == LoadingStyle.DEFAULT
                && getVisibility() == VISIBLE
                && tvText != null
                && tvText.getVisibility() == VISIBLE;
    }

    private void stopLoadingDotAnim() {
        if (tvText != null) {
            tvText.removeCallbacks(loadingDotTicker);
        }
    }

    private void resetLoadingDotAnim() {
        stopLoadingDotAnim();
        loadingDotMaxCount = 0;
        loadingDotCurrentCount = 0;
        loadingTextPrefix = "";
        if (tvText != null) {
            tvText.setMinWidth(0);
        }
    }

    public void dismiss() {
        setState(State.HIDE_LAYOUT);
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isLoadError() {
        return currentState == State.LOADING_ERROR;
    }

    public boolean isLoading() {
        return currentState == State.NETWORK_LOADING ||
                currentState == State.NETWORK_LOADING_REFRESH;
    }

    public void setOnEmptyLayoutClickListener(OnEmptyLayoutClickListener listener) {
        this.onEmptyLayoutClickListener = listener;
    }

    public void setErrorMessage(String msg) {
        tvText.setText(msg);
    }

    public void setErrorImage(@DrawableRes int resId) {
        this.errorImage = resId;
        if (currentState == State.LOADING_ERROR) {
            applyImageViewSize(errorImageWidth, errorImageHeight);
            ivImage.setImageResource(resId);
        }
    }

    public void setNoDataContent(String content) {
        this.customNoDataContent = content;
        if (currentState == State.NO_DATA || currentState == State.NO_DATA_ENABLE_CLICK) {
            tvText.setText(content);
        }
    }

    // ==================== Setter方法（用于配置）====================

    public void setErrorImageRes(@DrawableRes int resId) {
        this.errorImage = resId;
    }

    public void setLoadingImageRes(@DrawableRes int resId) {
        this.loadingImage = resId;
    }

    public void setNoDataImageRes(@DrawableRes int resId) {
        this.noDataImage = resId;
    }

    public void setClickableNoDataImageRes(@DrawableRes int resId) {
        this.clickableNoDataImage = resId;
    }

    /**
     * 一次性覆盖本实例的空态图标，不改进程级 {@link EmptyLayoutConfig}。
     * 若要连同文案 / 颜色 / 字号一起覆盖，请用 {@link #setConfig(EmptyLayoutConfig)}。
     */
    public EmptyLayout setImageConfig(@NonNull EmptyLayoutConfig config) {
        this.errorImage = config.getErrorImageRes();
        this.loadingImage = config.getLoadingImageRes();
        this.noDataImage = config.getNoDataImageRes();
        this.clickableNoDataImage = config.getClickableNoDataImageRes();
        return this;
    }

    /**
     * 一次性覆盖本实例的图标、文案、颜色、字号、字重，不改进程级 {@link EmptyLayoutConfig}。
     * 若要基于全局再改某一项，可 {@code Config.getInstance().getEmptyLayoutConfig().copy().setNoDataTextRes(...)}。
     */
    public EmptyLayout setConfig(@NonNull EmptyLayoutConfig config) {
        applyConfigValues(config);
        return this;
    }

    public EmptyLayout setErrorImageSize(int widthPx, int heightPx) {
        this.errorImageWidth = widthPx;
        this.errorImageHeight = heightPx;
        if (currentState == State.LOADING_ERROR) {
            applyImageViewSize(errorImageWidth, errorImageHeight);
        }
        return this;
    }

    public EmptyLayout setLoadingImageSize(int widthPx, int heightPx) {
        this.loadingImageWidth = widthPx;
        this.loadingImageHeight = heightPx;
        return this;
    }

    public EmptyLayout setNoDataImageSize(int widthPx, int heightPx) {
        this.noDataImageWidth = widthPx;
        this.noDataImageHeight = heightPx;
        if (currentState == State.NO_DATA) {
            applyImageViewSize(noDataImageWidth, noDataImageHeight);
        }
        return this;
    }

    public EmptyLayout setClickableNoDataImageSize(int widthPx, int heightPx) {
        this.clickableNoDataImageWidth = widthPx;
        this.clickableNoDataImageHeight = heightPx;
        if (currentState == State.NO_DATA_ENABLE_CLICK) {
            applyImageViewSize(clickableNoDataImageWidth, clickableNoDataImageHeight);
        }
        return this;
    }

    public void setErrorTextRes(@StringRes int resId) {
        this.errorText = resId;
    }

    public void setLoadingTextRes(@StringRes int resId) {
        this.loadingText = resId;
        if (isLoading() && loadingStyle == LoadingStyle.DEFAULT) {
            applyLoadingTextAndMaybeAnimate();
        }
    }

    public void setNoDataTextRes(@StringRes int resId) {
        this.noDataText = resId;
    }

    public void setClickableNoDataTextRes(@StringRes int resId) {
        this.clickableNoDataText = resId;
    }

    public void setErrorTextColor(int color) {
        this.errorTextColor = color;
    }

    public void setLoadingTextColor(int color) {
        this.loadingTextColor = color;
    }

    public void setNoDataTextColor(int color) {
        this.noDataTextColor = color;
    }

    public void setClickableNoDataTextColor(int color) {
        this.clickableNoDataTextColor = color;
    }

    // ==================== 加载样式 / 骨架屏配置 ====================

    public LoadingStyle getLoadingStyle() {
        return loadingStyle;
    }

    /**
     * 切换加载态样式。默认 {@link LoadingStyle#DEFAULT}，旧代码无需调用。
     */
    public EmptyLayout setLoadingStyle(@NonNull LoadingStyle loadingStyle) {
        this.loadingStyle = loadingStyle;
        if (isLoading()) {
            updateViewForState(currentState);
        }
        return this;
    }

    /** 便捷方法：是否使用骨架屏加载。 */
    public EmptyLayout setSkeletonLoadingEnabled(boolean enabled) {
        return setLoadingStyle(enabled ? LoadingStyle.SKELETON : LoadingStyle.DEFAULT);
    }

    public boolean isSkeletonLoadingEnabled() {
        return loadingStyle == LoadingStyle.SKELETON;
    }

    /**
     * 是否循环播放加载文案尾部的 {@code .} / {@code …}。默认 {@code true}；
     * 文案尾部没有点号时即使开启也不会播放。
     */
    public EmptyLayout setLoadingDotAnimEnabled(boolean enabled) {
        this.loadingDotAnimEnabled = enabled;
        if (isLoading() && loadingStyle == LoadingStyle.DEFAULT) {
            applyLoadingTextAndMaybeAnimate();
        } else if (!enabled) {
            stopLoadingDotAnim();
        }
        return this;
    }

    public boolean isLoadingDotAnimEnabled() {
        return loadingDotAnimEnabled;
    }

    public EmptyLayout setSkeletonRowCount(int rowCount) {
        if (skeletonPanel != null) {
            skeletonPanel.setRowCount(rowCount);
        }
        return this;
    }

    /** 当前骨架屏占位行数（未创建面板时返回 XML/代码最近一次配置，默认 4）。 */
    public int getSkeletonRowCount() {
        return skeletonPanel != null ? skeletonPanel.getRowCount() : 4;
    }

    public EmptyLayout setSkeletonRowHeight(int rowHeightPx) {
        if (skeletonPanel != null) {
            skeletonPanel.setRowHeight(rowHeightPx);
        }
        return this;
    }

    public EmptyLayout setSkeletonShimmerEnabled(boolean enabled) {
        if (skeletonPanel != null) {
            skeletonPanel.setShimmerEnabled(enabled);
        }
        return this;
    }

    public interface OnEmptyLayoutClickListener {
        void onEmptyLayoutClick(View v);
    }
}
