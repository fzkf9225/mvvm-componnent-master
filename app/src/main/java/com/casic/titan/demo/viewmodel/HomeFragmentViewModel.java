package com.casic.titan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.view.HomeFragmentView;
import com.casic.titan.usercomponent.api.UserApiService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseViewModel;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class HomeFragmentViewModel extends BaseViewModel<HomeFragmentView> implements DefaultLifecycleObserver {
    private ApiServiceHelper apiServiceHelper;
    private UserApiService userApiService;
    @Inject
    public HomeFragmentViewModel(@NonNull Application application) {
        super(application);
        apiServiceHelper = ApiRetrofit.getInstance().getApiService(ApiServiceHelper.class);
        userApiService = ApiRetrofit.getInstance().getApiService(UserApiService.class);
    }

}
