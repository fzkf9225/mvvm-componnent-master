package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2024/5/30 17:14
 * describe :
 */
public class EmptyViewModel extends BaseViewModel<RepositoryImpl, BaseView> {

    public EmptyViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
