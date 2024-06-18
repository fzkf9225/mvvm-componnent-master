package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.navigation.Navigation;

import com.casic.titan.demo.R;
import com.casic.titan.demo.activity.PagingDetailActivity;
import com.casic.titan.demo.adapter.PagingDemoAdapter;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.repository.DemoPagingRepositoryImpl;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.databinding.PagingRecyclerViewBinding;
import pers.fz.mvvm.listener.OnHeaderViewClickListener;
import pers.fz.mvvm.repository.PagingRepositoryImpl;
import pers.fz.mvvm.viewmodel.PagingViewModel;
import pers.fz.mvvm.base.BasePagingFragment;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
public class DemoPagingFragment extends BasePagingFragment<PagingViewModel, PagingRecyclerViewBinding, ForestBean> implements OnHeaderViewClickListener {

    @Override
    protected BasePagingAdapter<ForestBean, ?> getRecyclerAdapter() {
        return new PagingDemoAdapter();
    }

    @Override
    public PagingRepositoryImpl createRepository() {
        return new DemoPagingRepositoryImpl(mViewModel.retryService,this);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        adapter.setOnHeaderViewClickListener(this);
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        requestData();
    }

    @Override
    public void onItemClick(View view, ForestBean item, int position) {
        super.onItemClick(view, item, position);
        showToast("点击的是第" + position + "行，内容是：" + item.getCertificate());
        Bundle bundle = new Bundle();
        bundle.putString(PagingDetailActivity.ARGS, item.getCertificate());
        bundle.putInt(PagingDetailActivity.LINE, position);
        Navigation.findNavController(view).navigate(
                R.id.navigate_to_paging_detail,
                bundle);
    }

    @Override
    public void onItemLongClick(View view, ForestBean item, int position) {
        super.onItemLongClick(view, item, position);
        //不能这么删除，这样删除会有bug
        new ConfirmDialog(requireContext())
                .setSureText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnSureClickListener(dialog -> adapter.notifyItemRemoved(position + 1))
                .builder()
                .show();
    }

    @Override
    protected void requestData() {
        super.requestData();
        mViewModel.requestPagingData(ForestBean.class).observe(this, observer);
    }

    @Override
    public void onHeaderViewClick(View view) {
        showToast("头布局点击事件！");
    }

    @Override
    public void onHeaderViewLongClick(View view) {
        showToast("头布局长按事件！");
    }
}
