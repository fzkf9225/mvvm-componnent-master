package pers.fz.mvvm.wight.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.ViewErrorLayoutBinding;

public class EmptyLayout extends LinearLayout {

    public enum State {
        /**
         * 隐藏布局
         */
        HIDE_LAYOUT,
        /**
         * 错误
         */
        LOADING_ERROR,
        /**
         * 加载中
         */
        NETWORK_LOADING,
        /**
         * 暂无数据
         */
        NO_DATA,
        /**
         * 暂无数据，点击重试
         */
        NO_DATA_ENABLE_CLICK,
        /**
         * 加载中，点击重试
         */
        NETWORK_LOADING_REFRESH
    }

    private boolean clickEnable = true;
    private OnEmptyLayoutClickListener onEmptyLayoutClickListener;
    private State currentState;
    private String customNoDataContent = "";

    // 默认资源
    @DrawableRes private int defaultErrorImage = R.mipmap.loading_error;
    @DrawableRes private int defaultLoadingImage = R.mipmap.icon_loading_again;
    @DrawableRes private int defaultNoDataImage = R.mipmap.page_icon_empty;
    @StringRes private int defaultErrorText = R.string.state_load_error;
    @StringRes private int defaultLoadingText = R.string.state_loading;
    @StringRes private int defaultNoDataText = R.string.noData;
    @StringRes private int defaultClickableNoDataText = R.string.state_loading_again;

    private final ViewErrorLayoutBinding binding;

    public EmptyLayout(Context context) {
        this(context, null);
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        binding = ViewErrorLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setupClickListener();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.default_background));
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void setupClickListener() {
        binding.imgErrorLayout.setOnClickListener(v -> {
            if (clickEnable && onEmptyLayoutClickListener != null) {
                onEmptyLayoutClickListener.onEmptyLayoutClick(v);
            }
        });
    }

    public ViewErrorLayoutBinding getBinding() {
        return binding;
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
        binding.tvErrorLayout.setText(msg);
    }

    public void setErrorImage(@DrawableRes int imgResource) {
        binding.imgErrorLayout.setImageResource(imgResource);
    }

    public void setState(State state) {
        this.currentState = state;

        if (state == State.HIDE_LAYOUT) {
            hide();
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
        setErrorImage(defaultErrorImage);
        binding.imgErrorLayout.setVisibility(View.VISIBLE);
        binding.tvErrorLayout.setText(defaultErrorText);
        stopLoading();
        clickEnable = true;
    }

    private void setupLoadingState(State state) {
        setErrorImage(defaultLoadingImage);
        binding.loading.setVisibility(View.VISIBLE);
        binding.loading.start();
        binding.imgErrorLayout.setVisibility(View.GONE);
        binding.tvErrorLayout.setText(defaultLoadingText);
        clickEnable = false;
    }

    private void setupNoDataState(boolean clickable) {
        setErrorImage(clickable ? defaultLoadingImage : defaultNoDataImage);
        binding.imgErrorLayout.setVisibility(View.VISIBLE);
        stopLoading();
        setNoDataContentText(clickable);
        clickEnable = true;
    }

    private void setNoDataContentText(boolean clickable) {
        if (!customNoDataContent.isEmpty()) {
            binding.tvErrorLayout.setText(customNoDataContent);
        } else {
            binding.tvErrorLayout.setText(clickable ? defaultClickableNoDataText : defaultNoDataText);
        }
    }

    private void stopLoading() {
        binding.loading.stop();
        binding.loading.setVisibility(View.GONE);
    }

    private void hide() {
        stopLoading();
        setVisibility(View.GONE);
    }

    public void setNoDataContent(String noDataContent) {
        this.customNoDataContent = noDataContent;
    }

    // 设置默认资源的方法
    public void setDefaultErrorImage(@DrawableRes int resId) {
        this.defaultErrorImage = resId;
    }

    public void setDefaultLoadingImage(@DrawableRes int resId) {
        this.defaultLoadingImage = resId;
    }

    public void setDefaultNoDataImage(@DrawableRes int resId) {
        this.defaultNoDataImage = resId;
    }

    public void setDefaultErrorText(@StringRes int resId) {
        this.defaultErrorText = resId;
    }

    public void setDefaultLoadingText(@StringRes int resId) {
        this.defaultLoadingText = resId;
    }

    public void setDefaultNoDataText(@StringRes int resId) {
        this.defaultNoDataText = resId;
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            currentState = State.HIDE_LAYOUT;
        }
        super.setVisibility(visibility);
    }

    public interface OnEmptyLayoutClickListener {
        void onEmptyLayoutClick(View v);
    }
}