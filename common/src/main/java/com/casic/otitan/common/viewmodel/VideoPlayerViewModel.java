package com.casic.otitan.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;

/**
 * Created by fz on 2021/5/26 14:16
 * describe:
 */
public class VideoPlayerViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {
    public VideoPlayerViewModel(@NonNull Application application) {
        super(application);
    }
    @Override
    protected BaseRepository<BaseView> createRepository() {
        return null;
    }

}
