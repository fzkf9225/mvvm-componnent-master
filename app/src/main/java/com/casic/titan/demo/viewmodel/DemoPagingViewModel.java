package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.NotificationMessageBean;
import com.casic.titan.demo.repository.KtDemoPagingRepositoryImpl;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.api.RepositoryFactory;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.viewmodel.PagingViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class DemoPagingViewModel extends PagingViewModel<KtDemoPagingRepositoryImpl, NotificationMessageBean, BaseView> {
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public RetryService retryService;

    @Inject
    public DemoPagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected KtDemoPagingRepositoryImpl createRepository() {
        return RepositoryFactory.create(KtDemoPagingRepositoryImpl.class,retryService,baseView,apiServiceHelper);
    }

}
