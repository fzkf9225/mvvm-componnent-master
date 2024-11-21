package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.navigation.Navigation;

import com.casic.titan.demo.R;
import com.casic.titan.demo.activity.PagingDetailActivity;
import com.casic.titan.demo.adapter.PagingHeaderDemoAdapter;
import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.RegionBean;
import com.casic.titan.demo.viewmodel.DemoPagingViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BasePagingFragment;
import pers.fz.mvvm.databinding.PagingRecyclerViewBinding;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
public class DemoPagingFragment extends BasePagingFragment<DemoPagingViewModel, PagingRecyclerViewBinding, RegionBean> {
    @Inject
    ApiServiceHelper apiServiceHelper;
    @Override
    protected BasePagingAdapter<RegionBean, ?> getRecyclerAdapter() {
        return new PagingHeaderDemoAdapter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, responseBean -> adapter.submitData(getLifecycle(), responseBean));
    }

    @Override
    public void onItemClick(View view, RegionBean item, int position) {
        super.onItemClick(view, item, position);
        showToast("点击的是第" + position + "行，内容是：" + item.getAreaCode());
        Bundle bundle = new Bundle();
        bundle.putString(PagingDetailActivity.ARGS, item.getAreaName());
        bundle.putInt(PagingDetailActivity.LINE, position);
        Navigation.findNavController(view).navigate(
                R.id.navigate_to_paging_detail,
                bundle);
    }

    @Override
    public void onItemLongClick(View view, RegionBean item, int position) {
        super.onItemLongClick(view, item, position);
        new ConfirmDialog(requireContext())
                .setSureText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnSureClickListener(dialog -> adapter.notifyItemRemoved(position))
                .builder()
                .show();
    }
}
