package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;

/**
 * EmptyViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/5/30 17:14
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
