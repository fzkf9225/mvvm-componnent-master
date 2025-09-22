package com.casic.otitan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.demo.api.ApiServiceHelper;
import com.casic.otitan.demo.bean.NotificationMessageBean;
import com.casic.otitan.demo.repository.KtDemoPagingRepositoryImpl;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.casic.otitan.common.api.RepositoryFactory;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.viewmodel.PagingViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class DemoPagingViewModel extends PagingViewModel<KtDemoPagingRepositoryImpl, NotificationMessageBean, BaseView> {
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public DemoPagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected KtDemoPagingRepositoryImpl createRepository() {
        return RepositoryFactory.create(KtDemoPagingRepositoryImpl.class,baseView,apiServiceHelper);
    }

}
