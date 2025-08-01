package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FileShowAdapter;
import com.casic.titan.commonui.bean.AttachmentBean;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.common.CollectionUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormFileShow extends FormMedia {
    /**
     * 适配器
     */
    protected BaseRecyclerViewAdapter<?, ?> adapter;
    /**
     * 空白暂无数据控件
     */
    protected AppCompatTextView tvEmpty;

    /**
     * 空白暂无数据文字颜色
     */
    protected int emptyTextColor;
    /**
     * 空白暂无数据文字大小
     */
    protected float emptyTextSize;
    /**
     * 空白暂无数据文字内容
     */
    protected String emptyText = "暂无附件";
    /**
     * 列表中文字颜色
     */
    protected int itemTextColor;
    /**
     * 文件图标
     */
    protected Drawable fileDrawable;
    /**
     * 是否展示文件图标
     */
    protected boolean isShowFileDrawable = true;

    public FormFileShow(@NonNull Context context) {
        super(context);
    }

    public FormFileShow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormFileShow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            emptyTextColor = typedArray.getColor(R.styleable.FormUI_emptyTextColor, ContextCompat.getColor(getContext(), R.color.dark_color));
            fileDrawable = typedArray.getDrawable(R.styleable.FormUI_fileDrawable);
            emptyTextSize = typedArray.getDimension(R.styleable.FormUI_emptyTextSize, DensityUtil.sp2px(getContext(), 14));
            emptyText = typedArray.getString(R.styleable.FormUI_emptyText);
            isShowFileDrawable = typedArray.getBoolean(R.styleable.FormUI_isShowFileDrawable, true);
            if (TextUtils.isEmpty(emptyText)) {
                emptyText = "暂无附件";
            }
            itemTextColor = typedArray.getColor(R.styleable.FormUI_itemTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
        } else {
            itemTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            emptyTextColor = ContextCompat.getColor(getContext(), R.color.dark_color);
            emptyTextSize = DensityUtil.sp2px(getContext(), 14);
            emptyText = "暂无附件";
            isShowFileDrawable = true;
        }
        if (fileDrawable == null) {
            fileDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.icon_file);
        }
    }


    @Override
    protected void init() {
        super.init();
        createEmptyLayout();
        layoutEmptyLayout();
        if (adapter == null) {
            adapter = new FileShowAdapter();
            FileShowAdapter fileShowAdapter = (FileShowAdapter) adapter;
            fileShowAdapter.setRadius(radius);
            fileShowAdapter.setBgColor(bgColor);
            fileShowAdapter.setTextColor(itemTextColor);
            fileShowAdapter.setFileDrawable(fileDrawable);
            fileShowAdapter.setShowFileDrawable(isShowFileDrawable);
        }
        mediaRecyclerView.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, (int) columnMargin,
                        0x00000000)
        );
        mediaRecyclerView.setLayoutManager(new FullyLinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mediaRecyclerView.setAdapter(adapter);
    }

    public void createEmptyLayout() {
        tvEmpty = new AppCompatTextView(getContext());
        tvEmpty.setId(View.generateViewId());
        tvEmpty.setTextColor(emptyTextColor);
        tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_PX, emptyTextSize);
        tvEmpty.setText(emptyText);
        tvEmpty.setGravity(Gravity.CENTER);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, DensityUtil.dp2px(getContext(), 60f));
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
    public <T extends AttachmentBean> void setFiles(List<AttachmentBean> images) {
        if (adapter instanceof FileShowAdapter fileShowAdapter) {
            fileShowAdapter.setList(images);
            fileShowAdapter.notifyDataSetChanged();
            mediaRecyclerView.setVisibility((CollectionUtil.isEmpty(images)) ? View.GONE : View.VISIBLE);
            tvEmpty.setVisibility((CollectionUtil.isEmpty(images)) ? View.VISIBLE : View.GONE);
        }
    }

}
