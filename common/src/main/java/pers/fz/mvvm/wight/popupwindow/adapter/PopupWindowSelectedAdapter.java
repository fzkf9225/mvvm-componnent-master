package pers.fz.mvvm.wight.popupwindow.adapter;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.jetbrains.annotations.NotNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionSelectedViewBinding;
import pers.fz.mvvm.util.common.DensityUtil;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowSelectedAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionSelectedViewBinding> {

    private @ColorInt int selectTextColor;

    private @ColorInt int unSelectTextColor;

    private float radius;

    private @ColorInt int selectBgColor;

    private @ColorInt int unSelectBgColor;

    private float paddingStart,paddingTop,paddingEnd,paddingBottom;

    public PopupWindowSelectedAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.option_selected_view;
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionSelectedViewBinding> holder, int pos) {
        if (pos == mList.size() - 1) {
            holder.getBinding().tvOption.setTextColor(selectTextColor);
            holder.getBinding().tvOption.setBgColorAndRadius(selectBgColor,radius);
        } else {
            holder.getBinding().tvOption.setTextColor(unSelectTextColor);
            holder.getBinding().tvOption.setBgColorAndRadius(unSelectBgColor,radius);
        }
        holder.getBinding().tvOption.setText(mList.get(pos).getPopupName());
    }

    @Override
    protected BaseViewHolder<OptionSelectedViewBinding> createViewHold(OptionSelectedViewBinding binding) {
        return new ViewHolder(binding, this);
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

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setSelectBgColor(int selectBgColor) {
        this.selectBgColor = selectBgColor;
    }

    public void setUnSelectBgColor(int unSelectBgColor) {
        this.unSelectBgColor = unSelectBgColor;
    }

    public void setPadding(float paddingStart, float paddingTop, float paddingEnd, float paddingBottom) {
        this.paddingStart = paddingStart;
        this.paddingTop = paddingTop;
        this.paddingEnd = paddingEnd;
        this.paddingBottom = paddingBottom;
    }

    public float getPaddingEnd() {
        return paddingEnd;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public float getPaddingStart() {
        return paddingStart;
    }

    public static class ViewHolder<T extends PopupWindowBean> extends BaseViewHolder<OptionSelectedViewBinding> {
        public ViewHolder(@NotNull OptionSelectedViewBinding binding, PopupWindowSelectedAdapter<T> adapter) {
            super(binding, adapter);
            ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMarginEnd(DensityUtil.dp2px(binding.getRoot().getContext(), 12f));
            binding.clItem.setLayoutParams(layoutParams);

            binding.tvOption.setGravity(Gravity.CENTER);
            binding.tvOption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            binding.tvOption.setPadding(
                    (int) adapter.getPaddingStart(),
                    (int) adapter.getPaddingTop(),
                    (int) adapter.getPaddingEnd(),
                    (int) adapter.getPaddingBottom()
            );

            binding.ivClear.setOnClickListener(v -> {
                if (adapter.getOnItemSelectedClearListener() == null) {
                    return;
                }
                adapter.getOnItemSelectedClearListener().onItemSelectedClear((AppCompatImageView) v, getAbsoluteAdapterPosition());
            });
        }
    }

    public interface OnItemSelectedClearListener {
        void onItemSelectedClear(AppCompatImageView ivCheckView, int position);
    }

    private OnItemSelectedClearListener onItemSelectedClearListener;

    public void setOnItemSelectedClearListener(OnItemSelectedClearListener onItemSelectedClearListener) {
        this.onItemSelectedClearListener = onItemSelectedClearListener;
    }

    public OnItemSelectedClearListener getOnItemSelectedClearListener() {
        return onItemSelectedClearListener;
    }
}
