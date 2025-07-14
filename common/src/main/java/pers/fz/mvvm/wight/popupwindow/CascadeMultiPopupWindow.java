package pers.fz.mvvm.wight.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.PopupMultiCascadeBinding;
import pers.fz.mvvm.util.common.CollectionUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.DrawableUtil;
import pers.fz.mvvm.wight.popupwindow.adapter.PopupWindowCheckBoxAdapter;
import pers.fz.mvvm.wight.popupwindow.adapter.PopupWindowSelectedAdapter;
import pers.fz.mvvm.wight.recyclerview.RecycleViewDivider;

/**
 * created by fz on 2025/7/4 17:38
 * describe:多选级联
 */
public class CascadeMultiPopupWindow<T extends PopupWindowBean> extends PopupWindow implements PopupWindowCheckBoxAdapter.OnItemSelectedChangedListener, PopupWindowSelectedAdapter.OnItemSelectedClearListener {
    public final static String TAG = "PopupMultiCascadeView";
    private PopupWindowCheckBoxAdapter<T> popupWindowAdapter;
    private PopupWindowSelectedAdapter<T> popupWindowSelectedAdapter;
    private PopupMultiCascadeBinding binding;
    private final List<T> dataList;
    private final SelectedListener<T> selectedListener;
    /**
     * 顶部选中文字颜色
     */
    private @ColorInt int selectTextColor;

    private float radius;

    private @ColorInt int selectBgColor;

    /**
     * 选项字体颜色
     */
    private @ColorInt int selectionTextColor;
    /**
     * 列表选中图标样式
     */
    private Drawable checkedDrawable;
    /**
     * 列表未选中图标样式
     */
    private Drawable uncheckedDrawable;

    /**
     * 顶部选中按钮的padding
     */
    private float paddingStart,paddingTop,paddingEnd,paddingBottom;

    /**
     * 列表的高度
     */
    private float itemHeight = 0;

    public CascadeMultiPopupWindow(Activity context, List<T> dataList, SelectedListener<T> selectedListener) {
        super(context);
        this.dataList = dataList;
        this.selectedListener = selectedListener;
        //默认参数
        itemHeight = DensityUtil.dp2px(context, 40f);
        selectionTextColor = ContextCompat.getColor(context, R.color.autoColor);
        selectTextColor = ContextCompat.getColor(context, R.color.autoColor);
        radius = DensityUtil.dp2px(context, 6f);
        selectBgColor = ContextCompat.getColor(context, R.color.default_background);
        paddingStart = DensityUtil.dp2px(context, 12f);
        paddingTop = DensityUtil.dp2px(context, 6f);
        paddingEnd = DensityUtil.dp2px(context, 12f);
        paddingBottom = DensityUtil.dp2px(context, 6f);
        // 获取选中状态的Drawable
        checkedDrawable = DrawableUtil.createCheckedDrawable(context,ContextCompat.getColor(context, R.color.themeColor),DensityUtil.dp2px(context, 16f));
        // 获取未选中状态的Drawable
        uncheckedDrawable = DrawableUtil.createUncheckedDrawable(DensityUtil.dp2px(context, 1),ContextCompat.getColor(context, R.color.themeColor),DensityUtil.dp2px(context, 16));
        initViews(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViews(Activity context) {
        binding = PopupMultiCascadeBinding.inflate(LayoutInflater.from(context), null, false);
        //初始化popupWindowAdapter
        popupWindowSelectedAdapter = new PopupWindowSelectedAdapter<>();
        popupWindowSelectedAdapter.setOnItemSelectedClearListener(this);
        popupWindowAdapter = new PopupWindowCheckBoxAdapter<>(popupWindowSelectedAdapter);
        popupWindowAdapter.setOnItemSelectedChangedListener(this);
        //设置默认参数
        popupWindowAdapter.setItemHeight(itemHeight);
        popupWindowAdapter.setCheckedDrawable(checkedDrawable);
        popupWindowAdapter.setUncheckedDrawable(uncheckedDrawable);
        popupWindowAdapter.setTextColor(selectionTextColor);
        popupWindowAdapter.setShowCheckBox(true);

        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
        popupWindowSelectedAdapter.setSelectBgColor(selectBgColor);
        popupWindowSelectedAdapter.setRadius(radius);
        popupWindowSelectedAdapter.setSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setUnSelectTextColor(selectTextColor);

        popupWindowAdapter.setOnItemClickListener((view, position) -> {
            if (!binding.mRecyclerviewSelected.isShown()) {
                binding.mRecyclerviewSelected.setVisibility(View.VISIBLE);
            }

            boolean hasChild = CollectionUtil.isNotEmpty(popupWindowAdapter.getList().get(position).getChildList());

            if (hasChild) {
                popupWindowAdapter.setList(popupWindowAdapter.getList().get(position).getChildList());
                popupWindowAdapter.notifyDataSetChanged();
            }
        });

        popupWindowAdapter.setList(dataList);
        binding.mRecyclerviewOptions.addItemDecoration(
                new RecycleViewDivider(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        DensityUtil.dp2px(context, 1f),
                        ContextCompat.getColor(context, R.color.h_line_color)
                )
        );
        binding.mRecyclerviewOptions.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        binding.mRecyclerviewOptions.setAdapter(popupWindowAdapter);

        binding.mRecyclerviewSelected.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        binding.mRecyclerviewSelected.setAdapter(popupWindowSelectedAdapter);

        binding.tvClearSelected.setOnClickListener(v -> {
            popupWindowSelectedAdapter.setList(new ArrayList<>());
            popupWindowSelectedAdapter.notifyDataSetChanged();
            popupWindowAdapter.setList(dataList);
            popupWindowAdapter.notifyDataSetChanged();
        });

        // Confirm button click event
        binding.tvConfirm.setOnClickListener(v -> {
            if (popupWindowSelectedAdapter.getList() == null || popupWindowSelectedAdapter.getList().isEmpty()) {
                Toast.makeText(context, "请至少选择一项", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedListener.onSelectedResult(this, popupWindowSelectedAdapter.getList());
            dismiss();
        });

        setContentView(binding.getRoot());
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);
        setAnimationStyle(android.R.style.Animation_Dialog);
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.color.transparent));

        DisplayMetrics appDisplayMetrics = context.getResources().getDisplayMetrics();
        setWidth(appDisplayMetrics.widthPixels);
        setHeight(appDisplayMetrics.heightPixels * 2 / 3);

        android.view.WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = 0.5f; // Set shadow transparency
        context.getWindow().setAttributes(lp);

        setOnDismissListener(() -> {
            android.view.WindowManager.LayoutParams windowLayoutParams = context.getWindow().getAttributes();
            windowLayoutParams.alpha = 1f;
            context.getWindow().setAttributes(windowLayoutParams);
        });
    }
    /**
     * 顶部标题文字
     * @param title 标题提示文字
     */
    public void setTitle(String title) {
        binding.tvTitle.setText(title);
    }
    /**
     * 确认按钮文字内容
     * @param confirmText 确认按钮文字内容
     */
    public void setConfirmText(String confirmText) {
        binding.tvConfirm.setText(confirmText);
    }
    /**
     * 清空按钮文字内容
     * @param clearText 清空按钮文字内容
     */
    public void setClearText(String clearText) {
        binding.tvClearSelected.setText(clearText);
    }
    /**
     * 确认按钮文字颜色
     * @param confirmTextColor 确认按钮文字颜色
     */
    public void setConfirmTextColor(@ColorInt int confirmTextColor) {
        binding.tvConfirm.setTextColor(confirmTextColor);
    }
    /**
     * 清空按钮文字颜色
     * @param clearTextColor 清空按钮文字颜色
     */
    public void setClearTextColor(@ColorInt int clearTextColor) {
        binding.tvClearSelected.setTextColor(clearTextColor);
    }
    /**
     * 确认按钮文字大小
     * @param confirmTextSize 确认按钮文字大小
     */
    public void setConfirmTextSize(float confirmTextSize) {
        binding.tvConfirm.setTextSize(confirmTextSize);
    }
    /**
     * 清空按钮文字大小
     * @param clearTextSize 清空按钮文字大小
     */
    public void setClearTextSize(float clearTextSize) {
        binding.tvClearSelected.setTextSize(clearTextSize);
    }
    /**
     * 顶部标题文字颜色
     * @param titleTextColor 顶部标题文字颜色
     */
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        binding.tvTitle.setTextColor(titleTextColor);
    }

    /**
     * 顶部标题文字是否加粗
     * @param titleBold 顶部标题文字是否加粗
     */
    public void setTitleBold(boolean titleBold) {
        binding.tvTitle.setTypeface(titleBold ? Typeface.defaultFromStyle(Typeface.BOLD) : null);
    }

    /**
     * 设置顶部选项未选中样式，不区分选中和不选中
     * @param bgColorRes 选项未选中背景颜色
     * @param textColorRes 选项未选中文字颜色
     * @param radius 选项未选中圆角
     */
    public void setSelectedBgStyle(@ColorInt int bgColorRes,@ColorInt int textColorRes,int radius) {
        this.radius = radius;
        this.selectBgColor = bgColorRes;
        this.selectTextColor = textColorRes;
        popupWindowSelectedAdapter.setSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setUnSelectTextColor(selectTextColor);
        popupWindowSelectedAdapter.setSelectBgColor(this.selectBgColor);
        popupWindowSelectedAdapter.setUnSelectBgColor(this.selectBgColor);
        popupWindowSelectedAdapter.setRadius(this.radius);
    }


    /**
     * 设置顶部选中选项的按钮内间距
     * @param paddingStart 左间距
     * @param paddingTop 上间距
     * @param paddingEnd 右间距
     * @param paddingBottom 下间距
     */
    public void setPadding(float paddingStart, float paddingTop, float paddingEnd, float paddingBottom) {
        this.paddingStart = paddingStart;
        this.paddingTop = paddingTop;
        this.paddingEnd = paddingEnd;
        this.paddingBottom = paddingBottom;
        popupWindowSelectedAdapter.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom);
    }

    /**
     * 列表文字颜色
     * @param selectionTextColor 文字颜色
     */
    public void setSelectionTextColor(int selectionTextColor) {
        this.selectionTextColor = selectionTextColor;
        popupWindowAdapter.setTextColor(selectionTextColor);
    }

    /**
     * 列表项选中时图标，也就是checkbox样式
     * @param checkedDrawable 选中图标
     */
    public void setCheckedDrawable(Drawable checkedDrawable) {
        this.checkedDrawable = checkedDrawable;
        popupWindowAdapter.setCheckedDrawable(checkedDrawable);
    }

    /**
     * 列表项未选中时图标，也就是checkbox样式
     * @param uncheckedDrawable 未选中图标
     */
    public void setUncheckedDrawable(Drawable uncheckedDrawable) {
        this.uncheckedDrawable = uncheckedDrawable;
        popupWindowAdapter.setUncheckedDrawable(uncheckedDrawable);
    }

    /**
     * 列表项高度
     * @param itemHeight 高度，单位px
     */
    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
        popupWindowAdapter.setItemHeight(itemHeight);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSelectedChanged(AppCompatImageView ivCheckView, int position) {
        if (!binding.mRecyclerviewSelected.isShown()) {
            binding.mRecyclerviewSelected.setVisibility(View.VISIBLE);
        }
        T selectedItem = popupWindowAdapter.getList().get(position);
        if (PopupWindowBean.containsById(popupWindowSelectedAdapter.getList(), selectedItem.getPopupId())) {
            popupWindowSelectedAdapter.getList().remove(selectedItem);
        } else {
            popupWindowSelectedAdapter.getList().add(selectedItem);
        }
        popupWindowSelectedAdapter.notifyDataSetChanged();
        popupWindowAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemSelectedClear(AppCompatImageView ivCheckView, int position) {
        popupWindowSelectedAdapter.getList().remove(position);
        popupWindowSelectedAdapter.notifyDataSetChanged();
        popupWindowAdapter.notifyDataSetChanged();
    }

    public interface SelectedListener<T> {
         void onSelectedResult(PopupWindow popupWindow, List<T> dataList);
    }
}

