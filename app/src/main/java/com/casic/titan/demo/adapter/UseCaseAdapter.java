package com.casic.titan.demo.adapter;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.UseCaseItemBinding;

import java.util.List;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;

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
