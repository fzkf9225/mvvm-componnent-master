package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.repository.DemoPagingRepositoryImpl;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.viewmodel.PagingViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class DemoPagingViewModel extends PagingViewModel<DemoPagingRepositoryImpl, ForestBean, BaseView> {
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public RetryService retryService;

    @Inject
    public DemoPagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected DemoPagingRepositoryImpl repository() {
        return new DemoPagingRepositoryImpl(apiServiceHelper, retryService, baseView);
    }

}
