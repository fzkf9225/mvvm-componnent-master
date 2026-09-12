package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;

/**
 * DownloadViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/8/14 10:56
 */
public class DownloadViewModel extends BaseViewModel<BaseRepository> {

    public DownloadViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
