package com.casic.otitan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.RepositoryImpl;

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */
public class DownloadViewModel extends BaseViewModel<BaseRepository<BaseView>,BaseView> {

    public DownloadViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
