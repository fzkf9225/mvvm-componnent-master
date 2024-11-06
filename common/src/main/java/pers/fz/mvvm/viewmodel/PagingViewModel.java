package pers.fz.mvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import kotlinx.coroutines.CoroutineScope;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.datasource.PagingSource;
import pers.fz.mvvm.repository.IRepository;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 14:17
 * describe :
 */
public abstract class PagingViewModel<IR extends IRepository, T, V extends BaseView> extends BasePagingViewModel<IR, V> {
    private PagingConfig pagingConfig;
    protected final static int DEFAULT_START_PAGE = 1;
    protected final static int DEFAULT_PAGE_SIZE = 20;
    protected final static int DEFAULT_INIT_LOAD_SIZE = DEFAULT_PAGE_SIZE * 2;
    protected final static int DEFAULT_PREFETCH_DISTANCE = 3;

    private int startPage = DEFAULT_START_PAGE;
    /**
     * 当请求发生错误时是否用EmptyLayout占用显示错误页
     */
    private boolean errorPlaceholder = true;

    private LiveData<PagingData<T>> items;


    public PagingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PagingData<T>> getItems() {
        return items;
    }

    public LiveData<PagingData<T>> createPagingData() {
        return PagingLiveData.cachedIn(PagingLiveData.getLiveData(
                        new Pager<>(getPagingConfig(), () -> new PagingSource<T, V>((PagingRepositoryImpl<T, V>) iRepository, getStartPage()))),
                getCoroutineScope()
        );
    }

    @Override
    public void createRepository(V baseView) {
        super.createRepository(baseView);
        items = createPagingData();
    }

    public void refreshData() {
        items = createPagingData();
    }

    public CoroutineScope getCoroutineScope() {
        return ViewModelKt.getViewModelScope(this);
    }

    public void setErrorPlaceholder(boolean errorPlaceholder) {
        this.errorPlaceholder = errorPlaceholder;
    }

    public boolean isErrorPlaceholder() {
        return errorPlaceholder;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setPagingConfig(PagingConfig pagingConfig) {
        this.pagingConfig = pagingConfig;
    }

    public PagingConfig getPagingConfig() {
        if (pagingConfig == null) {
            pagingConfig = new PagingConfig(DEFAULT_PAGE_SIZE, DEFAULT_PREFETCH_DISTANCE, true, DEFAULT_INIT_LOAD_SIZE);
        }
        return pagingConfig;
    }
}
