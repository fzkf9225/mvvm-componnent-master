package com.casic.otitan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.demo.view.ScanQrCodeView;

import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.RepositoryImpl;


/**
 * Created by fz on 2023/11/9 9:26
 * describe :
 */
public class ScanQrCodeViewModel extends BaseViewModel<BaseRepository<ScanQrCodeView>,ScanQrCodeView> {

    public ScanQrCodeViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
