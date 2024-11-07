package com.casic.titan.demo.fragment;

import android.os.Bundle;
import android.view.View;

import com.casic.titan.demo.adapter.PagingRoomAdapter;
import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.database.PersonDatabase;
import com.casic.titan.demo.viewmodel.DemoRoomPagingViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
public class RoomSmartPagingFragment extends BaseSmartPagingFragment<DemoRoomPagingViewModel, BaseSmartPagingBinding, Person> {

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
        Disposable disposable = mViewModel.getRepository().findInfoById(item.getId() ,true)
                .subscribe((data) -> {
                    LogUtil.show(ApiRetrofit.TAG, "查询成功：" + new Gson().toJson(data));
                    showToast("查询成功！");
                }, throwable -> {
                    LogUtil.show(ApiRetrofit.TAG, "查询失败：" + throwable);
                    showToast("查询失败，" + throwable.getMessage());
                });
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
                .setSureText("确认删除")
                .setMessage("是否确认删除此行？")
                .setOnSureClickListener(dialog -> {
                    Disposable disposable = mViewModel.getRepository().deleteByParams(new HashMap<>() {{
                                put("id", item.getId());
                            }}, true)
                            .subscribe((integer) -> {
                                LogUtil.show(ApiRetrofit.TAG, "删除成功：" + integer);
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

}

