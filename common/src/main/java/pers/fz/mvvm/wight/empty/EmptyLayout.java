package pers.fz.mvvm.wight.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.ViewErrorLayoutBinding;

public class EmptyLayout extends LinearLayout {

    public static final int HIDE_LAYOUT = 4;
    public static final int LOADING_ERROR = 1;
    public static final int NETWORK_LOADING = 2;
    public static final int NO_DATA = 3;
    public static final int NO_DATA_ENABLE_CLICK = 10;
    public static final int NETWORK_LOADING_REFRESH = 5;
    private boolean clickEnable = true;
    private OnEmptyLayoutClickListener onEmptyLayoutClickListener;
    private int mErrorState;
    private String strNoDataContent = "";
    private ViewErrorLayoutBinding binding;

    public EmptyLayout(Context context) {
        super(context);
        init();
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.default_background));
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        binding = ViewErrorLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.imgErrorLayout.setOnClickListener(v -> {
            if (!clickEnable) {
               return;
            }
            if (onEmptyLayoutClickListener != null) {
                onEmptyLayoutClickListener.onEmptyLayoutClick(v);
            }
        });
    }

    public ViewErrorLayoutBinding getBinding() {
        return binding;
    }

    public void dismiss() {
        mErrorState = HIDE_LAYOUT;
        setVisibility(View.GONE);
    }

    public int getErrorState() {
        return mErrorState;
    }

    public boolean isLoadError() {
        return mErrorState == LOADING_ERROR;
    }

    public boolean isLoading() {
        return mErrorState == NETWORK_LOADING;
    }

    public void setOnEmptyLayoutClickListener(OnEmptyLayoutClickListener onEmptyLayoutClickListener) {
        this.onEmptyLayoutClickListener = onEmptyLayoutClickListener;
    }

    public interface OnEmptyLayoutClickListener {
        void onEmptyLayoutClick(View v);
    }

    public void setErrorMessage(String msg) {
        binding.tvErrorLayout.setText(msg);
    }

    /**
     * 新添设置背景
     */
    public void setErrorImage(@DrawableRes int imgResource) {
        binding.imgErrorLayout.setImageResource(imgResource);
    }

    public void setErrorType(int i) {
        setVisibility(View.VISIBLE);
        switch (i) {
            case LOADING_ERROR -> {
                mErrorState = LOADING_ERROR;
                binding.imgErrorLayout.setVisibility(View.VISIBLE);
                setErrorImage(R.mipmap.loading_error);
                binding.tvErrorLayout.setText(R.string.state_load_error);
                binding.loading.stop();
                binding.loading.setVisibility(View.GONE);
                clickEnable = true;
            }
            case NETWORK_LOADING, NETWORK_LOADING_REFRESH -> {
                mErrorState = i == NETWORK_LOADING ? NETWORK_LOADING : NETWORK_LOADING_REFRESH;
                setErrorImage(R.mipmap.icon_loading_again);
                binding.loading.setVisibility(View.VISIBLE);
                binding.loading.start();
                binding.imgErrorLayout.setVisibility(View.GONE);
                binding.tvErrorLayout.setText(R.string.state_loading);
                clickEnable = false;
            }
            case NO_DATA -> {
                mErrorState = NO_DATA;
                setErrorImage(R.mipmap.page_icon_empty);
                binding.imgErrorLayout.setVisibility(View.VISIBLE);
                binding.loading.stop();
                binding.loading.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;
            }
            case HIDE_LAYOUT -> {
                binding.loading.stop();
                setVisibility(View.GONE);
            }
            case NO_DATA_ENABLE_CLICK -> {
                mErrorState = NO_DATA_ENABLE_CLICK;
                setErrorImage(R.mipmap.icon_loading_again);
                binding.imgErrorLayout.setVisibility(View.VISIBLE);
                binding.loading.stop();
                binding.loading.setVisibility(View.GONE);
                setTvNoDataContent();
                binding.tvErrorLayout.setText(R.string.state_loading_again);
                clickEnable = true;
            }
            default -> {
            }
        }
    }

    public void setNoDataContent(String noDataContent) {
        strNoDataContent = noDataContent;
    }

    public void setTvNoDataContent() {
        if (!strNoDataContent.isEmpty()) {
            binding.tvErrorLayout.setText(strNoDataContent);
        } else {
            binding.tvErrorLayout.setText(R.string.noData);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mErrorState = HIDE_LAYOUT;
        }
        super.setVisibility(visibility);
    }
}
