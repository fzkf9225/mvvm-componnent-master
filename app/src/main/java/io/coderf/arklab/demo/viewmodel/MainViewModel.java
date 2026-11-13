package io.coderf.arklab.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.demo.api.ApiServiceHelper;

/**
 * MainViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/4/27 14:58
 */
@HiltViewModel
public class MainViewModel extends BaseViewModel implements DefaultLifecycleObserver {
    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }
}
