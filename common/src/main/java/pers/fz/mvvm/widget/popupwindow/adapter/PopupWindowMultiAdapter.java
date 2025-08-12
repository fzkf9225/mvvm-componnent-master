package pers.fz.mvvm.widget.popupwindow.adapter;

import android.graphics.drawable.Drawable;
import android.view.Gravity;

import androidx.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowMultiAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {
    private final List<T> selected = new ArrayList<>();

    private @ColorInt int selectTextColor;

    private @ColorInt int unSelectTextColor;

    private Drawable selectBgDrawable;

    private Drawable unSelectBgDrawable;

    public PopupWindowMultiAdapter() {
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
        if (selected.contains(mList.get(pos))) {
            holder.getBinding().tvOption.setTextColor(selectTextColor);
            holder.getBinding().tvOption.setBackgroundDrawable(selectBgDrawable);
        } else {
            holder.getBinding().tvOption.setTextColor(unSelectTextColor);
            holder.getBinding().tvOption.setBackgroundDrawable(unSelectBgDrawable);
        }
    }


    @Override
    protected BaseViewHolder<OptionTextViewBinding> createViewHold(OptionTextViewBinding binding) {
        return new ViewHolder(binding, this);
    }

    public List<T> getSelected() {
        return selected;
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

    public Drawable getSelectBgDrawable() {
        return selectBgDrawable;
    }

    public void setSelectBgDrawable(Drawable selectBgDrawable) {
        this.selectBgDrawable = selectBgDrawable;
    }

    public Drawable getUnSelectBgDrawable() {
        return unSelectBgDrawable;
    }

    public void setUnSelectBgDrawable(Drawable unSelectBgDrawable) {
        this.unSelectBgDrawable = unSelectBgDrawable;
    }

    public static class ViewHolder<T extends PopupWindowBean> extends BaseViewHolder<OptionTextViewBinding> {
        public ViewHolder(@NotNull OptionTextViewBinding binding, PopupWindowMultiAdapter<T> adapter) {
            super(binding, adapter);
            binding.tvOption.setGravity(Gravity.CENTER);
            binding.tvOption.setPadding(
                    DensityUtil.dp2px(binding.getRoot().getContext(), 6f),
                    DensityUtil.dp2px(binding.getRoot().getContext(), 6f),
                    DensityUtil.dp2px(binding.getRoot().getContext(), 6f),
                    DensityUtil.dp2px(binding.getRoot().getContext(), 6f)
            );
        }
    }

}
