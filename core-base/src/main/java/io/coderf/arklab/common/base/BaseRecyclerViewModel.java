package io.coderf.arklab.common.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.common.bean.base.PageBean;

/**
 * Recycler 列表 ViewModel 基类（无 Repository 泛型）。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @created 2020/12/17 16:23
 * @updated 2026/11/13
 */
public abstract class BaseRecyclerViewModel<T> extends BaseViewModel {

    public BaseRecyclerViewModel(@NonNull Application application) {
        super(application);
    }

    public final MutableLiveData<PageBean<T>> listLiveData = new MutableLiveData<>();

    public MutableLiveData<PageBean<T>> getListLiveData() {
        return listLiveData;
    }
}
