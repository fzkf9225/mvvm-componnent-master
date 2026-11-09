package io.coderf.arklab.demo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.core.bean.RequestResult;
import io.coderf.arklab.demo.activity.VerifyActivity;
import io.coderf.arklab.demo.adapter.PagingRoomAdapter;
import io.coderf.arklab.demo.bean.Person;
import io.coderf.arklab.demo.viewmodel.DemoRoomPagingViewModel;

/**
 * RoomSmartPagingFragment：点击编辑回显，长按删除，保存后刷新。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/6 10:23
 * @updated 2026/11/9
 */
public class RoomSmartPagingFragment extends BaseSmartPagingFragment<DemoRoomPagingViewModel, BaseSmartPagingBinding, Person> {

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onRefresh(binding.smartFreshLayout);
                }
            });

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
        LogUtil.logger(ApiRetrofit.TAG, "点击编辑：" + position + "," + item.getName());
        Intent intent = new Intent(requireContext(), VerifyActivity.class);
        intent.putExtra(VerifyActivity.EXTRA_PERSON_ID, item.getId());
        editLauncher.launch(intent);
    }

    public void searcher(String keywords) {
        mViewModel.setKeywords(keywords);
        // 与下拉刷新同一入口：refreshData() 换新 Pager + adapter.refresh()
        onRefresh(binding.smartFreshLayout);
    }

    /** 新增/编辑成功后刷新，复用基类 onRefresh 链路 */
    public void refreshList() {
        onRefresh(binding.smartFreshLayout);
    }

    @Override
    public void onItemLongClick(View view, Person item, int position) {
        super.onItemLongClick(view, item, position);
        new ConfirmDialog(requireContext())
                .setPositiveText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnPositiveClickListener(dialog -> {
                    mViewModel.deletePerson(item, result -> {
                        if (result instanceof RequestResult.Success) {
                            LogUtil.logger(ApiRetrofit.TAG, "删除成功");
                            onRefresh(binding.smartFreshLayout);
                        } else if (result instanceof RequestResult.Error) {
                            LogUtil.logger(ApiRetrofit.TAG, "删除失败：" + ((RequestResult.Error) result).getError());
                        }
                        return kotlin.Unit.INSTANCE;
                    });
                })
                .builder()
                .show();
    }
}
