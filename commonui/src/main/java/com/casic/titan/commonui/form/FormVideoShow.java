package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;

import pers.fz.mvvm.adapter.VideoShowAdapter;
import pers.fz.mvvm.bean.AttachmentBean;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.common.CollectionUtil;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormVideoShow extends FormMedia {
    /**
     * 适配器
     */
    private BaseRecyclerViewAdapter<?, ?> adapter;

    /**
     * 暂无数据控件
     */
    protected AppCompatTextView tvEmpty;

    /**
     * 暂无数据控件文字颜色
     */
    private int emptyTextColor;
    /**
     * 暂无数据控件文字大小
     */
    private float emptyTextSize;
    /**
     * 暂无数据控件文字内容
     */
    private String emptyText = "暂无附件";

    public FormVideoShow(@NonNull Context context) {
        super(context);
    }

    public FormVideoShow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormVideoShow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            emptyTextColor = typedArray.getColor(R.styleable.FormUI_emptyTextColor, ContextCompat.getColor(getContext(), R.color.dark_color));
            emptyTextSize = typedArray.getDimension(R.styleable.FormUI_emptyTextSize, DensityUtil.sp2px(getContext(), 14));
            emptyText = typedArray.getString(R.styleable.FormUI_emptyText);
            if (TextUtils.isEmpty(emptyText)) {
                emptyText = "暂无附件";
            }
        } else {
            emptyTextColor = ContextCompat.getColor(getContext(), R.color.dark_color);
            emptyTextSize = DensityUtil.sp2px(getContext(), 14);
            emptyText = "暂无附件";
        }
    }

    @Override
    protected void init() {
        super.init();
        createEmptyLayout();
        layoutEmptyLayout();
        mediaRecyclerView.setVisibility(View.GONE);
        if (adapter == null) {
            adapter = new VideoShowAdapter();
            ((VideoShowAdapter) adapter).setRadius(radius);
            ((VideoShowAdapter) adapter).setErrorImage(errorImage);
            ((VideoShowAdapter) adapter).setPlaceholderImage(placeholderImage);
        }
        mediaRecyclerView.setAdapter(adapter);
    }

    @Override
    public String[] defaultFileType() {
        return new String[]{"*/*"};
    }

    public AppCompatTextView getTvEmpty() {
        return tvEmpty;
    }

    public void createEmptyLayout() {
        tvEmpty = new AppCompatTextView(getContext());
        tvEmpty.setId(View.generateViewId());
        tvEmpty.setTextColor(emptyTextColor);
        tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, emptyTextSize);
        tvEmpty.setText(emptyText);
        tvEmpty.setGravity(Gravity.CENTER);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, DensityUtil.dp2px(getContext(),60f));
        params.setMarginStart((int) textEndMargin);
        params.setMarginEnd((int) textEndMargin);
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        addView(tvEmpty, params);
    }

    public void layoutEmptyLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(tvEmpty.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    public <AD extends BaseRecyclerViewAdapter<?, ?>> void setAdapter(AD adapter) {
        this.adapter = adapter;
        mediaRecyclerView.setAdapter(adapter);
    }

    public BaseRecyclerViewAdapter<?, ?> getAdapter() {
        return adapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public <T extends AttachmentBean> void setVideos(List<T> images) {
        if (adapter instanceof VideoShowAdapter formVideoShowAdapter) {
            formVideoShowAdapter.setList((List<AttachmentBean>) images);
            formVideoShowAdapter.notifyDataSetChanged();
            mediaRecyclerView.setVisibility((CollectionUtil.isEmpty(images))?View.GONE:View.VISIBLE);
            tvEmpty.setVisibility((CollectionUtil.isEmpty(images))?View.VISIBLE:View.GONE);
        }
    }


}
