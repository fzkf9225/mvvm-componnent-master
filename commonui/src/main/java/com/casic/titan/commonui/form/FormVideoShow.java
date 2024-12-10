package com.casic.titan.commonui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FormVideoShowAdapter;
import com.casic.titan.commonui.bean.AttachmentBean;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager;
import pers.fz.mvvm.wight.recyclerview.GridSpacingItemDecoration;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormVideoShow extends ConstraintLayout {
    protected String labelString;
    protected int bgColor;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected TextView tvLabel, tvRequired;
    protected RecyclerView mRecyclerViewImage;
    private BaseRecyclerViewAdapter<?, ?> adapter;
    private float formLabelTextSize;
    private float formRequiredSize;

    public FormVideoShow(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormVideoShow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormVideoShow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormImage);
            labelString = typedArray.getString(R.styleable.FormImage_label);
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormImage_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormImage_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            typedArray.recycle();
        } else {
            bgColor = 0xFFF1F3F2;
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.form_image, this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(getPaddingStart(), DensityUtil.dp2px(getContext(), 12),
                getPaddingEnd(), DensityUtil.dp2px(getContext(), 12));
        tvLabel = findViewById(R.id.tv_label);
        mRecyclerViewImage = findViewById(R.id.mRecyclerViewImage);
        tvRequired = findViewById(R.id.tv_required);
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvLabel.setText(labelString);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        ConstraintLayout.LayoutParams imageLayoutParams = (LayoutParams) mRecyclerViewImage.getLayoutParams();
        imageLayoutParams.topMargin = DensityUtil.dp2px(getContext(), 12);
        mRecyclerViewImage.setLayoutParams(imageLayoutParams);
        if (adapter == null) {
            adapter = new FormVideoShowAdapter();
        }
        mRecyclerViewImage.addItemDecoration(new GridSpacingItemDecoration(DensityUtil.dp2px(getContext(), 12), 0x00000000));
        mRecyclerViewImage.setLayoutManager(new FullyGridLayoutManager(getContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerViewImage.setAdapter(adapter);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    public <AD extends BaseRecyclerViewAdapter<?, ?>> void setAdapter(AD adapter) {
        this.adapter = adapter;
        mRecyclerViewImage.setAdapter(adapter);
    }

    public BaseRecyclerViewAdapter<?, ?> getAdapter() {
        return adapter;
    }
    @SuppressLint("NotifyDataSetChanged")
    public <T extends AttachmentBean> void setImages(List<T> images) {
        if(adapter instanceof FormVideoShowAdapter formVideoShowAdapter){
            formVideoShowAdapter.setList((List<AttachmentBean>) images);
            formVideoShowAdapter.notifyDataSetChanged();
        }
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }

}
