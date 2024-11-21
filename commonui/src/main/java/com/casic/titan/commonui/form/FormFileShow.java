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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.adapter.FileShowAdapter;
import com.casic.titan.commonui.bean.AttachmentBean;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.FullyLinearLayoutManager;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormFileShow extends ConstraintLayout {
    protected String labelString;
    protected int bgColor = 0xFFF1F3F2;
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected TextView tvLabel, tvRequired;
    protected RecyclerView mRecyclerViewImage;
    private BaseRecyclerViewAdapter<?, ?> adapter;
    private float formLabelTextSize;
    private float formRequiredSize;

    protected int rightTextColor = 0xFF333333;
    protected int labelTextColor = 0xFF999999;

    private float radius = 5;

    public FormFileShow(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormFileShow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormFileShow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormImage);
            labelString = typedArray.getString(R.styleable.FormImage_label);
            bgColor = typedArray.getColor(R.styleable.FormImage_bgColor, 0xFFF1F3F2);
            rightTextColor = typedArray.getColor(R.styleable.FormImage_rightTextColor, rightTextColor);
            labelTextColor = typedArray.getColor(R.styleable.FormImage_labelTextColor, labelTextColor);
            required = typedArray.getBoolean(R.styleable.FormImage_required, false);
            radius = typedArray.getDimension(R.styleable.FormImage_add_image_radius, DensityUtil.dp2px(getContext(), 4));
            bottomBorder = typedArray.getBoolean(R.styleable.FormImage_bottomBorder, true);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormImage_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormImage_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            typedArray.recycle();
        } else {
            radius = DensityUtil.dp2px(getContext(), 4);
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
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        if (adapter == null) {
            adapter = new FileShowAdapter(getContext());
            FileShowAdapter fileShowAdapter = (FileShowAdapter) adapter;
            fileShowAdapter.setRadius(radius);
            fileShowAdapter.setBgColor(bgColor);
            fileShowAdapter.setTextColor(rightTextColor);
        }

        ConstraintLayout.LayoutParams imageLayoutParams = (LayoutParams) mRecyclerViewImage.getLayoutParams();
        imageLayoutParams.topMargin = DensityUtil.dp2px(getContext(), 12);
        mRecyclerViewImage.setLayoutParams(imageLayoutParams);
        mRecyclerViewImage.addItemDecoration(
                new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL, DensityUtil.dp2px(getContext(), 8),
                        0x00000000)
        );
        mRecyclerViewImage.setLayoutManager(new FullyLinearLayoutManager(getContext()) {
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
    public <T extends AttachmentBean> void setImages(List<AttachmentBean> images) {
        if (adapter instanceof FileShowAdapter fileShowAdapter) {
            fileShowAdapter.setList(images);
            fileShowAdapter.notifyDataSetChanged();
        }
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }


}
