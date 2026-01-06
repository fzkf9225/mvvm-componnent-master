package com.casic.otitan.common.widget.popupwindow.adapter;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;

import org.jetbrains.annotations.NotNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionCheckboxViewBinding;
import com.casic.otitan.common.utils.common.DensityUtil;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowCheckBoxAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionCheckboxViewBinding> {

    /**
     * 文本颜色
     */
    private @ColorInt int textColor;

    /**
     * 选中样式
     */
    private Drawable checkedDrawable;

    /**
     * 未选中样式
     */
    private Drawable uncheckedDrawable;
    /**
     * 列表项高度
     */
    private float itemHeight = 0;

    /**
     * 是否显示复选框
     */
    private boolean showCheckBox = false;

    private final PopupWindowSelectedAdapter<T> selectedAdapter;

    public PopupWindowCheckBoxAdapter(PopupWindowSelectedAdapter<T> selectedAdapter) {
        super();
        this.selectedAdapter = selectedAdapter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.option_checkbox_view;
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionCheckboxViewBinding> holder, int pos) {
        if (PopupWindowBean.containsById(selectedAdapter.getList(), mList.get(pos).getPopupId())) {
            holder.getBinding().ivCheck.setImageDrawable(checkedDrawable);
        } else {
            holder.getBinding().ivCheck.setImageDrawable(uncheckedDrawable);
        }
        holder.getBinding().setItem(mList.get(pos));
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected BaseViewHolder<OptionCheckboxViewBinding> createViewHold(OptionCheckboxViewBinding binding) {
        return new ViewHolder(binding, this);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
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

    public float getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
    }

    public boolean isShowCheckBox() {
        return showCheckBox;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
    }

    public static class ViewHolder<T extends PopupWindowBean> extends BaseViewHolder<OptionCheckboxViewBinding> {
        public ViewHolder(@NotNull OptionCheckboxViewBinding binding, PopupWindowCheckBoxAdapter<T> adapter) {
            super(binding, adapter);
            ViewGroup.MarginLayoutParams layoutParams;

            if (adapter.getItemHeight() <= 0) {
                layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dp2px(binding.getRoot().getContext(), 40f));
            } else {
                layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) adapter.getItemHeight());
            }
            binding.tvOption.setTextColor(adapter.getTextColor());
            binding.clItem.setLayoutParams(layoutParams);
            binding.tvOption.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            binding.ivCheck.setVisibility(adapter.isShowCheckBox() ? ViewGroup.VISIBLE : View.GONE);
            binding.ivCheck.setOnClickListener(v -> {
                if (adapter.getOnItemSelectedChangedListener() == null) {
                    return;
                }
                adapter.getOnItemSelectedChangedListener().onItemSelectedChanged((AppCompatImageView) v, getAbsoluteAdapterPosition());
            });
        }
    }

    public interface OnItemSelectedChangedListener {
        void onItemSelectedChanged(AppCompatImageView ivCheckView, int position);
    }

    private OnItemSelectedChangedListener onItemSelectedChangedListener;

    public void setOnItemSelectedChangedListener(OnItemSelectedChangedListener onItemSelectedChangedListener) {
        this.onItemSelectedChangedListener = onItemSelectedChangedListener;
    }

    public OnItemSelectedChangedListener getOnItemSelectedChangedListener() {
        return onItemSelectedChangedListener;
    }
}
