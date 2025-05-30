package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2021/5/26 14:16
 * describe:
 */
public class VideoPlayerViewModel extends BaseViewModel<RepositoryImpl, BaseView> {
    public VideoPlayerViewModel(@NonNull Application application) {
        super(application);
    }
    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

}
