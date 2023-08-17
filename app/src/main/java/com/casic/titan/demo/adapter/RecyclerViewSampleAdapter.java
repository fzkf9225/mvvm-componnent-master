package com.casic.titan.demo.adapter;

import android.content.Context;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.UseCaseItemBinding;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;

/**
 * Created by fz on 2023/8/14 10:39
 * describe :
 */
public class RecyclerViewSampleAdapter extends BaseRecyclerViewAdapter<PopupWindowBean, OptionTextViewBinding> {
    public RecyclerViewSampleAdapter(Context context) {
        super(context);
    }

    public RecyclerViewSampleAdapter(Context context, List<PopupWindowBean> list) {
        super(context, list);
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        holder.getBinding().setItem(mList.get(pos));
    }

    @Override
    protected int getLayoutId() {
        return pers.fz.mvvm.R.layout.option_text_view;
    }
}
