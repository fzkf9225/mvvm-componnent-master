package pers.fz.mvvm.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import pers.fz.mvvm.bean.base.PageBean;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2020/12/17 16:23
 * describe:
 */
public class BaseRecyclerViewModel<BV extends BaseView,T> extends BaseViewModel<RepositoryImpl,BV> {

    public BaseRecyclerViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl repository() {
        return null;
    }

    public final MutableLiveData<PageBean<T>> listLiveData = new MutableLiveData<>();

    public MutableLiveData<PageBean<T>> getListLiveData() {
        return listLiveData;
    }
}
