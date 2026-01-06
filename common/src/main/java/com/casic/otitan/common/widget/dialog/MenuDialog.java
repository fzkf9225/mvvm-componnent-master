package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.casic.otitan.common.R;
import com.casic.otitan.common.adapter.MenuListAdapter;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.MenuDialogBinding;
import com.casic.otitan.common.listener.OnOptionBottomMenuClickListener;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.widget.recyclerview.RecycleViewDivider;


/**
 * Created by fz on 2019/10/31.
 * 底部确认弹框
 */
public class MenuDialog<T extends PopupWindowBean> extends Dialog {
    /**
     * 菜单点击监听
     */
    private OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener;
    /**
     * 是否可以点击外部取消
     */
    private boolean outSide = true;
    /**
     * 菜单数据
     */
    private List<T> menuData;
    /**
     * 菜单适配器
     */
    private MenuListAdapter<T> optionBottomMenuListAdapter;
    /**
     * dialog展示位置
     */
    private int gravity = Gravity.BOTTOM;
    /**
     * 是否显示取消按钮
     */
    private boolean isShowCancelButton = true;
    /**
     * 菜单分割线颜色
     */
    private @ColorInt int lineColor = -1;
    /**
     * 是否显示分割线
     */
    private boolean isShowLine = true;
    /**
     * 取消按钮颜色
     */
    private ColorStateList negativeTextColor = null;

    public MenuDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public MenuDialog<T> setOnOptionBottomMenuClickListener(OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener) {
        this.optionBottomMenuClickListener = optionBottomMenuClickListener;
        return this;
    }

    public MenuDialog<T> setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public MenuDialog<T> setData(List<T> menuDatas) {
        this.menuData = menuDatas == null ? new ArrayList<>() : menuDatas;
        return this;
    }

    public MenuDialog<T> setData(String... data) {
        this.menuData = new ArrayList<>();
        if (data == null) {
            return this;
        }
        for (String menu : data) {
            this.menuData.add((T) new PopupWindowBean(null, menu));
        }
        return this;
    }

    public MenuDialog<T> setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public MenuDialog<T> setShowLine(boolean showLine) {
        isShowLine = showLine;
        return this;
    }

    public MenuDialog<T> setDialogGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public MenuDialog<T> setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public MenuDialog<T> setShowCancelButton(boolean showCancelButton) {
        isShowCancelButton = showCancelButton;
        return this;
    }

    public MenuDialog<T> builder() {
        initView();
        return this;
    }

    private MenuDialogBinding binding;

    public MenuDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = MenuDialogBinding.inflate(LayoutInflater.from(getContext()), null, false);
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        if (negativeTextColor != null) {
            binding.buttonCancel.setTextColor(negativeTextColor);
        }
        binding.buttonCancel.setVisibility(isShowCancelButton ? View.VISIBLE : View.GONE);
        optionBottomMenuListAdapter = new MenuListAdapter<>();
        optionBottomMenuListAdapter.setList(menuData);
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });
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
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(gravity);
    }

}
