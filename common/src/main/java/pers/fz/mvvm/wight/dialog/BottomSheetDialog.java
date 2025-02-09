package pers.fz.mvvm.wight.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.OptionBottomMenuListAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionBottomMenuDialogBinding;
import pers.fz.mvvm.listener.OnOptionBottomMenuClickListener;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * Create by CherishTang on 2020/6/23 0023
 * describe:底部选择框
 */
public class BottomSheetDialog<T extends PopupWindowBean> extends com.google.android.material.bottomsheet.BottomSheetDialog {
    private Context context = null;
    private OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener;
    private boolean outSide = true;
    private List<T> menuData;
    private OptionBottomMenuListAdapter<T> optionBottomMenuListAdapter;
    private boolean showCancelButton = false;
    public BottomSheetDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public BottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        this.context = context;
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

    public BottomSheetDialog<T> setData(String... data) {
        this.menuData = new ArrayList<>();
        if (data == null) {
            return this;
        }
        for (String menu : data) {
            this.menuData.add((T)new PopupWindowBean(null, menu));
        }
        return this;
    }

    public BottomSheetDialog<T> builder() {
        initView(context);
        return this;
    }

    private OptionBottomMenuDialogBinding binding;

    public OptionBottomMenuDialogBinding getBinding() {
        return binding;
    }

    public OptionBottomMenuListAdapter<T> getOptionBottomMenuListAdapter() {
        return optionBottomMenuListAdapter;
    }

    private void initView(Context context) {
        binding = OptionBottomMenuDialogBinding.inflate(LayoutInflater.from(context), null, false);
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        optionBottomMenuListAdapter = new OptionBottomMenuListAdapter<>();
        optionBottomMenuListAdapter.setList(menuData);
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });
        binding.buttonCancel.setVisibility(showCancelButton?View.VISIBLE:View.GONE);
        binding.mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        binding.mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(context));
        binding.mRecyclerViewOption.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL,
                DensityUtil.dp2px(context, 1),
                ContextCompat.getColor(context, R.color.h_line_color), false));
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
    }

}
