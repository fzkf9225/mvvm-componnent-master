package io.coderf.arklab.common.widget.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.adapter.MenuListAdapter;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.OptionBottomMenuDialogBinding;
import io.coderf.arklab.common.listener.OnOptionBottomMenuClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;

/**
 * Create by fz on 2020/6/23 0023
 * describe:底部选择框
 * 支持自定义菜单项样式：
 * - 列表项高度
 * - 字体大小
 * - 字体颜色
 * - 单行/多行显示
 * - 左右margin
 * - 上下padding
 */
public class BottomSheetDialog<T extends PopupWindowBean> extends com.google.android.material.bottomsheet.BottomSheetDialog {
    /**
     * 菜单点击监听
     */
    private OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener;
    /**
     * 是否允许点击外部取消
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
     * 菜单分割线颜色
     */
    private @ColorInt int lineColor = -1;
    /**
     * 是否显示分割线
     */
    private boolean isShowLine = true;
    /**
     * 是否显示取消按钮
     */
    private boolean showCancelButton = false;

    // ==================== 新增样式属性 ====================
    /**
     * 列表项高度
     */
    private int itemHeight = -1;
    /**
     * 字体大小
     */
    private float textSize = -1;
    /**
     * 字体颜色
     */
    private int textColor = -1;
    /**
     * 最大行数
     */
    private int maxLines = 1;
    /**
     * 是否单行显示
     */
    private boolean isSingleLine = true;
    /**
     * 左边距
     */
    private int leftMargin = -1;
    /**
     * 右边距
     */
    private int rightMargin = -1;
    /**
     * 上内边距
     */
    private int topPadding = -1;
    /**
     * 下内边距
     */
    private int bottomPadding = -1;
    /**
     * 左内边距
     */
    private int leftPadding = -1;
    /**
     * 右内边距
     */
    private int rightPadding = -1;
    /**
     * 取消按钮文字
     */
    private String cancelButtonText;
    /**
     * 取消按钮文字颜色
     */
    private int cancelButtonTextColor = -1;
    /**
     * 取消按钮文字大小
     */
    private float cancelButtonTextSize = -1;
    /**
     * 背景样式
     */
    private Drawable bgDrawable;

    /**
     * 取消按钮背景颜色
     */
    private int cancelButtonBackgroundColor = -1;

    public BottomSheetDialog(@NonNull Context context) {
        super(context);
        itemHeight = DensityUtil.dp2px(getContext(), 54f);
    }

    public BottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        itemHeight = DensityUtil.dp2px(getContext(), 54f);
    }

    public BottomSheetDialog<T> setOnOptionBottomMenuClickListener(OnOptionBottomMenuClickListener<T> optionBottomMenuClickListener) {
        this.optionBottomMenuClickListener = optionBottomMenuClickListener;
        return this;
    }

    public BottomSheetDialog<T> setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public BottomSheetDialog<T> setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

    // ==================== 新增样式设置方法 ====================

    /**
     * 设置列表项高度
     */
    public BottomSheetDialog<T> setItemHeight(int height) {
        this.itemHeight = height;
        return this;
    }

    /**
     * 设置字体大小
     */
    public BottomSheetDialog<T> setTextSize(float size) {
        this.textSize = size;
        return this;
    }

    /**
     * 设置字体颜色
     */
    public BottomSheetDialog<T> setTextColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }

    /**
     * 设置最大行数
     */
    public BottomSheetDialog<T> setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        this.isSingleLine = (maxLines == 1);
        return this;
    }

    /**
     * 设置是否单行显示
     */
    public BottomSheetDialog<T> setSingleLine(boolean singleLine) {
        isSingleLine = singleLine;
        this.maxLines = singleLine ? 1 : Integer.MAX_VALUE;
        return this;
    }

    /**
     * 设置左右边距
     */
    public BottomSheetDialog<T> setMargins(int left, int right) {
        this.leftMargin = left;
        this.rightMargin = right;
        return this;
    }

    /**
     * 设置上下内边距
     */
    public BottomSheetDialog<T> setVerticalPadding(int top, int bottom) {
        this.topPadding = top;
        this.bottomPadding = bottom;
        return this;
    }

    /**
     * 设置左右内边距
     */
    public BottomSheetDialog<T> setHorizontalPadding(int left, int right) {
        this.leftPadding = left;
        this.rightPadding = right;
        return this;
    }

    /**
     * 设置所有内边距
     */
    public BottomSheetDialog<T> setPadding(int left, int top, int right, int bottom) {
        this.leftPadding = left;
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        return this;
    }

    /**
     * 设置取消按钮文字
     */
    public BottomSheetDialog<T> setCancelButtonText(String text) {
        this.cancelButtonText = text;
        return this;
    }

    /**
     * 设置取消按钮文字颜色
     */
    public BottomSheetDialog<T> setCancelButtonTextColor(@ColorInt int color) {
        this.cancelButtonTextColor = color;
        return this;
    }

    /**
     * 设置取消按钮文字大小
     */
    public BottomSheetDialog<T> setCancelButtonTextSize(float size) {
        this.cancelButtonTextSize = size;
        return this;
    }

    /**
     * 设置取消按钮背景颜色
     */
    public BottomSheetDialog<T> setCancelButtonBackgroundColor(@ColorInt int color) {
        this.cancelButtonBackgroundColor = color;
        return this;
    }

    /**
     * 批量设置菜单项样式
     */
    public BottomSheetDialog<T> applyMenuStyles(MenuListAdapter.StyleBuilder builder) {
        if (optionBottomMenuListAdapter != null) {
            optionBottomMenuListAdapter.applyStyles(builder);
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

    public MenuListAdapter<T> getOptionBottomMenuListAdapter() {
        return optionBottomMenuListAdapter;
    }

    private void initView() {
        binding = OptionBottomMenuDialogBinding.inflate(LayoutInflater.from(getContext()), null, false);

        // 设置取消按钮
        setupCancelButton();

        // 初始化适配器
        optionBottomMenuListAdapter = new MenuListAdapter<>();
        optionBottomMenuListAdapter.setList(menuData);

        // 应用样式到适配器
        applyStylesToAdapter();

        // 设置点击监听
        optionBottomMenuListAdapter.setOnItemClickListener((view, position) -> {
            if (optionBottomMenuClickListener != null) {
                optionBottomMenuClickListener.onOptionBottomMenuClick(this, optionBottomMenuListAdapter.getList(), position);
            }
        });

        // 设置RecyclerView
        binding.mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        binding.mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置分割线
        setupDivider();

        // 设置对话框属性
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        setOnShowListener(dialog -> {
            View bottomSheet = this.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                        Color.WHITE,
                        DensityUtil.dp2px(getContext(), 16f),
                        DensityUtil.dp2px(getContext(), 16f),
                        0,
                        0
                )));
            }
        });
    }

    /**
     * 设置取消按钮样式
     */
    private void setupCancelButton() {
        binding.buttonCancel.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);

        if (cancelButtonText != null) {
            binding.buttonCancel.setText(cancelButtonText);
        }

        if (cancelButtonTextColor != -1) {
            binding.buttonCancel.setTextColor(cancelButtonTextColor);
        }

        if (cancelButtonTextSize > 0) {
            binding.buttonCancel.setTextSize(cancelButtonTextSize);
        }

        if (cancelButtonBackgroundColor != -1) {
            binding.buttonCancel.setBackgroundColor(cancelButtonBackgroundColor);
        }
    }

    /**
     * 应用样式到适配器
     */
    private void applyStylesToAdapter() {
        if (itemHeight > 0) {
            optionBottomMenuListAdapter.setItemHeight(itemHeight);
        }

        if (textSize > 0) {
            optionBottomMenuListAdapter.setTextSize(textSize);
        }

        if (textColor != -1) {
            optionBottomMenuListAdapter.setTextColor(textColor);
        }

        optionBottomMenuListAdapter.setSingleLine(isSingleLine);

        if (maxLines > 0) {
            optionBottomMenuListAdapter.setMaxLines(maxLines);
        }

        if (leftMargin >= 0 || rightMargin >= 0) {
            int left = Math.max(leftMargin, 0);
            int right = Math.max(rightMargin, 0);
            optionBottomMenuListAdapter.setMargins(left, right);
        }

        if (topPadding >= 0 || bottomPadding >= 0) {
            int top = Math.max(topPadding, 0);
            int bottom = Math.max(bottomPadding, 0);
            optionBottomMenuListAdapter.setVerticalPadding(top, bottom);
        }

        if (leftPadding >= 0 || rightPadding >= 0) {
            int left = Math.max(leftPadding, 0);
            int right = Math.max(rightPadding, 0);
            optionBottomMenuListAdapter.setHorizontalPadding(left, right);
        }
    }

    /**
     * 设置分割线
     */
    private void setupDivider() {
        if (isShowLine) {
            binding.mRecyclerViewOption.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.HORIZONTAL,
                    DensityUtil.dp2px(getContext(), 1),
                    lineColor == -1 ? ContextCompat.getColor(getContext(), R.color.h_line_color) : lineColor, false));
        }
    }

    /**
     * 刷新菜单项样式
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshStyles() {
        if (optionBottomMenuListAdapter != null) {
            applyStylesToAdapter();
            optionBottomMenuListAdapter.notifyDataSetChanged();
        }
    }
}