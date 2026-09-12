package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.repository.IRepository;

/**
 * BasePagingViewModel 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/6 11:36
 * @updated 2026/9/12
 */
public abstract class BasePagingViewModel<IR extends IRepository> extends BaseViewModel<IR> {

    public BasePagingViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void refreshData();

}
