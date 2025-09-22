package com.casic.otitan.common.base;

import android.app.Application;

import androidx.annotation.NonNull;

import com.casic.otitan.common.repository.IRepository;

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:baseViewMode封装
 */
public abstract class BaseViewModel<IR extends IRepository<BV>, BV extends BaseView> extends BaseViewViewModel<BV> {

    protected IR iRepository;

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (iRepository != null) {
            iRepository.clear();
        }
    }

    protected abstract IR createRepository();

    public void createRepository(BV baseView) {
        this.baseView = baseView;
        iRepository = createRepository();
        if (iRepository != null && iRepository.getBaseView() == null) {
            iRepository.setBaseView(baseView);
        }
    }

    public IR getIRepository() {
        return iRepository;
    }

}
