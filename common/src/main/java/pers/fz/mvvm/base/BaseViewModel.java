package pers.fz.mvvm.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;

import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.IRepository;

import javax.inject.Inject;


/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:baseViewMode封装
 */
public abstract class BaseViewModel<IR extends IRepository, V extends BaseView> extends AndroidViewModel {
    protected final String TAG = this.getClass().getSimpleName();

    protected V baseView;
    @Inject
    public RetryService retryService;
    protected IR iRepository;

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public void setBaseView(V baseView) {
        this.baseView = baseView;
        iRepository = createRepository();
    }

    public V getBaseView() {
        return baseView;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (iRepository != null) {
            iRepository.removeDisposable();
        }
    }

    public IR getRepository() {
        return iRepository;
    }

    protected abstract IR createRepository();

    public void startActivity(Context context, Class<?> toClx, Bundle bundle) {
        Intent intent = new Intent(context, toClx);
        ContextCompat.startActivity(context, intent, bundle);
    }

    public void startActivity(Context context, Class<?> toClx) {
        startActivity(context, toClx, null);
    }
}
