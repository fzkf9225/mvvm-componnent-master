package io.coderf.arklab.demo.adapter;

import com.bumptech.glide.Glide;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.RecyclerHeaderViewBinding;

import java.util.List;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.OptionTextViewBinding;

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
        return io.coderf.arklab.common.R.layout.option_text_view;
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
