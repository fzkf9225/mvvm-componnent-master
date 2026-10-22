package io.coderf.arklab.demo.adapter;

import io.coderf.arklab.common.adapter.PagingLiveAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.BR;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.NotificationMessageBean;
import io.coderf.arklab.demo.databinding.PagingItemBinding;

/**
 * 与 {@link PagingDemoAdapter} 同布局、同类型，作为 Paging 实时尾演示。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/22
 */
public class PagingLiveDemoAdapter extends PagingLiveAdapter<NotificationMessageBean, PagingItemBinding> {

    @Override
    public void onBindHolder(BaseViewHolder<PagingItemBinding> holder, int pos) {
        NotificationMessageBean item = getItem(pos);
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.paging_item;
    }
}
