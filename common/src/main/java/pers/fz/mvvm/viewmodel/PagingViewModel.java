package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.CoroutineScope;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.datasource.PagingSource;
import pers.fz.mvvm.inter.PagingView;
import pers.fz.mvvm.repository.PagingRepository;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/12/1 14:17
 * describe :
 */
@HiltViewModel
public class PagingViewModel extends BaseViewModel<PagingRepository, PagingView> {
    private PagingConfig pagingConfig;
    private int startPage = 0;
    @Inject
    public PagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected PagingRepository createRepository() {
        return baseView.createRepository();
    }

    public <T> LiveData<PagingData<T>> requestPagingData() {
        return requestPagingData(null, null);
    }
    public <T> LiveData<PagingData<T>> requestPagingData(Class<T> clx) {
        return requestPagingData(clx, null);
    }

    public <T> LiveData<PagingData<T>> requestPagingData(Class<T> clx,PagingConfig pagingConfig) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        return PagingLiveData.cachedIn(PagingLiveData.getLiveData(new Pager<>(pagingConfig == null ? getPagingConfig() : pagingConfig,
                () -> new PagingSource<T>(iRepository, startPage))), viewModelScope);
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public void setPagingConfig(PagingConfig pagingConfig) {
        this.pagingConfig = pagingConfig;
    }

    public PagingConfig getPagingConfig() {
        if (pagingConfig == null) {
            pagingConfig = new PagingConfig(20, 3, true);
        }
        return pagingConfig;
    }
}