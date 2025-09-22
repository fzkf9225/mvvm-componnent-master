package com.casic.otitan.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.casic.otitan.common.bean.base.PageBean;
import com.casic.otitan.common.repository.IRepository;

/**
 * Created by fz on 2020/12/17 16:23
 * describe:
 */
public abstract class BaseRecyclerViewModel<IR extends IRepository<BV>,BV extends BaseView,T> extends BaseViewModel<IR,BV> {

    public BaseRecyclerViewModel(@NonNull Application application) {
        super(application);
    }


    public final MutableLiveData<PageBean<T>> listLiveData = new MutableLiveData<>();

    public MutableLiveData<PageBean<T>> getListLiveData() {
        return listLiveData;
    }
}
