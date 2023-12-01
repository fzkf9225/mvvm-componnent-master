package com.casic.titan.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.demo.view.MainView;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
public class MainViewModel extends BaseViewModel<RepositoryImpl,MainView> implements DefaultLifecycleObserver {

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
