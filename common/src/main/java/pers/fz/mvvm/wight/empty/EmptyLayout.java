package pers.fz.mvvm.wight.empty;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import pers.fz.mvvm.R;

import net.qiujuer.genius.ui.widget.Loading;

public class EmptyLayout extends LinearLayout {

    public static final int HIDE_LAYOUT = 4;
    public static final int LOADING_ERROR = 1;
    public static final int NETWORK_LOADING = 2;
    public static final int NODATA = 3;
    public static final int NODATA_ENABLE_CLICK = 10;
    public static final int NETWORK_LOADING_RERESH = 5;
    public static final int NETWORK_LOADING_LOADMORE = 6;
    private Loading mLoading;
    private boolean clickEnable = true;
    private final Context context;
    public ImageView img;
    private OnEmptyLayoutClickListener onEmptyLayoutClickListener;
    private int mErrorState;
    private String strNoDataContent = "";
    private TextView tv;

    public EmptyLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_error_layout, this, false);
        img = (ImageView) view.findViewById(R.id.img_error_layout);
        tv = (TextView) view.findViewById(R.id.tv_error_layout);
        mLoading = (Loading) view.findViewById(R.id.animProgress);
        setBackgroundColor(-1);
//        setOnClickListener(this);
        img.setOnClickListener(v -> {
            if (clickEnable) {
                if (onEmptyLayoutClickListener != null) {
                    onEmptyLayoutClickListener.onEmptyLayoutClick(v);
                }
            }
        });
        addView(view);
        changeErrorLayoutBgMode(context);
    }

    public void changeErrorLayoutBgMode(Context context1) {
        // mLayout.setBackgroundColor(SkinsUtil.getColor(context1,
        // "bgcolor01"));
        // tv.setTextColor(SkinsUtil.getColor(context1, "textcolor05"));
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // ApplicationHelper.getInstance().getAtSkinObserable().registered(this);
        onSkinChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // ApplicationHelper.getInstance().getAtSkinObserable().unregistered(this);
    }

    public void setOnEmptyLayoutClickListener(OnEmptyLayoutClickListener onEmptyLayoutClickListener) {
        this.onEmptyLayoutClickListener = onEmptyLayoutClickListener;
    }

    public interface OnEmptyLayoutClickListener {
        void onEmptyLayoutClick(View v);
    }

    public void onSkinChanged() {
        // mLayout.setBackgroundColor(SkinsUtil
        // .getColor(getContext(), "bgcolor01"));
        // tv.setTextColor(SkinsUtil.getColor(getContext(), "textcolor05"));
    }

    public void setErrorMessage(String msg) {
        tv.setText(msg);
    }

    /**
     * 新添设置背景
     */
    public void setErrorImag(int imgResource) {
        try {
            img.setImageResource(imgResource);
        } catch (Exception e) {
        }
    }

    public void setErrorType(int i) {
        setVisibility(View.VISIBLE);
        switch (i) {
            case LOADING_ERROR:
                mErrorState = LOADING_ERROR;
                img.setVisibility(View.VISIBLE);
                setErrorImag(R.mipmap.loading_error);
                tv.setText(R.string.state_load_error);
                mLoading.stop();
                mLoading.setVisibility(View.GONE);
                clickEnable = true;
                break;
            case NETWORK_LOADING:
            case NETWORK_LOADING_RERESH:
                mErrorState = i == NETWORK_LOADING ? NETWORK_LOADING : NETWORK_LOADING_RERESH;
                setErrorImag(R.mipmap.icon_loading_again);
                mLoading.setVisibility(View.VISIBLE);
                mLoading.start();
                img.setVisibility(View.GONE);
                tv.setText(R.string.state_loading);
                clickEnable = false;
                break;
            case NODATA:
                mErrorState = NODATA;
                setErrorImag(R.mipmap.page_icon_empty);
                img.setVisibility(View.VISIBLE);
                mLoading.stop();
                mLoading.setVisibility(View.GONE);
                setTvNoDataContent();
                clickEnable = true;
                break;
            case HIDE_LAYOUT:
                mLoading.stop();
                setVisibility(View.GONE);
                break;
            case NODATA_ENABLE_CLICK:
                mErrorState = NODATA_ENABLE_CLICK;
                setErrorImag(R.mipmap.icon_loading_again);
                img.setVisibility(View.VISIBLE);
                mLoading.stop();
                mLoading.setVisibility(View.GONE);
                setTvNoDataContent();
                tv.setText(R.string.state_loading_again);
                clickEnable = true;
                break;
            default:
                break;
        }
    }

    public void setNoDataContent(String noDataContent) {
        strNoDataContent = noDataContent;
    }

    public void setTvNoDataContent() {
        if (!strNoDataContent.equals("")) {
            tv.setText(strNoDataContent);
        } else {
            tv.setText(R.string.noData);
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
