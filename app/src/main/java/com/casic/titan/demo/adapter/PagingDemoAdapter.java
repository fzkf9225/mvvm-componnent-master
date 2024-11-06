package com.casic.titan.demo.adapter;


import com.casic.titan.demo.R;
import com.casic.titan.demo.BR;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.databinding.PagingItemBinding;

import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.base.DefaultDiffCallback;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingDemoAdapter extends BasePagingAdapter<ForestBean, PagingItemBinding> {
    public PagingDemoAdapter() {
        super(new DefaultDiffCallback<>());
    }

    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder,ForestBean item, int pos) {
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.paging_item;
    }
}
