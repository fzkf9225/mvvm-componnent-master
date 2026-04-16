package io.coderf.arklab.common.adapter;

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
import androidx.databinding.ViewDataBinding;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.AdapterCheckBoxItemBinding;
import io.coderf.arklab.common.listener.OnHeaderViewClickListener;
import io.coderf.arklab.common.utils.common.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 复选框adapter - 支持单选/多选，支持头布局全选
 *
 * @author fz
 * @version 3.0
 * @since 3.0
 * @created 2026/3/12
 */
public class CheckBoxAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, AdapterCheckBoxItemBinding> {

    /**
     * 选择模式：多选
     */
    public static final int MODE_MULTI = 0;
    /**
     * 选择模式：单选
     */
    public static final int MODE_SINGLE = 1;

    /**
     * 当前选择模式，默认为多选
     */
    private int selectionMode = MODE_MULTI;
    /**
     * 输入框、选择框正文行数
     */
    protected int line = 1;

    /**
     * 文本颜色
     */
    private @ColorInt Integer textColor;

    /**
     * 文本大小（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float textSize;

    /**
     * 文本左边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer textMarginStart;

    /**
     * 文本右边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer textMarginEnd;

    /**
     * 文本上边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer textMarginTop;

    /**
     * 文本下边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer textMarginBottom;

    /**
     * 图标的左边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer iconMarginStart;

    /**
     * 图标的右边距（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Integer iconMarginEnd;

    /**
     * 选中样式
     */
    private Drawable checkedDrawable;

    /**
     * 未选中样式
     */
    private Drawable uncheckedDrawable;

    /**
     * 列表项高度（单位：px）
     */
    private @Dimension(unit = Dimension.PX) Float itemHeight;

    /**
     * 是否显示复选框
     */
    private boolean showCheckBox = true;

    /**
     * 是否启用默认的点击选择逻辑
     */
    private boolean enableDefaultClick = true;

    /**
     * 是否显示头布局
     */
    private boolean showHeader = false;

    /**
     * 头布局标题
     */
    private String headerTitle = "全部";

    /**
     * 头布局标题颜色
     */
    private @ColorInt Integer headerTextColor;

    /**
     * 头布局标题大小
     */
    private @Dimension(unit = Dimension.PX) Float headerTextSize;


    public CheckBoxAdapter() {
        super();
        initDefaultClickListener();
    }

    public CheckBoxAdapter(int selectionMode) {
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

        setOnHeaderViewClickListener(new OnHeaderViewClickListener() {
            @Override
            public void onHeaderViewClick(View view) {
                if (!enableDefaultClick) {
                    return;
                }
                if(selectionMode==MODE_SINGLE){
                    return;
                }
                // 默认的全选/取消全选逻辑
                toggleSelectAll();
            }

            @Override
            public void onHeaderViewLongClick(View view) {

            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adapter_check_box_item;
    }

    @Override
    public Integer getHeaderViewId() {
        return showHeader ? R.layout.adapter_check_box_item : null;
    }

    @Override
    public void onBindHeaderHolder(BaseViewHolder holder) {
        AdapterCheckBoxItemBinding headerBinding = (AdapterCheckBoxItemBinding) holder.getBinding();
        // 设置头布局标题
        headerBinding.tvOption.setText(headerTitle);

        // 设置头布局图标状态（全选/不全选）
        boolean allSelected = isAllSelected();
        if (allSelected) {
            if (checkedDrawable != null) {
                headerBinding.ivCheck.setImageDrawable(checkedDrawable);
            } else {
                headerBinding.ivCheck.setImageDrawable(
                        ContextCompat.getDrawable(headerBinding.getRoot().getContext(),
                                R.drawable.common_ic_circle_checked)
                );
            }
        } else {
            if (uncheckedDrawable != null) {
                headerBinding.ivCheck.setImageDrawable(uncheckedDrawable);
            } else {
                headerBinding.ivCheck.setImageDrawable(
                        ContextCompat.getDrawable(headerBinding.getRoot().getContext(),
                                R.drawable.common_ic_circle_unchecked)
                );
            }
        }
        // 关键：执行挂起的绑定，确保UI更新
        headerBinding.executePendingBindings();
        // 应用头布局的动态属性
        applyHeaderDynamicProperties(headerBinding);
    }

    @Override
    public void onBindHolder(BaseViewHolder<AdapterCheckBoxItemBinding> holder, int pos) {
        T item = mList.get(pos);

        // 设置选中状态图标
        if (item.getSelected() != null && item.getSelected()) {
            if (checkedDrawable == null) {
                holder.getBinding().ivCheck.setImageDrawable(ContextCompat.getDrawable(holder.getBinding().getRoot().getContext(), R.drawable.common_ic_circle_checked));
            } else {
                holder.getBinding().ivCheck.setImageDrawable(checkedDrawable);
            }
        } else {
            if (uncheckedDrawable == null) {
                holder.getBinding().ivCheck.setImageDrawable(ContextCompat.getDrawable(holder.getBinding().getRoot().getContext(), R.drawable.common_ic_circle_unchecked));
            } else {
                holder.getBinding().ivCheck.setImageDrawable(uncheckedDrawable);
            }
        }

        holder.getBinding().tvOption.setText(item.getPopupName());

        // 应用动态属性
        applyDynamicProperties(holder);
    }

    /**
     * 应用动态设置的属性到ViewHolder
     */
    private void applyDynamicProperties(BaseViewHolder<AdapterCheckBoxItemBinding> holder) {
        AdapterCheckBoxItemBinding binding = holder.getBinding();

        // 设置文本颜色
        if (textColor != null) {
            binding.tvOption.setTextColor(textColor);
        }
        if (line == 1) {
            binding.tvOption.setMaxLines(1);
            binding.tvOption.setEllipsize(TextUtils.TruncateAt.END);
        } else if (line > 1) {
            binding.tvOption.setMaxLines(line);
            binding.tvOption.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            binding.tvOption.setMaxLines(Integer.MAX_VALUE);
        }
        // 设置文本大小
        if (textSize != null) {
            binding.tvOption.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize);
        }

        // 设置文本边距
        ViewGroup.MarginLayoutParams tvParams = (ViewGroup.MarginLayoutParams) binding.tvOption.getLayoutParams();
        if (tvParams != null) {
            int left = textMarginStart != null ? textMarginStart : tvParams.leftMargin;
            int top = textMarginTop != null ? textMarginTop : tvParams.topMargin;
            int right = textMarginEnd != null ? textMarginEnd : tvParams.rightMargin;
            int bottom = textMarginBottom != null ? textMarginBottom : tvParams.bottomMargin;

            tvParams.setMargins(left, top, right, bottom);
            binding.tvOption.setLayoutParams(tvParams);
        }

        // 设置图标边距
        ViewGroup.MarginLayoutParams iconParams = (ViewGroup.MarginLayoutParams) binding.ivCheck.getLayoutParams();
        if (iconParams != null) {
            int left = iconMarginStart != null ? iconMarginStart : iconParams.leftMargin;
            int right = iconMarginEnd != null ? iconMarginEnd : iconParams.rightMargin;

            iconParams.setMargins(left, iconParams.topMargin, right, iconParams.bottomMargin);
            binding.ivCheck.setLayoutParams(iconParams);
        }
    }

    /**
     * 应用头布局的动态属性
     */
    private void applyHeaderDynamicProperties(AdapterCheckBoxItemBinding headerBinding) {
        // 设置头布局文本颜色
        if (headerTextColor != null) {
            headerBinding.tvOption.setTextColor(headerTextColor);
        }

        // 设置头布局文本大小
        if (headerTextSize != null) {
            headerBinding.tvOption.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, headerTextSize);
        }
        if (line == 1) {
            headerBinding.tvOption.setMaxLines(1);
        } else if (line > 1) {
            headerBinding.tvOption.setMaxLines(line);
        } else {
            headerBinding.tvOption.setMaxLines(Integer.MAX_VALUE);
        }
        // 设置头布局文本边距（与普通项保持一致）
        ViewGroup.MarginLayoutParams tvParams = (ViewGroup.MarginLayoutParams) headerBinding.tvOption.getLayoutParams();
        if (tvParams != null) {
            int left = textMarginStart != null ? textMarginStart : tvParams.leftMargin;
            int top = textMarginTop != null ? textMarginTop : tvParams.topMargin;
            int right = textMarginEnd != null ? textMarginEnd : tvParams.rightMargin;
            int bottom = textMarginBottom != null ? textMarginBottom : tvParams.bottomMargin;

            tvParams.setMargins(left, top, right, bottom);
            headerBinding.tvOption.setLayoutParams(tvParams);
        }

        // 设置头布局图标边距（与普通项保持一致）
        ViewGroup.MarginLayoutParams iconParams = (ViewGroup.MarginLayoutParams) headerBinding.ivCheck.getLayoutParams();
        if (iconParams != null) {
            int left = iconMarginStart != null ? iconMarginStart : iconParams.leftMargin;
            int right = iconMarginEnd != null ? iconMarginEnd : iconParams.rightMargin;

            iconParams.setMargins(left, iconParams.topMargin, right, iconParams.bottomMargin);
            headerBinding.ivCheck.setLayoutParams(iconParams);
        }
    }

    public int getLine() {
        return line;
    }

    public CheckBoxAdapter<T> setLine(int line) {
        this.line = line;
        return this;
    }

    @Override
    protected BaseViewHolder<AdapterCheckBoxItemBinding> createViewHold(AdapterCheckBoxItemBinding binding) {
        return new ItemViewHolder<>(binding, this);
    }

    @Override
    protected <HVDB extends ViewDataBinding> BaseViewHolder<HVDB> createHeaderViewHold(HVDB binding) {
        return new HeaderViewHolder((AdapterCheckBoxItemBinding) binding, this);
    }

    /**
     * 判断是否所有项都被选中
     */
    private boolean isAllSelected() {
        if (CollectionUtil.isEmpty(mList)) {
            return false;
        }
        for (T item : mList) {
            if (item.getSelected() == null || !item.getSelected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 切换全选状态
     */
    private void toggleSelectAll() {
        boolean allSelected = isAllSelected();
        for (T item : mList) {
            item.setSelected(!allSelected);
        }
        notifyDataSetChanged();
    }

    /**
     * 切换项的选择状态（多选模式）
     */
    private void toggleSelection(int position) {
        T item = mList.get(position);
        boolean newSelected = item.getSelected() == null || !item.getSelected();
        item.setSelected(newSelected);
        notifyItemChanged(hasHeaderView() ? position + 1 : position);

        // 如果显示头布局，更新头布局的选中状态
        if (showHeader) {
            notifyItemChanged(0);
        }
    }

    /**
     * 单选模式：选择一项，取消其他项
     */
    private void selectSingle(int position) {
        T clickedItem = mList.get(position);
        boolean willBeSelected = clickedItem.getSelected() == null || !clickedItem.getSelected();

        if (!willBeSelected && clickedItem.getSelected() != null && clickedItem.getSelected()) {
            clickedItem.setSelected(false);
            notifyItemChanged(hasHeaderView() ? position + 1 : position);

            // 如果显示头布局，更新头布局的选中状态
            if (showHeader) {
                notifyItemChanged(0);
            }
            return;
        }

        for (int i = 0; i < mList.size(); i++) {
            T item = mList.get(i);
            boolean shouldSelect = (i == position);
            if (shouldSelect != (item.getSelected() != null && item.getSelected())) {
                item.setSelected(shouldSelect);
                notifyItemChanged(hasHeaderView() ? i + 1 : i);
            }
        }

        // 如果显示头布局，更新头布局的选中状态
        if (showHeader) {
            notifyItemChanged(0);
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
                notifyItemChanged(hasHeaderView() ? i + 1 : i);
            }
        }

        // 如果显示头布局，更新头布局的选中状态
        if (showHeader) {
            notifyItemChanged(0);
        }
    }

    // ========== 头布局相关方法 ==========

    /**
     * 设置是否显示头布局
     */
    public CheckBoxAdapter<T> setShowHeader(boolean showHeader) {
        if (this.showHeader != showHeader) {
            this.showHeader = showHeader;
            if (showHeader) {
                notifyItemInserted(0);
            } else {
                notifyItemRemoved(0);
            }
        }
        return this;
    }

    /**
     * 设置头布局标题
     */
    public CheckBoxAdapter<T> setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
        if (showHeader) {
            notifyItemChanged(0);
        }
        return this;
    }

    /**
     * 设置头布局标题颜色
     */
    public CheckBoxAdapter<T> setHeaderTextColor(@ColorInt int headerTextColor) {
        this.headerTextColor = headerTextColor;
        if (showHeader) {
            notifyItemChanged(0);
        }
        return this;
    }

    /**
     * 设置头布局标题大小（单位：sp）
     */
    public CheckBoxAdapter<T> setHeaderTextSizeSp(@Dimension(unit = Dimension.SP) float headerTextSizeSp) {
        this.headerTextSize = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_SP,
                headerTextSizeSp,
                android.content.res.Resources.getSystem().getDisplayMetrics()
        );
        if (showHeader) {
            notifyItemChanged(0);
        }
        return this;
    }

    /**
     * 设置头布局标题大小（单位：px）
     */
    public CheckBoxAdapter<T> setHeaderTextSizePx(@Dimension(unit = Dimension.PX) float headerTextSizePx) {
        this.headerTextSize = headerTextSizePx;
        if (showHeader) {
            notifyItemChanged(0);
        }
        return this;
    }

    // ========== 链式设置方法 ==========

    /**
     * 设置文本颜色
     */
    public CheckBoxAdapter<T> setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * 设置文本大小（单位：sp）
     */
    public CheckBoxAdapter<T> setTextSizeSp(@Dimension(unit = Dimension.SP) float textSizeSp) {
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
    public CheckBoxAdapter<T> setTextSizePx(@Dimension(unit = Dimension.PX) float textSizePx) {
        this.textSize = textSizePx;
        return this;
    }

    /**
     * 设置文本大小（从资源获取）
     */
    public CheckBoxAdapter<T> setTextSizeRes(@DimenRes int textSizeRes) {
        this.textSize = android.content.res.Resources.getSystem().getDimension(textSizeRes);
        return this;
    }

    /**
     * 设置文本左边距（单位：px）
     */
    public CheckBoxAdapter<T> setTextMarginStart(@Dimension(unit = Dimension.PX) int marginStart) {
        this.textMarginStart = marginStart;
        return this;
    }

    /**
     * 设置文本右边距（单位：px）
     */
    public CheckBoxAdapter<T> setTextMarginEnd(@Dimension(unit = Dimension.PX) int marginEnd) {
        this.textMarginEnd = marginEnd;
        return this;
    }

    /**
     * 设置文本上边距（单位：px）
     */
    public CheckBoxAdapter<T> setTextMarginTop(@Dimension(unit = Dimension.PX) int marginTop) {
        this.textMarginTop = marginTop;
        return this;
    }

    /**
     * 设置文本下边距（单位：px）
     */
    public CheckBoxAdapter<T> setTextMarginBottom(@Dimension(unit = Dimension.PX) int marginBottom) {
        this.textMarginBottom = marginBottom;
        return this;
    }

    /**
     * 设置文本所有边距（单位：px）
     */
    public CheckBoxAdapter<T> setTextMargin(
            @Dimension(unit = Dimension.PX) int left,
            @Dimension(unit = Dimension.PX) int top,
            @Dimension(unit = Dimension.PX) int right,
            @Dimension(unit = Dimension.PX) int bottom) {
        this.textMarginStart = left;
        this.textMarginTop = top;
        this.textMarginEnd = right;
        this.textMarginBottom = bottom;
        return this;
    }

    /**
     * 设置图标左边距（单位：px）
     */
    public CheckBoxAdapter<T> setIconMarginStart(@Dimension(unit = Dimension.PX) int marginStart) {
        this.iconMarginStart = marginStart;
        return this;
    }

    /**
     * 设置图标右边距（单位：px）
     */
    public CheckBoxAdapter<T> setIconMarginEnd(@Dimension(unit = Dimension.PX) int marginEnd) {
        this.iconMarginEnd = marginEnd;
        return this;
    }

    /**
     * 设置图标左右边距（单位：px）
     */
    public CheckBoxAdapter<T> setIconMargin(
            @Dimension(unit = Dimension.PX) int left,
            @Dimension(unit = Dimension.PX) int right) {
        this.iconMarginStart = left;
        this.iconMarginEnd = right;
        return this;
    }

    // ========== Getters and Setters ==========

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
    }

    public boolean isEnableDefaultClick() {
        return enableDefaultClick;
    }

    public void setEnableDefaultClick(boolean enableDefaultClick) {
        this.enableDefaultClick = enableDefaultClick;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public Float getTextSize() {
        return textSize;
    }

    public Integer getTextMarginStart() {
        return textMarginStart;
    }

    public Integer getTextMarginEnd() {
        return textMarginEnd;
    }

    public Integer getTextMarginTop() {
        return textMarginTop;
    }

    public Integer getTextMarginBottom() {
        return textMarginBottom;
    }

    public Integer getIconMarginStart() {
        return iconMarginStart;
    }

    public Integer getIconMarginEnd() {
        return iconMarginEnd;
    }

    public Drawable getCheckedDrawable() {
        return checkedDrawable;
    }

    public void setCheckedDrawable(Drawable checkedDrawable) {
        this.checkedDrawable = checkedDrawable;
    }

    public Drawable getUncheckedDrawable() {
        return uncheckedDrawable;
    }

    public void setUncheckedDrawable(Drawable uncheckedDrawable) {
        this.uncheckedDrawable = uncheckedDrawable;
    }

    public Float getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(@Dimension(unit = Dimension.PX) float itemHeight) {
        this.itemHeight = itemHeight;
    }

    public boolean isShowCheckBox() {
        return showCheckBox;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public Integer getHeaderTextColor() {
        return headerTextColor;
    }

    public Float getHeaderTextSize() {
        return headerTextSize;
    }

    // ========== ViewHolder 内部类 ==========

    /**
     * 普通项ViewHolder
     */
    public static class ItemViewHolder<T extends PopupWindowBean> extends BaseViewHolder<AdapterCheckBoxItemBinding> {
        public ItemViewHolder(@NotNull AdapterCheckBoxItemBinding binding, CheckBoxAdapter<T> adapter) {
            super(binding, adapter);

            // 设置item高度
            if (adapter.getItemHeight() != null && adapter.getItemHeight() > 0) {
                ViewGroup.LayoutParams params = binding.clItem.getLayoutParams();
                params.height = Math.round(adapter.getItemHeight());
                binding.clItem.setLayoutParams(params);
            }

            // 设置复选框显示状态
            binding.ivCheck.setVisibility(adapter.isShowCheckBox() ? View.VISIBLE : View.GONE);
            // 设置文本对齐方式
            binding.tvOption.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }

    /**
     * 头布局ViewHolder
     */
    public static class HeaderViewHolder<T extends PopupWindowBean> extends BaseViewHolder<AdapterCheckBoxItemBinding> {
        public HeaderViewHolder(@NotNull AdapterCheckBoxItemBinding binding, CheckBoxAdapter<T> adapter) {
            super(binding, true, adapter);
            // 设置item高度
            if (adapter.getItemHeight() != null && adapter.getItemHeight() > 0) {
                ViewGroup.LayoutParams params = binding.clItem.getLayoutParams();
                params.height = Math.round(adapter.getItemHeight());
                binding.clItem.setLayoutParams(params);
            }

            // 设置复选框显示状态
            binding.ivCheck.setVisibility(adapter.isShowCheckBox() ? View.VISIBLE : View.GONE);
            // 设置文本对齐方式
            binding.tvOption.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }

}