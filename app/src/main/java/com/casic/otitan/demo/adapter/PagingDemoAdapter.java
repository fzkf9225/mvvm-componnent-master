package com.casic.otitan.demo.adapter;


import com.casic.otitan.demo.BR;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.NotificationMessageBean;
import com.casic.otitan.demo.databinding.PagingItemBinding;

import com.casic.otitan.common.base.BasePagingAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.base.DefaultDiffCallback;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingDemoAdapter extends BasePagingAdapter<NotificationMessageBean, PagingItemBinding> {
    public PagingDemoAdapter() {
        super(new DefaultDiffCallback<>());
    }

    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder, NotificationMessageBean item, int pos) {
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.paging_item;
    }
}
