package io.coderf.arklab.demo.fragment;

import android.os.Bundle;
import android.view.View;

import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.demo.adapter.DemoSmartPagingRatioHeaderAdapter;
import io.coderf.arklab.demo.adapter.PagingDemoAdapter;
import io.coderf.arklab.demo.bean.NotificationMessageBean;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.demo.viewmodel.DemoPagingViewModel;

/**
 * 在 {@link DemoSmartPagingFragment} 基础上增加列表头（示例为 16:9 图片），分页与空态逻辑与父类一致。
 */
@AndroidEntryPoint
public class DemoSmartPagingHeaderFragment extends BaseSmartPagingFragment<DemoPagingViewModel, BaseSmartPagingBinding, NotificationMessageBean> {

    @Override
    protected BasePagingAdapter<NotificationMessageBean, ?> getRecyclerAdapter() {
        PagingDemoAdapter pagingDemoAdapter = new PagingDemoAdapter();
        pagingDemoAdapter.setPagingHeaderAdapter(new DemoSmartPagingRatioHeaderAdapter());
        return pagingDemoAdapter;
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, observer);
        onRefresh(binding.smartFreshLayout);
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
