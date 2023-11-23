package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
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

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.MenuListAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.listener.OnOptionBottomMenuClickListener;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by fz on 2019/10/31.
 * 底部确认弹框
 */
public class MenuDialog<T extends PopupWindowBean> extends Dialog {
    private Context context;
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
            this.menuData.add((T)new PopupWindowBean(null, menu));
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

    private void initView() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.menu_dialog, null);
        RecyclerView mRecyclerViewOption = inflate.findViewById(R.id.mRecyclerView_option);
        Button buttonCancel = inflate.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());
        if (negativeTextColor != null) {
            buttonCancel.setTextColor(negativeTextColor);
        }
        buttonCancel.setVisibility(isShowCancelButton ? View.VISIBLE : View.GONE);
        optionBottomMenuListAdapter = new MenuListAdapter<>(context);
        optionBottomMenuListAdapter.setList(menuData);
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });
        mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewOption.addItemDecoration(new RecycleViewDivider(context, LinearLayoutManager.HORIZONTAL,
                DensityUtil.dp2px(context,1),
                ContextCompat.getColor(context, R.color.h_line_color),false));
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(inflate);
        Window dialogWindow = getWindow();
        DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(gravity);
    }

}
