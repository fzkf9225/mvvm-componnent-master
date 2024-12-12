package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.MenuListAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.MenuDialogBinding;
import pers.fz.mvvm.listener.OnOptionBottomMenuClickListener;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;


/**
 * Created by fz on 2019/10/31.
 * 底部确认弹框
 */
public class MenuDialog<T extends PopupWindowBean> extends Dialog {
    private final Context context;
    private OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener;
    /**
     * 是否可以点击外部取消
     */
    private boolean outSide = true;
    private List<T> menuData;
    private MenuListAdapter<T> optionBottomMenuListAdapter;
    /**
     * dialog展示位置
     */
    private int gravity = Gravity.BOTTOM;
    private boolean isShowCancelButton = true;
    /**
     * 取消按钮颜色
     */
    private ColorStateList negativeTextColor = null;

    public MenuDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
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
        binding = MenuDialogBinding.inflate(LayoutInflater.from(context), null, false);
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
        binding.mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(context));
        binding.mRecyclerViewOption.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL,
                DensityUtil.dp2px(context, 1),
                ContextCompat.getColor(context, R.color.h_line_color), false));
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
