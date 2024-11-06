package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.IRepository;

/**
 * created by fz on 2024/11/6 11:36
 * describe:
 */
public abstract class BasePagingViewModel<IR extends IRepository,BV extends BaseView> extends BaseViewModel<IR, BV> {

    public BasePagingViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void refreshData();

}

