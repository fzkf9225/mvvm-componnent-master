package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import pers.fz.mvvm.base.BaseRepository;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class SplashViewModel extends BaseViewModel<BaseRepository<BaseView>,BaseView> {

    public SplashViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
