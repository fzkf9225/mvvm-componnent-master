package com.casic.otitan.common.adapter;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionTextViewBinding;

/**
 * Created by fz on 2019/10/31.
 * describe：底部选择菜单
 */
public class MenuListAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T,OptionTextViewBinding> {

    public MenuListAdapter() {
        super();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.option_text_view;
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> viewHolder, int pos) {
        viewHolder.getBinding().setItem(mList.get(pos));
        viewHolder.getBinding().executePendingBindings();
    }
}
