package com.casic.titan.demo.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.casic.titan.demo.R;
import com.casic.titan.demo.BR;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.databinding.PagingItemBinding;

import java.util.Objects;

import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BaseViewHolder;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingDemoAdapter extends BasePagingAdapter<ForestBean, PagingItemBinding> {
    public PagingDemoAdapter() {
        super(COMPARATOR);
    }

    private static final DiffUtil.ItemCallback<ForestBean> COMPARATOR = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ForestBean oldItem, @NonNull ForestBean newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ForestBean oldItem, @NonNull ForestBean newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder, int pos) {
        holder.getBinding().setVariable(BR.item, getItem(pos));
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.paging_item;
    }

}
