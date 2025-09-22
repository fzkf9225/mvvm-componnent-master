package com.casic.otitan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.casic.otitan.demo.api.ApiServiceHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.RepositoryImpl;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class MainViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> implements DefaultLifecycleObserver {
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }
}
