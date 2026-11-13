package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import io.coderf.arklab.common.base.BaseViewModel;

/**
 * 分页 ViewModel 基类（无 Repository 泛型）。
 * 仓库由子类构造注入或属性持有；首次拉数请在子类 init / 构造完成后调用 {@link #refreshData()}。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2024/11/6 11:36
 * @updated 2026/11/13
 */
public abstract class BasePagingViewModel extends BaseViewModel {

    public BasePagingViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void refreshData();

}
