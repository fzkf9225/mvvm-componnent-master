package com.casic.otitan.common.widget.popupwindow.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.api.Config;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionTextViewBinding;
import com.casic.otitan.common.utils.common.CollectionUtil;
import com.casic.otitan.common.utils.common.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本选项Adapter - 支持单选/多选，支持自定义样式
 *
 * @author fz
 * @version 2.0
 * @since 2.0
 * @created 2026/3/12
 */
public class PopupWindowAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {

    /**
     * 选择模式：多选
     */
    public static final int MODE_MULTI = 0;
    /**
     * 选择模式：单选
     */
    public static final int MODE_SINGLE = 1;

    /**
     * 当前选择模式，默认为单选（保持向后兼容）
     */
    private int selectionMode = MODE_SINGLE;

    /**
     * 文本对齐方式，默认左边对齐
     */
    private int textGravity = Gravity.START | Gravity.CENTER_VERTICAL;

    /**
     * 文本颜色（未选中）
     */
    private @ColorInt Integer textColor;
    /**
     * 输入框、选择框正文行数
     */
    protected int line = 1;

    /**
     * 文本选中颜色
     */
    private @ColorInt Integer textSelectedColor;

    /**
     * 文本大小（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float textSize;

    /**
     * 背景Drawable（未选中）
     */
    private Drawable backgroundDrawable;

    /**
     * 背景选中Drawable
     */
    private Drawable backgroundSelectedDrawable;

    /**
     * 列表项高度（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float itemHeight;

    /**
     * 左内边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer paddingLeft;

    /**
     * 右内边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer paddingRight;

    /**
     * 上内边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer paddingTop;

    /**
     * 下内边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer paddingBottom;

    /**
     * 左边距（单位：px）- 通过LayoutParams设置
     */
    private @Dimension(unit = Dimension.PX) Integer marginStart;

    /**
     * 右边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer marginEnd;

    /**
     * 上边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer marginTop;

    /**
     * 下边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer marginBottom;

    /**
     * 是否启用默认的点击选择逻辑
     */
    private boolean enableDefaultClick = true;

    public PopupWindowAdapter() {
        super();
        initDefaultClickListener();
    }

    public PopupWindowAdapter(int selectionMode) {
        this();
        this.selectionMode = selectionMode;
    }

    private void initDefaultClickListener() {
        setOnItemClickListener((view, position) -> {
            if (!enableDefaultClick) {
                return;
            }

            if (selectionMode == MODE_SINGLE) {
                selectSingle(position);
            } else {
                toggleSelection(position);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.option_text_view;
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        T item = mList.get(pos);

        // 设置数据
        holder.getBinding().setItem(item);

        // 应用动态属性
        applyDynamicProperties(holder);

        // 设置选中状态样式
        boolean isSelected = item.getSelected() != null && item.getSelected();

        // 设置文本颜色
        if (isSelected && textSelectedColor != null) {
            holder.getBinding().tvOption.setTextColor(textSelectedColor);
        } else if (textColor != null) {
            holder.getBinding().tvOption.setTextColor(textColor);
        }

        // 设置背景
        if (isSelected && backgroundSelectedDrawable != null) {
            holder.getBinding().tvOption.setBackground(backgroundSelectedDrawable);
        } else if (backgroundDrawable != null) {
            holder.getBinding().tvOption.setBackground(backgroundDrawable);
        }

        holder.getBinding().executePendingBindings();
    }

    /**
     * 应用动态设置的属性到ViewHolder
     */
    private void applyDynamicProperties(BaseViewHolder<OptionTextViewBinding> holder) {
        OptionTextViewBinding binding = holder.getBinding();

        // 设置文本大小
        if (textSize != null) {
            binding.tvOption.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        // 设置文本对齐方式
        binding.tvOption.setGravity(textGravity);

        if (line == 1) {
            binding.tvOption.setMaxLines(1);
            binding.tvOption.setEllipsize(TextUtils.TruncateAt.END);
        } else if (line > 1) {
            binding.tvOption.setMaxLines(line);
            binding.tvOption.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            binding.tvOption.setMaxLines(Integer.MAX_VALUE);
        }
        // 设置内边距
        int left = paddingLeft != null ? paddingLeft : binding.tvOption.getPaddingLeft();
        int top = paddingTop != null ? paddingTop : binding.tvOption.getPaddingTop();
        int right = paddingRight != null ? paddingRight : binding.tvOption.getPaddingRight();
        int bottom = paddingBottom != null ? paddingBottom : binding.tvOption.getPaddingBottom();
        binding.tvOption.setPadding(left, top, right, bottom);

        // 设置高度
        if (itemHeight != null && itemHeight > 0) {
            ViewGroup.LayoutParams params = binding.tvOption.getLayoutParams();
            params.height = Math.round(itemHeight);
            binding.tvOption.setLayoutParams(params);
        }

        // 设置外边距
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.tvOption.getLayoutParams();
        if (params != null) {
            int leftMargin = marginStart != null ? marginStart : params.leftMargin;
            int topMargin = marginTop != null ? marginTop : params.topMargin;
            int rightMargin = marginEnd != null ? marginEnd : params.rightMargin;
            int bottomMargin = marginBottom != null ? marginBottom : params.bottomMargin;

            params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            binding.tvOption.setLayoutParams(params);
        }
    }

    @Override
    protected BaseViewHolder<OptionTextViewBinding> createViewHold(OptionTextViewBinding binding) {
        return new ViewHolder<>(binding, this);
    }

    /**
     * 切换项的选择状态（多选模式）
     */
    private void toggleSelection(int position) {
        T item = mList.get(position);
        boolean newSelected = item.getSelected() == null || !item.getSelected();
        item.setSelected(newSelected);
        notifyItemChanged(position);
    }

    /**
     * 单选模式：选择一项，取消其他项
     */
    private void selectSingle(int position) {
        T clickedItem = mList.get(position);
        boolean willBeSelected = clickedItem.getSelected() == null || !clickedItem.getSelected();

        // 如果点击的是当前选中的项，且要取消选中
        if (!willBeSelected && clickedItem.getSelected() != null && clickedItem.getSelected()) {
            clickedItem.setSelected(false);
            notifyItemChanged(position);
            return;
        }

        // 选中新项，取消其他所有项
        for (int i = 0; i < mList.size(); i++) {
            T item = mList.get(i);
            boolean shouldSelect = (i == position);
            if (shouldSelect != (item.getSelected() != null && item.getSelected())) {
                item.setSelected(shouldSelect);
                notifyItemChanged(i);
            }
        }
    }

    /**
     * 获取当前选中的项
     */
    public List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<>();
        for (T item : mList) {
            if (item.getSelected() != null && item.getSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    /**
     * 获取当前选中的项（单选模式下返回第一个选中的，如果没有返回null）
     */
    public T getSelectedItem() {
        for (T item : mList) {
            if (item.getSelected() != null && item.getSelected()) {
                return item;
            }
        }
        return null;
    }

    /**
     * 设置所有项为未选中
     */
    @SuppressLint("NotifyDataSetChanged")
    public void clearSelection() {
        boolean changed = false;
        for (T item : mList) {
            if (item.getSelected() != null && item.getSelected()) {
                item.setSelected(false);
                changed = true;
            }
        }
        if (changed) {
            notifyDataSetChanged();
        }
    }

    /**
     * 设置选中项（多选模式）
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedItems(List<T> items) {
        if (CollectionUtil.isEmpty(items)) {
            clearSelection();
            return;
        }

        clearSelection();

        for (T targetItem : items) {
            for (T item : mList) {
                if (item.getPopupId() != null && item.getPopupId().equals(targetItem.getPopupId())) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 设置选中项（单选模式）
     */
    public void setSelectedItem(T targetItem) {
        if (targetItem == null) {
            clearSelection();
            return;
        }

        for (int i = 0; i < mList.size(); i++) {
            T item = mList.get(i);
            boolean shouldSelect = item.getPopupId() != null && item.getPopupId().equals(targetItem.getPopupId());
            if (shouldSelect != (item.getSelected() != null && item.getSelected())) {
                item.setSelected(shouldSelect);
                notifyItemChanged(i);
            }
        }
    }

    // ========== 链式设置方法 ==========

    /**
     * 设置选择模式
     */
    public PopupWindowAdapter<T> setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    /**
     * 设置文本颜色（未选中）
     */
    public PopupWindowAdapter<T> setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getLine() {
        return line;
    }

    public PopupWindowAdapter<T> setLine(int line) {
        this.line = line;
        return this;
    }

    /**
     * 设置文本选中颜色
     */
    public PopupWindowAdapter<T> setTextSelectedColor(@ColorInt int textSelectedColor) {
        this.textSelectedColor = textSelectedColor;
        return this;
    }

    /**
     * 设置文本大小（单位：sp）
     */
    public PopupWindowAdapter<T> setTextSizeSp(@Dimension(unit = Dimension.SP) float textSizeSp) {
        this.textSize = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_SP,
                textSizeSp,
                android.content.res.Resources.getSystem().getDisplayMetrics()
        );
        return this;
    }

    /**
     * 设置文本大小（单位：px）
     */
    public PopupWindowAdapter<T> setTextSizePx(@Dimension(unit = Dimension.PX) float textSizePx) {
        this.textSize = textSizePx;
        return this;
    }

    /**
     * 设置文本大小（从资源获取）
     */
    public PopupWindowAdapter<T> setTextSizeRes(@DimenRes int textSizeRes) {
        this.textSize = android.content.res.Resources.getSystem().getDimension(textSizeRes);
        return this;
    }

    /**
     * 设置文本对齐方式
     * @param gravity 例如：Gravity.START, Gravity.CENTER, Gravity.END 等
     */
    public PopupWindowAdapter<T> setTextGravity(int gravity) {
        this.textGravity = gravity | Gravity.CENTER_VERTICAL;
        return this;
    }

    /**
     * 设置背景（未选中）
     */
    public PopupWindowAdapter<T> setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        return this;
    }

    /**
     * 设置背景（未选中）- 通过资源ID
     */
    public PopupWindowAdapter<T> setBackgroundResource(int resId) {
        this.backgroundDrawable = ContextCompat.getDrawable(
                Config.getInstance().getApplication(),
                resId
        );
        return this;
    }

    /**
     * 设置背景选中
     */
    public PopupWindowAdapter<T> setBackgroundSelectedDrawable(Drawable backgroundSelectedDrawable) {
        this.backgroundSelectedDrawable = backgroundSelectedDrawable;
        return this;
    }

    /**
     * 设置背景选中 - 通过资源ID
     */
    public PopupWindowAdapter<T> setBackgroundSelectedResource(int resId) {
        this.backgroundSelectedDrawable = ContextCompat.getDrawable(
                Config.getInstance().getApplication(),
                resId
        );
        return this;
    }

    /**
     * 设置列表项高度（单位：px）
     */
    public PopupWindowAdapter<T> setItemHeight(@Dimension(unit = Dimension.PX) float itemHeight) {
        this.itemHeight = itemHeight;
        return this;
    }

    /**
     * 设置内边距（单位：px）
     */
    public PopupWindowAdapter<T> setPadding(
            @Dimension(unit = Dimension.PX) int left,
            @Dimension(unit = Dimension.PX) int top,
            @Dimension(unit = Dimension.PX) int right,
            @Dimension(unit = Dimension.PX) int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        return this;
    }

    /**
     * 设置左内边距（单位：px）
     */
    public PopupWindowAdapter<T> setPaddingLeft(@Dimension(unit = Dimension.PX) int paddingLeft) {
        this.paddingLeft = paddingLeft;
        return this;
    }

    /**
     * 设置右内边距（单位：px）
     */
    public PopupWindowAdapter<T> setPaddingRight(@Dimension(unit = Dimension.PX) int paddingRight) {
        this.paddingRight = paddingRight;
        return this;
    }

    /**
     * 设置上内边距（单位：px）
     */
    public PopupWindowAdapter<T> setPaddingTop(@Dimension(unit = Dimension.PX) int paddingTop) {
        this.paddingTop = paddingTop;
        return this;
    }

    /**
     * 设置下内边距（单位：px）
     */
    public PopupWindowAdapter<T> setPaddingBottom(@Dimension(unit = Dimension.PX) int paddingBottom) {
        this.paddingBottom = paddingBottom;
        return this;
    }

    /**
     * 设置外边距（单位：px）
     */
    public PopupWindowAdapter<T> setMargin(
            @Dimension(unit = Dimension.PX) int left,
            @Dimension(unit = Dimension.PX) int top,
            @Dimension(unit = Dimension.PX) int right,
            @Dimension(unit = Dimension.PX) int bottom) {
        this.marginStart = left;
        this.marginTop = top;
        this.marginEnd = right;
        this.marginBottom = bottom;
        return this;
    }

    /**
     * 设置左边距（单位：px）
     */
    public PopupWindowAdapter<T> setMarginStart(@Dimension(unit = Dimension.PX) int marginStart) {
        this.marginStart = marginStart;
        return this;
    }

    /**
     * 设置右边距（单位：px）
     */
    public PopupWindowAdapter<T> setMarginEnd(@Dimension(unit = Dimension.PX) int marginEnd) {
        this.marginEnd = marginEnd;
        return this;
    }

    /**
     * 设置上边距（单位：px）
     */
    public PopupWindowAdapter<T> setMarginTop(@Dimension(unit = Dimension.PX) int marginTop) {
        this.marginTop = marginTop;
        return this;
    }

    /**
     * 设置下边距（单位：px）
     */
    public PopupWindowAdapter<T> setMarginBottom(@Dimension(unit = Dimension.PX) int marginBottom) {
        this.marginBottom = marginBottom;
        return this;
    }

    /**
     * 设置是否启用默认点击逻辑
     */
    public PopupWindowAdapter<T> setEnableDefaultClick(boolean enableDefaultClick) {
        this.enableDefaultClick = enableDefaultClick;
        return this;
    }

    // ========== Getters ==========

    public int getSelectionMode() {
        return selectionMode;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public Integer getTextSelectedColor() {
        return textSelectedColor;
    }

    public Float getTextSize() {
        return textSize;
    }

    public int getTextGravity() {
        return textGravity;
    }

    public Drawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public Drawable getBackgroundSelectedDrawable() {
        return backgroundSelectedDrawable;
    }

    public Float getItemHeight() {
        return itemHeight;
    }

    public Integer getPaddingLeft() {
        return paddingLeft;
    }

    public Integer getPaddingRight() {
        return paddingRight;
    }

    public Integer getPaddingTop() {
        return paddingTop;
    }

    public Integer getPaddingBottom() {
        return paddingBottom;
    }

    public Integer getMarginStart() {
        return marginStart;
    }

    public Integer getMarginEnd() {
        return marginEnd;
    }

    public Integer getMarginTop() {
        return marginTop;
    }

    public Integer getMarginBottom() {
        return marginBottom;
    }

    public boolean isEnableDefaultClick() {
        return enableDefaultClick;
    }

    // ========== ViewHolder ==========

    public static class ViewHolder<T extends PopupWindowBean> extends BaseViewHolder<OptionTextViewBinding> {
        public ViewHolder(@NotNull OptionTextViewBinding binding, PopupWindowAdapter<T> adapter) {
            super(binding, adapter);
            // ViewHolder中不再设置固定样式，全部通过动态属性设置
        }
    }
}