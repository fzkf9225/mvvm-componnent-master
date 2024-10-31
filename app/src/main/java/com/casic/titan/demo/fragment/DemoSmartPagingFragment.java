package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.adapter.PagingDemoAdapter;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.viewmodel.DemoPagingViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BaseSmartPagingFragment;
import pers.fz.mvvm.databinding.BaseSmartPagingBinding;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
public class DemoSmartPagingFragment extends BaseSmartPagingFragment<DemoPagingViewModel, BaseSmartPagingBinding, ForestBean> {

    @Override
    protected BasePagingAdapter<ForestBean, ?> getRecyclerAdapter() {
        return new PagingDemoAdapter();
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, responseBean -> adapter.submitData(getLifecycle(), responseBean));
    }

    @Override
    public void onItemClick(View view, ForestBean item, int position) {
        super.onItemClick(view, item, position);
        showToast("点击的是第" + position + "行，内容是：" + item.getCaretaker());
    }

    @Override
    public void onItemLongClick(View view, ForestBean item, int position) {
        super.onItemLongClick(view, item, position);
        //不能这么删除，这样删除会有bug
        new ConfirmDialog(requireContext())
                .setSureText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnSureClickListener(dialog -> adapter.notifyItemRemoved(position+1))
                .builder()
                .show();
    }

}
