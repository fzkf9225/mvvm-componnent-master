package com.casic.titan.demo.adapter;


import com.bumptech.glide.Glide;
import com.casic.titan.demo.BR;
import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.databinding.PagingItemBinding;
import com.casic.titan.demo.databinding.RecyclerHeaderViewBinding;

import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.base.DefaultDiffCallback;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingHeaderDemoAdapter extends BasePagingAdapter<ForestBean, PagingItemBinding> {
    public PagingHeaderDemoAdapter() {
        super(new DefaultDiffCallback());
    }
    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder,ForestBean item, int pos) {
        LogUtil.show(ApiRetrofit.TAG,"第："+pos+"行，"+item.getCaretaker());
        holder.getBinding().setVariable(BR.item, item);
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
