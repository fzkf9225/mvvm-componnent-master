package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.demo.api.ApiServiceHelper;
import io.coderf.arklab.demo.bean.NotificationMessageBean;
import io.coderf.arklab.demo.repository.KtDemoPagingRepositoryImpl;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.api.RepositoryFactory;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.viewmodel.PagingViewModel;

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
