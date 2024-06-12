package pers.fz.mvvm.wight.popupwindow.adapter;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.ActivityParentCategoryItemBinding;
import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2018/7/11.
 */
public class ParentAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, ActivityParentCategoryItemBinding> {
    private int selectedPosition = -1;
    /**
     * 是否添加不限、全部
     */
    private boolean hasHeader = false;

    public ParentAdapter(Context mContext) {
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
        holder.getBinding().tvParentCategoryName.setText(mList.get(pos).getName());
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
    public int getHeaderViewId() {
        return R.layout.activity_parent_category_item;
    }

    @Override
    public void onBindHeaderHolder(BaseViewHolder holder) {
        super.onBindHeaderHolder(holder);
        ActivityParentCategoryItemBinding binding = (ActivityParentCategoryItemBinding) holder.getBinding();
        binding.tvParentCategoryName.setText("全部");
    }
}
