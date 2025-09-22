package com.casic.otitan.demo.adapter;

import com.bumptech.glide.Glide;
import com.casic.otitan.demo.R;
import com.casic.otitan.demo.databinding.RecyclerHeaderViewBinding;

import java.util.List;

import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionTextViewBinding;

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
        return com.casic.otitan.common.R.layout.option_text_view;
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
