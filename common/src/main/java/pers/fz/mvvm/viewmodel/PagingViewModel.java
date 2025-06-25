package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.datasource.PagingSource;
import pers.fz.mvvm.repository.IRepository;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 14:17
 * describe :
 */
public abstract class PagingViewModel<IR extends IRepository<V>, T, V extends BaseView> extends BasePagingViewModel<IR, V> {
    protected final static int DEFAULT_START_PAGE = 1;
    protected final static int DEFAULT_PAGE_SIZE = 20;
    protected final static int DEFAULT_PREFETCH_DISTANCE = 3;

    private LiveData<PagingData<T>> items;

    public PagingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PagingData<T>> getItems() {
        return items;
    }

    public LiveData<PagingData<T>> createPagingData() {
        return PagingLiveData.getLiveData(new Pager<>(getPagingConfig(), () -> new PagingSource<T, V>((PagingRepositoryImpl<?,T, V>) iRepository, getStartPage())));
    }

    @Override
    public void createRepository(V baseView) {
        super.createRepository(baseView);
        items = createPagingData();
    }

    public void refreshData() {
        items = createPagingData();
    }

    public int getStartPage() {
        return DEFAULT_START_PAGE;
    }

    /**
     * PagingConfig中有两个参数，一个是你每页加载条数，另一个是初始的时候加载条数，Paging3默认会加载每页条数*3，但是最好是设置相同
     * 不然PagerSource中的分页offset不太好计算，因为可能会每页的loadSize不相同，如果这两个值设置为相同是最方便的，不然的话你需要计算上一页的offset
     */
    public PagingConfig getPagingConfig() {
        return new PagingConfig(DEFAULT_PAGE_SIZE, DEFAULT_PREFETCH_DISTANCE, true, DEFAULT_PAGE_SIZE);
    }
}
