package com.casic.titan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.repository.DemoPagingRepositoryImpl;
import com.casic.titan.demo.view.MainView;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.IRepository;
import pers.fz.mvvm.repository.PagingRepositoryImpl;
import pers.fz.mvvm.repository.RepositoryImpl;
import pers.fz.mvvm.viewmodel.PagingViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class DemoPagingViewModel extends PagingViewModel<DemoPagingRepositoryImpl,ForestBean, BaseView>{
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public RetryService retryService;

    @Inject
    public DemoPagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected DemoPagingRepositoryImpl createRepository() {
        return new DemoPagingRepositoryImpl(apiServiceHelper,retryService,baseView);
    }

}
