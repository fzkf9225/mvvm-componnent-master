package pers.fz.mvvm.adapter;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;

/**
 * Created by fz on 2019/10/31.
 * describe：底部选择菜单
 */
public class OptionBottomMenuListAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {

    public OptionBottomMenuListAdapter() {
        super();
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        holder.getBinding().setItem(mList.get(pos));
    }

    @Override
    public int getLayoutId() {
        return R.layout.option_text_view;
    }


}
