package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pers.fz.mvvm.base.BaseRepository;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;

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
