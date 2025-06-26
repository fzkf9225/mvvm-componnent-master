package com.casic.titan.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.adapter.PagingRoomAdapter;
import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.viewmodel.DemoRoomPagingViewModel;
import com.casic.titan.usercomponent.view.UserView;
import com.google.gson.Gson;

import java.util.HashMap;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BasePagingAdapter;
import pers.fz.mvvm.base.BaseSmartPagingFragment;
import pers.fz.mvvm.databinding.BaseSmartPagingBinding;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;

/**
 * created by fz on 2024/11/6 10:23
 * describe:
 */
public class RoomSmartPagingFragment extends BaseSmartPagingFragment<DemoRoomPagingViewModel, BaseSmartPagingBinding, Person> implements UserView {

    @Override
    protected BasePagingAdapter<Person, ?> getRecyclerAdapter() {
        return new PagingRoomAdapter();
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, responseBean -> adapter.submitData(getLifecycle(), responseBean));
    }

    @Override
    public void onItemClick(View view, Person item, int position) {
        super.onItemClick(view, item, position);

        LogUtil.show(ApiRetrofit.TAG, "点击："+position+","+item.getName());
//        Disposable disposable = mViewModel.getRepository().findInfoById(item.getId() ,true)
//                .subscribe((data) -> {
//                    LogUtil.show(ApiRetrofit.TAG, "查询成功：" + new Gson().toJson(data));
//                    showToast("查询成功！");
//                }, throwable -> {
//                    LogUtil.show(ApiRetrofit.TAG, "查询失败：" + throwable);
//                    showToast("查询失败，" + throwable.getMessage());
//                });
    }

    public void searcher(String keywords) {
        mViewModel.setKeywords(keywords);
        mViewModel.keywordsKey.add("name");
        mViewModel.refreshData();
        adapter.refresh();
    }

    @Override
    public void onItemLongClick(View view, Person item, int position) {
        super.onItemLongClick(view, item, position);
        //不能这么删除，这样删除会有bug
        new ConfirmDialog(requireContext())
                .setPositiveText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnPositiveClickListener(dialog -> {
                    @SuppressLint("NotifyDataSetChanged") Disposable disposable = mViewModel.getIRepository().delete(item,true)
                            .subscribe(() -> {
                                LogUtil.show(ApiRetrofit.TAG, "删除成功" );
                                showToast("删除成功！");
                                mViewModel.refreshData();
                                adapter.refresh();
                            }, throwable -> {
                                LogUtil.show(ApiRetrofit.TAG, "删除失败：" + throwable);
                            });
                })
                .builder()
                .show();
    }

    @Override
    public void toLast() {

    }

    @Override
    public boolean hasTarget() {
        return false;
    }

    @Override
    public void toTarget() {

    }

    @Override
    public void toMain() {

    }

    @Override
    public void hideKeyboard() {

    }
}

