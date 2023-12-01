package com.casic.titan.demo.fragment;

import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.paging.PagingData;

import com.casic.titan.demo.adapter.PagingDemoAdapter;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.repository.DemoPagingRepository;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.adapter.BasePagingAdapter;
import pers.fz.mvvm.databinding.PagingRecyclerViewBinding;
import pers.fz.mvvm.repository.PagingRepository;
import pers.fz.mvvm.viewmodel.PagingViewModel;
import pers.fz.mvvm.base.BasePagingFragment;

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
public class DemoPagingFragment extends BasePagingFragment<PagingViewModel, PagingRecyclerViewBinding, ForestBean> {

    @Override
    protected BasePagingAdapter<ForestBean, ?> getRecyclerAdapter() {
        return new PagingDemoAdapter();
    }

    @Override
    public PagingRepository createRepository() {
        return new DemoPagingRepository(mViewModel.retryService);
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        requestData();
    }

    @Override
    protected void requestData() {
        super.requestData();
        mViewModel.requestPagingData(ForestBean.class).observe(this, observer);
    }
}
