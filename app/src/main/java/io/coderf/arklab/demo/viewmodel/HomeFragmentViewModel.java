package io.coderf.arklab.demo.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;

/**
 * HomeFragmentViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/4/27 14:58
 */
public class HomeFragmentViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> implements DefaultLifecycleObserver {

    public HomeFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

}
