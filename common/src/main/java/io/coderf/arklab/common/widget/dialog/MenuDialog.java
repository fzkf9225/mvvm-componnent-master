package io.coderf.arklab.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.adapter.MenuListAdapter;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.MenuDialogBinding;
import io.coderf.arklab.common.listener.OnOptionBottomMenuClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider;


/**
 * Created by fz on 2019/10/31.
 * 底部确认弹框
 * 支持自定义菜单项样式：
 * - 列表项高度
 * - 字体大小
 * - 字体颜色
 * - 单行/多行显示
 * - 左右margin
 * - 上下padding
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
    private ColorStateList negativeTextColor = null;
    /**
     * 取消按钮文字大小
     */
    private float cancelButtonTextSize = -1;
    /**
     * 取消按钮背景颜色
     */
    private int cancelButtonBackgroundColor = -1;
    /**
     * 取消按钮背景Drawable
     */
    private Drawable cancelButtonBackgroundDrawable;
    /**
     * 对话框背景Drawable
     */
    private Drawable bgDrawable;

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

    public MenuDialog<T> setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

    public MenuDialog<T> setShowCancelButton(boolean showCancelButton) {
        isShowCancelButton = showCancelButton;
        return this;
    }

    // ==================== 新增样式设置方法 ====================

    /**
     * 设置列表项高度
     */
    public MenuDialog<T> setItemHeight(int height) {
        this.itemHeight = height;
        return this;
    }

    /**
     * 设置字体大小
     */
    public MenuDialog<T> setTextSize(float size) {
        this.textSize = size;
        return this;
    }

    /**
     * 设置字体颜色
     */
    public MenuDialog<T> setTextColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }

    /**
     * 设置最大行数
     */
    public MenuDialog<T> setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        this.isSingleLine = (maxLines == 1);
        return this;
    }

    /**
     * 设置是否单行显示
     */
    public MenuDialog<T> setSingleLine(boolean singleLine) {
        isSingleLine = singleLine;
        this.maxLines = singleLine ? 1 : Integer.MAX_VALUE;
        return this;
    }

    /**
     * 设置左右边距
     */
    public MenuDialog<T> setMargins(int left, int right) {
        this.leftMargin = left;
        this.rightMargin = right;
        return this;
    }

    /**
     * 设置上下内边距
     */
    public MenuDialog<T> setVerticalPadding(int top, int bottom) {
        this.topPadding = top;
        this.bottomPadding = bottom;
        return this;
    }

    /**
     * 设置左右内边距
     */
    public MenuDialog<T> setHorizontalPadding(int left, int right) {
        this.leftPadding = left;
        this.rightPadding = right;
        return this;
    }

    /**
     * 设置所有内边距
     */
    public MenuDialog<T> setPadding(int left, int top, int right, int bottom) {
        this.leftPadding = left;
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        return this;
    }

    /**
     * 设置取消按钮文字
     */
    public MenuDialog<T> setCancelButtonText(String text) {
        this.cancelButtonText = text;
        return this;
    }

    /**
     * 设置取消按钮文字颜色
     */
    public MenuDialog<T> setNegativeTextColor(@ColorInt int color) {
        this.negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * 设置取消按钮文字大小
     */
    public MenuDialog<T> setCancelButtonTextSize(float size) {
        this.cancelButtonTextSize = size;
        return this;
    }

    /**
     * 设置取消按钮背景颜色
     */
    public MenuDialog<T> setCancelButtonBackgroundColor(@ColorInt int color) {
        this.cancelButtonBackgroundColor = color;
        return this;
    }

    /**
     * 设置取消按钮背景Drawable
     */
    public MenuDialog<T> setCancelButtonBackgroundDrawable(Drawable drawable) {
        this.cancelButtonBackgroundDrawable = drawable;
        return this;
    }

    /**
     * 批量设置菜单项样式
     */
    public MenuDialog<T> applyMenuStyles(MenuListAdapter.StyleBuilder builder) {
        if (optionBottomMenuListAdapter != null) {
            optionBottomMenuListAdapter.applyStyles(builder);
        }
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

        binding.mRecyclerViewOption.setAdapter(optionBottomMenuListAdapter);
        binding.mRecyclerViewOption.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置分割线
        setupDivider();

        // 设置对话框属性
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());

        // 设置窗口属性
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(gravity);
        if (bgDrawable != null) {
            dialogWindow.setBackgroundDrawable(bgDrawable);
        }
    }

    /**
     * 设置取消按钮样式
     */
    private void setupCancelButton() {
        binding.buttonCancel.setVisibility(isShowCancelButton ? View.VISIBLE : View.GONE);
        binding.buttonCancel.setOnClickListener(v -> dismiss());

        if (cancelButtonText != null) {
            binding.buttonCancel.setText(cancelButtonText);
        }

        if (negativeTextColor != null) {
            binding.buttonCancel.setTextColor(negativeTextColor);
        }

        if (cancelButtonTextSize > 0) {
            binding.buttonCancel.setTextSize(cancelButtonTextSize);
        }

        if (cancelButtonBackgroundColor != -1) {
            binding.buttonCancel.setBackgroundColor(cancelButtonBackgroundColor);
        }

        if (cancelButtonBackgroundDrawable != null) {
            binding.buttonCancel.setBackground(cancelButtonBackgroundDrawable);
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
            int left = leftMargin >= 0 ? leftMargin : 0;
            int right = rightMargin >= 0 ? rightMargin : 0;
            optionBottomMenuListAdapter.setMargins(left, right);
        }

        if (topPadding >= 0 || bottomPadding >= 0) {
            int top = topPadding >= 0 ? topPadding : 0;
            int bottom = bottomPadding >= 0 ? bottomPadding : 0;
            optionBottomMenuListAdapter.setVerticalPadding(top, bottom);
        }

        if (leftPadding >= 0 || rightPadding >= 0) {
            int left = leftPadding >= 0 ? leftPadding : 0;
            int right = rightPadding >= 0 ? rightPadding : 0;
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
    public void refreshStyles() {
        if (optionBottomMenuListAdapter != null) {
            applyStylesToAdapter();
            optionBottomMenuListAdapter.notifyDataSetChanged();
        }
    }

    public MenuListAdapter<T> getOptionBottomMenuListAdapter() {
        return optionBottomMenuListAdapter;
    }
}