package com.casic.otitan.common.widget.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.casic.otitan.common.R;
import com.casic.otitan.common.adapter.OptionBottomMenuListAdapter;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionBottomMenuDialogBinding;
import com.casic.otitan.common.listener.OnOptionBottomMenuClickListener;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.widget.recyclerview.RecycleViewDivider;

/**
 * Create by CherishTang on 2020/6/23 0023
 * describe:底部选择框
 */
public class BottomSheetDialog<T extends PopupWindowBean> extends com.google.android.material.bottomsheet.BottomSheetDialog {
    private OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener;
    private boolean outSide = true;
    private List<T> menuData;
    private OptionBottomMenuListAdapter<T> optionBottomMenuListAdapter;
    private @ColorInt int lineColor = -1;
    private boolean isShowLine = true;
    private boolean showCancelButton = false;

    public BottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public BottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    public BottomSheetDialog<T> setOnOptionBottomMenuClickListener(OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener) {
        this.optionBottomMenuClickListener = optionBottomMenuClickListener;
        return this;
    }

    public BottomSheetDialog<T> setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public BottomSheetDialog<T> setShowCancelButton(boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
        return this;
    }

    public BottomSheetDialog<T> setData(List<T> menuDatas) {
        this.menuData = menuDatas == null ? new ArrayList<>() : menuDatas;
        return this;
    }

    public BottomSheetDialog<T> setShowLine(boolean showLine) {
        isShowLine = showLine;
        return this;
    }

    public BottomSheetDialog<T> setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public BottomSheetDialog<T> setData(String... data) {
        this.menuData = new ArrayList<>();
        if (data == null) {
            return this;
        }
        for (String menu : data) {
            this.menuData.add((T) new PopupWindowBean(null, menu));
        }
        return this;
    }

    public BottomSheetDialog<T> builder() {
        initView();
        return this;
    }

    private OptionBottomMenuDialogBinding binding;

    public OptionBottomMenuDialogBinding getBinding() {
        return binding;
    }

    public OptionBottomMenuListAdapter<T> getOptionBottomMenuListAdapter() {
        return optionBottomMenuListAdapter;
    }

    private void initView() {
        binding = OptionBottomMenuDialogBinding.inflate(LayoutInflater.from(getContext()), null, false);
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        optionBottomMenuListAdapter = new OptionBottomMenuListAdapter<>();
        optionBottomMenuListAdapter.setList(menuData);
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });
        binding.buttonCancel.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);
        binding.mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        binding.mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(getContext()));
        if (isShowLine) {
            binding.mRecyclerViewOption.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL,
                    DensityUtil.dp2px(getContext(), 1),
                    lineColor == -1 ? ContextCompat.getColor(getContext(), R.color.h_line_color) : lineColor, false));
        }
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
    }

}
