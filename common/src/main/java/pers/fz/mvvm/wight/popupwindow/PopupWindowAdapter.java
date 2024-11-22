package pers.fz.mvvm.wight.popupwindow;

import android.content.Context;

import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.ActivityParentCategoryItemBinding;


/**
 * updated by fz on 2024/11/22.
 */
public class PopupWindowAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, ActivityParentCategoryItemBinding> {
    private int selectedPosition = -1;
    /**
     * 是否添加不限、全部
     */
    private boolean hasHeader = false;

    public PopupWindowAdapter(Context mContext) {
        super(mContext);
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_parent_category_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ActivityParentCategoryItemBinding> holder, int pos) {
        holder.getBinding().tvParentCategoryName.setText(mList.get(pos).getPopupName());
        if (pos == selectedPosition && selectedPosition >= 0) {
            holder.getBinding().tvParentCategoryName.setTextColor(ContextCompat.getColor(mContext, R.color.themeColor));
        } else {
            holder.getBinding().tvParentCategoryName.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public Integer getHeaderViewId() {
        return R.layout.activity_parent_category_item;
    }

    @Override
    public void onBindHeaderHolder(BaseViewHolder holder) {
        super.onBindHeaderHolder(holder);
        ActivityParentCategoryItemBinding binding = (ActivityParentCategoryItemBinding) holder.getBinding();
        binding.tvParentCategoryName.setText("全部");
    }
}
