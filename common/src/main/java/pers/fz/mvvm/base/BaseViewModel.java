package pers.fz.mvvm.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;

import pers.fz.mvvm.repository.IRepository;

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
