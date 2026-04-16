package io.coderf.arklab.demo.adapter;


import io.coderf.arklab.demo.BR;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.NotificationMessageBean;
import io.coderf.arklab.demo.databinding.PagingItemBinding;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.base.DefaultDiffCallback;

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
