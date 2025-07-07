package pers.fz.mvvm.wight.popupwindow.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;

import androidx.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {
    private int selectedPosition = -1;

    private @ColorInt int selectTextColor;

    private @ColorInt int unSelectTextColor;

    private @ColorInt int selectBgColor;

    private @ColorInt int unSelectBgColor;

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
