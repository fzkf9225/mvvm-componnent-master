package io.coderf.arklab.common.widget.empty;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

import net.qiujuer.genius.ui.widget.Loading;

/**
 * 可配置的空白占位View，支持加载中、加载失败、无数据等状态
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2025/12/09 16:30
 * @update 2026/3/10 19:25
 */
public class EmptyLayout extends ConstraintLayout {

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

    // ==================== 图片资源 ====================
    @DrawableRes private int errorImage = R.mipmap.loading_error;
    @DrawableRes private int loadingImage = R.mipmap.icon_loading_again;
    @DrawableRes private int noDataImage = R.mipmap.page_icon_empty;
    @DrawableRes private int clickableNoDataImage = R.mipmap.icon_loading_again;

    // ==================== 文字资源 ====================
    @StringRes private int errorText = R.string.state_load_error;
    @StringRes private int loadingText = R.string.state_loading;
    @StringRes private int noDataText = R.string.noData;
    @StringRes private int clickableNoDataText = R.string.state_loading_again;

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

    // ==================== Loading尺寸 ====================
    private float loadingWidth;
    private float loadingHeight;

    // ==================== 控件引用 ====================
    private LinearLayout containerView;  // 中间容器层
    private AppCompatImageView ivImage;
    private Loading loadingView;
    private AppCompatTextView tvText;

    public EmptyLayout(@NonNull Context context) {
        this(context, null);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        initViews();
        setupClickListener();
    }

    private void initAttr(AttributeSet attrs) {
        // 默认值初始化
        errorTextColor = ContextCompat.getColor(getContext(), R.color.gray);
        loadingTextColor = ContextCompat.getColor(getContext(), R.color.gray);
        noDataTextColor = ContextCompat.getColor(getContext(), R.color.gray);
        clickableNoDataTextColor = ContextCompat.getColor(getContext(), R.color.nv_bg_color);

        errorTextSize = DensityUtil.sp2px(getContext(), 14);
        loadingTextSize = DensityUtil.sp2px(getContext(), 14);
        noDataTextSize = DensityUtil.sp2px(getContext(), 14);
        clickableNoDataTextSize = DensityUtil.sp2px(getContext(), 14);

        errorTextStyle = 0;
        loadingTextStyle = 0;
        noDataTextStyle = 0;
        clickableNoDataTextStyle = 0;

        contentGravity = 0;
        imageTextArrangement = 0;

        contentStartMargin = 0;
        contentEndMargin = 0;
        contentTopMargin = DensityUtil.dp2px(getContext(), 20);
        contentBottomMargin = DensityUtil.dp2px(getContext(), 20);
        imageTextMargin = DensityUtil.dp2px(getContext(), 16);

        loadingWidth = DensityUtil.dp2px(getContext(), 32);
        loadingHeight = DensityUtil.dp2px(getContext(), 32);

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
            loadingWidth = a.getDimension(R.styleable.EmptyLayout_loadingWidth, loadingWidth);
            loadingHeight = a.getDimension(R.styleable.EmptyLayout_loadingHeight, loadingHeight);

            a.recycle();
        }
    }

    public AppCompatTextView getTvText() {
        return tvText;
    }

    public Loading getLoadingView() {
        return loadingView;
    }

    public AppCompatImageView getIvImage() {
        return ivImage;
    }

    public LinearLayout getContainerView() {
        return containerView;
    }

    private void initViews() {
        // 创建容器View（LinearLayout）
        createContainer();

        // 创建图片View
        createImageView();

        // 创建LoadingView
        createLoadingView();

        // 创建文字View
        createTextView();

        // 将所有View添加到容器中
        setupContainer();

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
        ivImage = new AppCompatImageView(getContext());
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
        loadingView = new Loading(getContext());
        loadingView.setId(View.generateViewId());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                loadingWidth > 0 ? (int) loadingWidth : LinearLayout.LayoutParams.WRAP_CONTENT,
                loadingHeight > 0 ? (int) loadingHeight : LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;

        loadingView.setVisibility(View.GONE);
        loadingView.setLayoutParams(params);
    }

    private void createTextView() {
        tvText = new AppCompatTextView(getContext());
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

    private void applyConstraints() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        // 设置容器View的水平居中
        constraintSet.connect(containerView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (int) contentStartMargin);
        constraintSet.connect(containerView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (int) contentEndMargin);

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
            setVisibility(View.GONE);
            return;
        }

        setVisibility(View.VISIBLE);
        updateViewForState(state);
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
        ivImage.setVisibility(View.VISIBLE);
        ivImage.setImageResource(errorImage);
        loadingView.setVisibility(View.GONE);
        loadingView.stop();

        tvText.setText(errorText);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, errorTextSize);
        tvText.setTextColor(errorTextColor);
        tvText.setTypeface(tvText.getTypeface(), errorTextStyle == 1 ? Typeface.BOLD : Typeface.NORMAL);

        clickEnable = true;
    }

    private void setupLoadingState(State state) {
        ivImage.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        loadingView.start();

        tvText.setText(loadingText);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, loadingTextSize);
        tvText.setTextColor(loadingTextColor);
        tvText.setTypeface(tvText.getTypeface(), loadingTextStyle == 1 ? Typeface.BOLD : Typeface.NORMAL);

        clickEnable = state == State.NETWORK_LOADING_REFRESH;
    }

    private void setupNoDataState(boolean clickable) {
        ivImage.setVisibility(View.VISIBLE);
        ivImage.setImageResource(clickable ? clickableNoDataImage : noDataImage);
        loadingView.setVisibility(View.GONE);
        loadingView.stop();

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

    public void setErrorTextRes(@StringRes int resId) {
        this.errorText = resId;
    }

    public void setLoadingTextRes(@StringRes int resId) {
        this.loadingText = resId;
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

    public interface OnEmptyLayoutClickListener {
        void onEmptyLayoutClick(View v);
    }
}