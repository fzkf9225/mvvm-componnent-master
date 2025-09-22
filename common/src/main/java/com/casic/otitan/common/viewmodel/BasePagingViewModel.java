package com.casic.otitan.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.IRepository;

/**
 * created by fz on 2024/11/6 11:36
 * describe:
 */
public abstract class BasePagingViewModel<IR extends IRepository<BV>,BV extends BaseView> extends BaseViewModel<IR, BV> {

    public BasePagingViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void refreshData();

}

