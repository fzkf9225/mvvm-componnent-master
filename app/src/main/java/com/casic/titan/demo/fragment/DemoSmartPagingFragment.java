package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.adapter.PagingDemoAdapter;
import com.casic.titan.demo.bean.NotificationMessageBean;
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
public class DemoSmartPagingFragment extends BaseSmartPagingFragment<DemoPagingViewModel, BaseSmartPagingBinding, NotificationMessageBean> {

    @Override
    protected BasePagingAdapter<NotificationMessageBean, ?> getRecyclerAdapter() {
        return new PagingDemoAdapter();
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, responseBean -> adapter.submitData(getLifecycle(), responseBean));
    }

    @Override
    public void onItemClick(View view, NotificationMessageBean item, int position) {
        super.onItemClick(view, item, position);
        showToast("点击的是第" + position + "行，内容是：" + item.getTitle());
    }

    @Override
    public void onItemLongClick(View view, NotificationMessageBean item, int position) {
        super.onItemLongClick(view, item, position);
        //不可以这样删除，因为这样值删除了列表项，但是真是数据没有删除
        new ConfirmDialog(requireContext())
                .setPositiveText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnPositiveClickListener(dialog -> {
                    adapter.notifyItemRemoved(position);
                })
                .builder()
                .show();
    }
}
