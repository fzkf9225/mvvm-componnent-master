package com.casic.titan.demo.adapter;

import com.bumptech.glide.Glide;
import com.casic.titan.demo.R;
import com.casic.titan.demo.databinding.RecyclerHeaderViewBinding;

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
    public RecyclerViewSampleAdapter() {
        super();
    }

    public RecyclerViewSampleAdapter( List<PopupWindowBean> list) {
        super(list);
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        holder.getBinding().setItem(mList.get(pos));
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return pers.fz.mvvm.R.layout.option_text_view;
    }

    @Override
    public Integer getHeaderViewId() {
        return R.layout.recycler_header_view;
    }

    @Override
    public void onBindHeaderHolder(BaseViewHolder holder) {
        super.onBindHeaderHolder(holder);
        RecyclerHeaderViewBinding binding = (RecyclerHeaderViewBinding) holder.getBinding();
        Glide.with(binding.image.getContext())
                .load("https://img2.baidu.com/it/u=1816408595,1545501487&fm=253&fmt=auto&app=120&f=JPEG?w=607&h=347")
                .into(binding.image);
    }
}
