package com.casic.otitan.common.widget.popupwindow.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;

import androidx.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionTextViewBinding;
import com.casic.otitan.common.utils.common.DensityUtil;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {
    private int selectedPosition = -1;

    /**
     * 选中文字样式
     */
    private @ColorInt int selectTextColor;

    /**
     * 未选中文字样式
     */
    private @ColorInt int unSelectTextColor;

    /**
     * 选中背景样式
     */
    private @ColorInt int selectBgColor;

    /**
     * 未选中背景样式
     */
    private @ColorInt int unSelectBgColor;

    /**
     * 列表项高度
     */
    private float itemHeight = 0;

    public PopupWindowAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.option_text_view;
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        holder.getBinding().setItem(mList.get(pos));
        holder.getBinding().executePendingBindings();
        if (pos == selectedPosition && selectedPosition >= 0) {
            holder.getBinding().tvOption.setTextColor(selectTextColor);
            holder.getBinding().tvOption.setBackgroundColor(selectBgColor);
        } else {
            holder.getBinding().tvOption.setTextColor(unSelectTextColor);
            holder.getBinding().tvOption.setBackgroundColor(unSelectBgColor);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @Override
    protected BaseViewHolder<OptionTextViewBinding> createViewHold(OptionTextViewBinding binding) {
        return new ViewHolder(binding, this);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getSelectTextColor() {
        return selectTextColor;
    }

    public void setSelectTextColor(@ColorInt int selectTextColor) {
        this.selectTextColor = selectTextColor;
    }

    public int getUnSelectTextColor() {
        return unSelectTextColor;
    }

    public void setUnSelectTextColor(@ColorInt int unSelectTextColor) {
        this.unSelectTextColor = unSelectTextColor;
    }

    public int getSelectBgColor() {
        return selectBgColor;
    }

    public void setSelectBgColor(@ColorInt int selectBgColor) {
        this.selectBgColor = selectBgColor;
    }

    public int getUnSelectBgColor() {
        return unSelectBgColor;
    }

    public void setUnSelectBgColor(@ColorInt int unSelectBgColor) {
        this.unSelectBgColor = unSelectBgColor;
    }

    public float getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
    }

    public static class ViewHolder<T extends PopupWindowBean> extends BaseViewHolder<OptionTextViewBinding> {
        public ViewHolder(@NotNull OptionTextViewBinding binding, PopupWindowAdapter<T> adapter) {
            super(binding, adapter);
            if (adapter.getItemHeight() <= 0) {
                binding.tvOption.setHeight(DensityUtil.dp2px(binding.getRoot().getContext(), 40f));
            } else {
                binding.tvOption.setHeight((int) adapter.getItemHeight());
            }
            binding.tvOption.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            binding.tvOption.setPadding(DensityUtil.dp2px(binding.getRoot().getContext(), 12), 0, DensityUtil.dp2px(binding.getRoot().getContext(), 12), 0);
        }
    }

}
