package com.casic.titan.demo.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.casic.titan.demo.adapter.RecyclerViewSampleAdapter;
import com.casic.titan.demo.viewmodel.RecyclerViewSampleViewModel;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseRecyclerViewFragment;
import pers.fz.mvvm.bean.PopupWindowBean;
import pers.fz.mvvm.databinding.OptionTextViewBinding;
import pers.fz.mvvm.databinding.SmartrecyclerviewBinding;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

public class RecyclerViewSampleFragment extends BaseRecyclerViewFragment<RecyclerViewSampleViewModel, SmartrecyclerviewBinding,PopupWindowBean> {
    private RecyclerViewSampleAdapter recyclerViewSampleAdapter;
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
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
        recyclerViewSampleAdapter = new RecyclerViewSampleAdapter(requireContext());
        recyclerViewSampleAdapter.setOnItemClickListener(this);
        recyclerViewSampleAdapter.setOnItemLongClickListener(this);
        return recyclerViewSampleAdapter;
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        showToast("点击内容是："+recyclerViewSampleAdapter.getList().get(position).getName());
    }

    @Override
    public void onItemLongClick(View view, int position) {
        super.onItemLongClick(view, position);
        new ConfirmDialog(requireContext())
                .setSureText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnSureClickListener(dialog -> {
                    recyclerViewSampleAdapter.getList().remove(position);
                    recyclerViewSampleAdapter.notifyItemRemoved(position);
                })
                .builder()
                .show();
    }
}