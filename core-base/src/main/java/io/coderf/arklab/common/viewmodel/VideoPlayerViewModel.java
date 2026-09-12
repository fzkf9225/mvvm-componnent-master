package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseViewModel;

/**
 * VideoPlayerViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2021/5/26 14:16
 */
public class VideoPlayerViewModel extends BaseViewModel<BaseRepository> {
    public VideoPlayerViewModel(@NonNull Application application) {
        super(application);
    }
    @Override
    protected BaseRepository createRepository() {
        return null;
    }

}
