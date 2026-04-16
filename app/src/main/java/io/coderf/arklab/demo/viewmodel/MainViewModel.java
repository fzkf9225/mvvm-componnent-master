package io.coderf.arklab.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.demo.api.ApiServiceHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;

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
