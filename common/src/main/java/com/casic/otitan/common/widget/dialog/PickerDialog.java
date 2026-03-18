package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.casic.otitan.common.R;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.DialogPickerBinding;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.DrawableUtil;

/**
 * PickerDialog选择框
 * 支持泛型数据，页面上显示popupName字段
 * 支持自定义样式：
 * - 自定义分割线颜色
 * - 自定义文字颜色和大小
 * - 自定义选中项文字颜色
 * - 自定义按钮样式
 *
 * @author fz
 * @version 2.0
 * @since 2.0
 * @created 2026/3/18
 */
public class PickerDialog<T extends PopupWindowBean> extends Dialog {

    // 数据相关
    private List<T> menuData; // 泛型数据列表
    private String[] displayValues; // 显示的字符串数组（兼容旧版）
    private int selectedPosition = 0; // 默认选中位置
    private OnNumberPickListener<T> onNumberPickListener;

    // 样式属性
    private int dividerHeight = 0; // 分割线高度
    private @ColorInt int dividerColor = -1; // 分割线颜色
    private @ColorInt int textColor = -1;     // 文字颜色
    private @ColorInt int selectedTextColor = -1; // 选中项文字颜色
    private float textSize = -1;              // 文字大小
    private String titleText;                  // 标题文字
    private String confirmButtonText;          // 确认按钮文字
    private String cancelButtonText;            // 取消按钮文字
    private @ColorInt int confirmButtonColor = -1; // 确认按钮文字颜色
    private @ColorInt int cancelButtonColor = -1;   // 取消按钮文字颜色
    private @ColorInt int confirmButtonBgColor = -1; // 确认按钮背景颜色
    private @ColorInt int cancelButtonBgColor = -1;   // 取消按钮背景颜色
    private Drawable bgDrawable;                 // 对话框背景
    private boolean wrapSelectorWheel = true;    // 是否循环滚动
    private int visibility = View.VISIBLE;       // 是否可见

    // 对话框属性
    private boolean showTitle = true;               // 是否允许点击外部取消
    // 对话框属性
    private boolean outSide = true;               // 是否允许点击外部取消
    private int gravity = Gravity.BOTTOM;         // 对话框显示位置

    private DialogPickerBinding binding;
    private final Context context;

    public PickerDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public PickerDialog(@NonNull Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    // ==================== 数据设置方法 ====================

    /**
     * 设置数据列表（泛型方式）
     */
    public PickerDialog<T> setData(List<T> dataList) {
        this.menuData = dataList == null ? new ArrayList<>() : dataList;
        // 同时设置displayValues为popupName数组
        if (this.menuData != null && !this.menuData.isEmpty()) {
            this.displayValues = new String[this.menuData.size()];
            for (int i = 0; i < this.menuData.size(); i++) {
                this.displayValues[i] = this.menuData.get(i).getPopupName();
            }
        }
        return this;
    }

    /**
     * 设置数据列表（可变参数方式）
     */
    @SafeVarargs
    public final PickerDialog<T> setData(T... data) {
        this.menuData = new ArrayList<>();
        if (data == null) {
            return this;
        }
        Collections.addAll(this.menuData, data);
        // 同时设置displayValues为popupName数组
        if (!this.menuData.isEmpty()) {
            this.displayValues = new String[this.menuData.size()];
            for (int i = 0; i < this.menuData.size(); i++) {
                this.displayValues[i] = this.menuData.get(i).getPopupName();
            }
        }
        return this;
    }

    /**
     * 设置数据列表（字符串数组方式，兼容旧版）
     */
    public PickerDialog<T> setDisplayedValues(String[] values) {
        this.displayValues = values;
        // 清空泛型数据
        this.menuData = null;
        return this;
    }

    /**
     * 设置数据列表（字符串列表方式，兼容旧版）
     */
    public PickerDialog<T> setDisplayedValues(List<String> values) {
        this.displayValues = values.toArray(new String[0]);
        // 清空泛型数据
        this.menuData = null;
        return this;
    }

    /**
     * 设置默认选中位置
     */
    public PickerDialog<T> setSelectedPosition(int position) {
        if (displayValues != null && position >= 0 && position < displayValues.length) {
            this.selectedPosition = position;
        }
        return this;
    }

    /**
     * 设置默认选中值（根据popupName匹配）
     */
    public PickerDialog<T> setSelectedValue(String value) {
        if (displayValues != null) {
            for (int i = 0; i < displayValues.length; i++) {
                if (displayValues[i].equals(value)) {
                    this.selectedPosition = i;
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 设置默认选中项（根据数据项匹配）
     */
    public PickerDialog<T> setSelectedItem(T item) {
        if (menuData != null && item != null) {
            for (int i = 0; i < menuData.size(); i++) {
                if (menuData.get(i).equals(item) ||
                        (item.getPopupId() != null && item.getPopupId().equals(menuData.get(i).getPopupId()))) {
                    this.selectedPosition = i;
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 设置选择监听
     */
    public PickerDialog<T> setOnNumberPickListener(OnNumberPickListener<T> listener) {
        this.onNumberPickListener = listener;
        return this;
    }

    // ==================== 样式设置方法 ====================

    /**
     * 设置分割线颜色
     */
    public PickerDialog<T> setDividerColor(@ColorInt int color) {
        this.dividerColor = color;
        return this;
    }

    /**
     * 设置文字颜色
     */
    public PickerDialog<T> setTextColor(@ColorInt int color) {
        this.textColor = color;
        return this;
    }

    /**
     * 分割线高度，默认为0
     * @param dividerHeight
     */
    public PickerDialog<T> setDividerHeight(int dividerHeight) {
        this.dividerHeight = dividerHeight;
        return this;
    }

    /**
     * 设置选中项文字颜色
     */
    public PickerDialog<T> setSelectedTextColor(@ColorInt int color) {
        this.selectedTextColor = color;
        return this;
    }

    /**
     * 设置文字大小
     */
    public PickerDialog<T> setTextSize(float sizeSp) {
        this.textSize = sizeSp;
        return this;
    }

    /**
     * 设置标题文字
     */
    public PickerDialog<T> setTitleText(String title) {
        this.titleText = title;
        return this;
    }

    /**
     * 设置确认按钮文字
     */
    public PickerDialog<T> setConfirmButtonText(String text) {
        this.confirmButtonText = text;
        return this;
    }

    /**
     * 设置取消按钮文字
     */
    public PickerDialog<T> setCancelButtonText(String text) {
        this.cancelButtonText = text;
        return this;
    }

    /**
     * 设置确认按钮文字颜色
     */
    public PickerDialog<T> setConfirmButtonColor(@ColorInt int color) {
        this.confirmButtonColor = color;
        return this;
    }

    /**
     * 设置取消按钮文字颜色
     */
    public PickerDialog<T> setCancelButtonColor(@ColorInt int color) {
        this.cancelButtonColor = color;
        return this;
    }

    /**
     * 设置确认按钮背景颜色
     */
    public PickerDialog<T> setConfirmButtonBgColor(@ColorInt int color) {
        this.confirmButtonBgColor = color;
        return this;
    }

    /**
     * 设置取消按钮背景颜色
     */
    public PickerDialog<T> setCancelButtonBgColor(@ColorInt int color) {
        this.cancelButtonBgColor = color;
        return this;
    }

    /**
     * 设置对话框背景
     */
    public PickerDialog<T> setBgDrawable(Drawable drawable) {
        this.bgDrawable = drawable;
        return this;
    }

    /**
     * 设置是否循环滚动
     */
    public PickerDialog<T> setWrapSelectorWheel(boolean wrap) {
        this.wrapSelectorWheel = wrap;
        return this;
    }

    /**
     * 设置是否允许点击外部取消
     */
    public PickerDialog<T> setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public PickerDialog<T> setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    /**
     * 设置对话框显示位置
     */
    public PickerDialog<T> setDialogGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * 设置NumberPicker是否可见
     */
    public PickerDialog<T> setNumberPickerVisibility(int visibility) {
        this.visibility = visibility;
        return this;
    }

    public PickerDialog<T> builder() {
        initView();
        return this;
    }

    private void initView() {
        binding = DialogPickerBinding.inflate(LayoutInflater.from(context), null, false);

        // 设置标题
        if (titleText != null && !titleText.isEmpty()) {
            binding.tvTitle.setText(titleText);
        }

        // 设置NumberPicker
        setupNumberPicker();

        // 设置按钮
        setupButtons();
        if (showTitle) {
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.vLine.setVisibility(View.VISIBLE);
        } else {
            binding.tvTitle.setVisibility(View.GONE);
            binding.vLine.setVisibility(View.GONE);
        }

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
        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                Color.WHITE,
                DensityUtil.dp2px(context, 16f),
                DensityUtil.dp2px(context, 16f),
                0,
                0
        )));

    }

    /**
     * 设置NumberPicker
     */
    private void setupNumberPicker() {
        if (displayValues == null || displayValues.length == 0) {
            binding.dataPicker.setVisibility(View.GONE);
            return;
        }
        binding.dataPicker.setTextSize(textSize);
        binding.dataPicker.setTextColor(textColor);
        binding.dataPicker.setSelectionDividerHeight(dividerHeight);
        binding.dataPicker.setVisibility(visibility);
        binding.dataPicker.setMinValue(0);
        binding.dataPicker.setMaxValue(displayValues.length - 1);
        binding.dataPicker.setDisplayedValues(displayValues);
        binding.dataPicker.setValue(selectedPosition);
        binding.dataPicker.setWrapSelectorWheel(wrapSelectorWheel);
    }

    /**
     * 设置按钮
     */
    private void setupButtons() {
        // 确认按钮
        if (confirmButtonText != null) {
            binding.dialogConfirm.setText(confirmButtonText);
        }
        if (confirmButtonColor != -1) {
            binding.dialogConfirm.setTextColor(confirmButtonColor);
        }
        if (confirmButtonBgColor != -1) {
            binding.dialogConfirm.setBackgroundColor(confirmButtonBgColor);
        }
        binding.dialogConfirm.setOnClickListener(v -> {
            int selectedPos = binding.dataPicker.getValue();
            if (onNumberPickListener != null) {
                String displayValue = displayValues != null ? displayValues[selectedPos] : null;
                T selectedItem = (menuData != null && selectedPos < menuData.size()) ? menuData.get(selectedPos) : null;
                onNumberPickListener.onConfirm(selectedPos, displayValue, selectedItem);
            }
            dismiss();
        });

        // 取消按钮
        if (cancelButtonText != null) {
            binding.dialogCancel.setText(cancelButtonText);
        }
        if (cancelButtonColor != -1) {
            binding.dialogCancel.setTextColor(cancelButtonColor);
        }
        if (cancelButtonBgColor != -1) {
            binding.dialogCancel.setBackgroundColor(cancelButtonBgColor);
        }
        binding.dialogCancel.setOnClickListener(v -> {
            if (onNumberPickListener != null) {
                onNumberPickListener.onCancel();
            }
            dismiss();
        });
    }

    /**
     * 获取选中的位置
     */
    public int getSelectedPosition() {
        return binding.dataPicker.getValue();
    }

    /**
     * 获取选中的显示值
     */
    public String getSelectedDisplayValue() {
        int pos = binding.dataPicker.getValue();
        return displayValues != null ? displayValues[pos] : null;
    }

    /**
     * 获取选中的数据项
     */
    public T getSelectedItem() {
        int pos = binding.dataPicker.getValue();
        return (menuData != null && pos < menuData.size()) ? menuData.get(pos) : null;
    }

    public DialogPickerBinding getBinding() {
        return binding;
    }

    // ==================== 监听接口 ====================

    public interface OnNumberPickListener<T extends PopupWindowBean> {
        /**
         * 确认选择
         * @param position 选中位置
         * @param displayValue 显示值
         * @param selectedItem 选中的数据项
         */
        void onConfirm(int position, String displayValue, T selectedItem);

        /**
         * 取消选择
         */
        default void onCancel() {
            // 默认空实现
        }
    }
}