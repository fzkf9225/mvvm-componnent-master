package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class WightViewModel extends BaseViewModel<BaseView> {
    public WightViewModel(@NonNull Application application) {
        super(application);
    }
}
