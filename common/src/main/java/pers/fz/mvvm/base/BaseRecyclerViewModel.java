package pers.fz.mvvm.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import pers.fz.mvvm.bean.base.PageBean;

/**
 * Created by fz on 2020/12/17 16:23
 * describe:
 */
public class BaseRecyclerViewModel<BV extends BaseView,T> extends BaseViewModel<BV> {

    public BaseRecyclerViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<PageBean<T>> listLiveData = new MutableLiveData<>();

    public MutableLiveData<PageBean<T>> getListLiveData() {
        return listLiveData;
    }
}
