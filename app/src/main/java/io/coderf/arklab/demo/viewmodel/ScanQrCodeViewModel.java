package io.coderf.arklab.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.RepositoryImpl;
import io.coderf.arklab.demo.view.ScanQrCodeView;


/**
 * ScanQrCodeViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/11/9 9:26
 */
public class ScanQrCodeViewModel extends BaseViewModel<BaseRepository<ScanQrCodeView>,ScanQrCodeView> {

    public ScanQrCodeViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }
}
