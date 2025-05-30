package com.casic.titan.demo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.database.PersonDatabase;
import com.casic.titan.demo.repository.RoomPagingRepositoryImpl;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2024/11/6 10:57
 * describe:
 */
public class VerifyViewModel extends BaseViewModel<RoomPagingRepositoryImpl, BaseView> {

    public MutableLiveData<Boolean> liveData = new MutableLiveData<>();

    public VerifyViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RoomPagingRepositoryImpl createRepository() {
        return new RoomPagingRepositoryImpl(PersonDatabase.getInstance(getApplication()).getPersonDao(), baseView);
    }

    public void add(Person person) {
        Disposable disposable = iRepository.insert(person,true).subscribe(
                () -> {
                    liveData.postValue(true);
                }, throwable -> {
                    LogUtil.show(ApiRetrofit.TAG,"错误："+throwable);
                    baseView.showToast(throwable.getMessage());
                    liveData.postValue(false);
                });
    }
}
