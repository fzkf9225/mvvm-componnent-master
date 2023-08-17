package pers.fz.mvvm.wight.dialog;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.OptionBottomMenuListAdapter;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.listener.OnOptionBottomMenuClickListener;

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
    private boolean showCancelButton = true;
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

    private void initView(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.option_bottom_menu_dialog, null);
        RecyclerView mRecyclerViewOption = inflate.findViewById(R.id.mRecyclerView_option);
        Button buttonCancel = inflate.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());
        optionBottomMenuListAdapter = new OptionBottomMenuListAdapter<>(context);
        optionBottomMenuListAdapter.setList(menuData);
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });
        buttonCancel.setVisibility(showCancelButton?View.VISIBLE:View.GONE);
        mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(context));
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(inflate);
    }

}
