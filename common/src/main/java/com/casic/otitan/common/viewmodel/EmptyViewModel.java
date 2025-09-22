package com.casic.otitan.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;

/**
 * Created by fz on 2024/5/30 17:14
 * describe :
 */
@HiltViewModel
public class EmptyViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {

    @Inject
    public EmptyViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected BaseRepository<BaseView> createRepository() {
        return null;
    }
}
