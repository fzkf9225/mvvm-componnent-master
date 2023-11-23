package pers.fz.mvvm.wight.popupwindow.adapter;

import android.content.Context;

import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.PopupWindowMenuItemBinding;

/**
 * Created by fz on 2018/7/16.
 *
 */
public class MenuAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T,PopupWindowMenuItemBinding> {

    public MenuAdapter(Context mContext) {
        super(mContext);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.popup_window_menu_item;
    }

    @Override
    public void onBindHolder(BaseViewHolder<PopupWindowMenuItemBinding> holder, int pos) {
        holder.getBinding().tvParentCategoryName.setText(mList.get(pos).getName());
        holder.getBinding().imgIcon.setImageResource(R.mipmap.icon_ewm);
    }
}
