package io.coderf.arklab.demo.adapter;

import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.UseCase;
import io.coderf.arklab.demo.databinding.UseCaseItemBinding;

import java.util.List;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;

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
