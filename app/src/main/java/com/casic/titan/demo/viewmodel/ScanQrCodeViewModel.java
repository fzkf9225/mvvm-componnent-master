package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.titan.demo.view.ScanQrCodeView;

import pers.fz.mvvm.base.BaseViewModel;


/**
 * Created by fz on 2023/11/9 9:26
 * describe :
 */
public class ScanQrCodeViewModel extends BaseViewModel<ScanQrCodeView> {
    public ScanQrCodeViewModel(@NonNull Application application) {
        super(application);
    }
}
