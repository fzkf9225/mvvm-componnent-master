package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;

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
