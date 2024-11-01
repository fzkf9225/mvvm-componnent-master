package com.casic.titan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.view.HomeFragmentView;
import com.casic.titan.usercomponent.api.UserApiService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
public class HomeFragmentViewModel extends BaseViewModel<RepositoryImpl, BaseView> implements DefaultLifecycleObserver {

    public HomeFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl repository() {
        return null;
    }

}
