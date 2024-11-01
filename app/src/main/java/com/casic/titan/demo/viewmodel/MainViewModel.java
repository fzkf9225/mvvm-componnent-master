package com.casic.titan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
public class MainViewModel extends BaseViewModel<RepositoryImpl, BaseView> implements DefaultLifecycleObserver {


    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl repository() {
        return null;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }
}
