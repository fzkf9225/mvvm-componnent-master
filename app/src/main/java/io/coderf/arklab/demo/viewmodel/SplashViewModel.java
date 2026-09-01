package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;

/**
 * SplashViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/8/14 10:56
 */
public class SplashViewModel extends BaseViewModel<BaseRepository<BaseView>,BaseView> {

    public SplashViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
