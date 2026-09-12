package io.coderf.arklab.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.demo.adapter.PagingRoomAdapter;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.viewmodel.DemoRoomPagingViewModel;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * RoomSmartPagingFragment 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/6 10:23
 * @updated 2026/9/12
 */
public class RoomSmartPagingFragment extends BaseSmartPagingFragment<DemoRoomPagingViewModel, BaseSmartPagingBinding, Person> {

    @Override
    protected BasePagingAdapter<Person, ?> getRecyclerAdapter() {
        return new PagingRoomAdapter();
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, responseBean -> adapter.submitData(getLifecycle(), responseBean));
        onRefresh(binding.smartFreshLayout);
    }

    @Override
    public void onItemClick(View view, Person item, int position) {
        super.onItemClick(view, item, position);
        LogUtil.logger(ApiRetrofit.TAG, "点击：" + position + "," + item.getName());
    }

    public void searcher(String keywords) {
        mViewModel.setKeywords(keywords);
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onItemLongClick(View view, Person item, int position) {
        super.onItemLongClick(view, item, position);
        new ConfirmDialog(requireContext())
                .setPositiveText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnPositiveClickListener(dialog -> {
                    @SuppressLint("NotifyDataSetChanged") Disposable disposable = mViewModel.getIRepository().delete(item, true)
                            .subscribe(() -> {
                                LogUtil.logger(ApiRetrofit.TAG, "删除成功");
                                showToast("删除成功！");
                                mViewModel.refreshData();
                                adapter.refresh();
                            }, throwable -> {
                                LogUtil.logger(ApiRetrofit.TAG, "删除失败：" + throwable);
                            });
                })
                .builder()
                .show();
    }
}
