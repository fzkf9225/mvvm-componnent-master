package com.casic.otitan.demo.adapter;

import com.casic.otitan.demo.R;
import com.casic.otitan.demo.bean.UseCase;
import com.casic.otitan.demo.databinding.UseCaseItemBinding;

import java.util.List;

import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;

/**
 * Created by fz on 2023/8/14 10:39
 * describe :
 */
public class UseCaseAdapter extends BaseRecyclerViewAdapter<UseCase, UseCaseItemBinding> {
    public UseCaseAdapter() {
        super();
    }

    public UseCaseAdapter(List<UseCase> list) {
        super(list);
    }

    @Override
    public void onBindHolder(BaseViewHolder<UseCaseItemBinding> holder, int pos) {
        holder.getBinding().setUseCase(mList.get(pos));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.use_case_item;
    }
}
