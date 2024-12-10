package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.adapter.RecyclerViewSampleAdapter;
import com.casic.titan.demo.viewmodel.RecyclerViewSampleViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseRecyclerViewFragment;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;
import pers.fz.mvvm.databinding.SmartrecyclerviewBinding;
import pers.fz.mvvm.listener.OnHeaderViewClickListener;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

@AndroidEntryPoint
public class OnViewSampleFragment extends BaseRecyclerViewFragment<RecyclerViewSampleViewModel, SmartrecyclerviewBinding, PopupWindowBean> implements
        OnHeaderViewClickListener {
    private RecyclerViewSampleAdapter recyclerViewSampleAdapter;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        recyclerViewSampleAdapter.setOnHeaderViewClickListener(this);
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        requestData();
    }

    @Override
    protected void requestData() {
        super.requestData();
        mViewModel.loadData(mCurrentPage);
    }

    @Override
    protected BaseRecyclerViewAdapter<PopupWindowBean, OptionTextViewBinding> getRecyclerAdapter() {
        recyclerViewSampleAdapter = new RecyclerViewSampleAdapter();
        recyclerViewSampleAdapter.setOnItemClickListener(this);
        recyclerViewSampleAdapter.setOnItemLongClickListener(this);
        return recyclerViewSampleAdapter;
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        showToast("点击内容是：" + recyclerViewSampleAdapter.getList().get(position).getPopupName());
    }

    @Override
    public void onItemLongClick(View view, int position) {
        super.onItemLongClick(view, position);
        new ConfirmDialog(requireContext())
                .setPositiveText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnPositiveClickListener(dialog -> {
                    recyclerViewSampleAdapter.getList().remove(position);
                    recyclerViewSampleAdapter.notifyItemRemoved(position + 1);
                })
                .builder()
                .show();
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