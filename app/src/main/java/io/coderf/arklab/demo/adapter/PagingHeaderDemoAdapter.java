package io.coderf.arklab.demo.adapter;

import io.coderf.arklab.demo.BR;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.NotificationMessageBean;
import io.coderf.arklab.demo.databinding.PagingItemBinding;

import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.base.DefaultDiffCallback;
import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * Created by fz on 2023/12/1 16:50
 * describe :
 */
public class PagingHeaderDemoAdapter extends BasePagingAdapter<NotificationMessageBean, PagingItemBinding> {
    public PagingHeaderDemoAdapter() {
        super(new DefaultDiffCallback());
    }
    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder, NotificationMessageBean item, int pos) {
        LogUtil.show(ApiRetrofit.TAG,"第："+pos+"行，"+item.getTitle());
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.paging_item;
    }

}
