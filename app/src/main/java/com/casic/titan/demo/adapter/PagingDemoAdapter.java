package com.casic.titan.demo.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;

import com.bumptech.glide.Glide;
import com.casic.titan.demo.R;
import com.casic.titan.demo.BR;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.databinding.PagingItemBinding;
import com.casic.titan.demo.databinding.RecyclerHeaderViewBinding;

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

    @Override
    public Integer getHeaderViewId() {
        return R.layout.recycler_header_view;
    }

    @Override
    public void onBindHeaderHolder(BaseViewHolder holder) {
        super.onBindHeaderHolder(holder);
        RecyclerHeaderViewBinding binding = (RecyclerHeaderViewBinding) holder.getBinding();
        Glide.with(holder.getBinding().getRoot().getContext())
                .load("https://img2.baidu.com/it/u=1816408595,1545501487&fm=253&fmt=auto&app=120&f=JPEG?w=607&h=347")
                .into(binding.image);
    }
}
