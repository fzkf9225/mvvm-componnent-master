package io.coderf.arklab.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.datasource.PagingSource;
import io.coderf.arklab.common.repository.IRepository;
import io.coderf.arklab.common.repository.PagingRepositoryImpl;
import io.coderf.arklab.core.bean.PagingQuery;

/**
 * Created by fz on 2023/12/1 14:17
 * describe : Rx 分页 ViewModel。查询参数由 [pagingQuery] 持有，经 PagingSource 快照传给 Repository。
 * 更新 query 默认不自动请求，需显式 [refreshData] 或 [updatePagingQuery] 的 refresh=true。
 *
 * @param IR Repository
 * @param T  列表元素
 * @param V  BaseView
 * @param Q  查询参数
 */
public abstract class PagingViewModel<IR extends IRepository<V>, T, V extends BaseView, Q extends PagingQuery>
        extends BasePagingViewModel<IR, V> {
    protected final static int DEFAULT_START_PAGE = 1;
    protected final static int DEFAULT_PAGE_SIZE = 20;
    protected final static int DEFAULT_PREFETCH_DISTANCE = 3;

    private final MediatorLiveData<PagingData<T>> items = new MediatorLiveData<>();

    private LiveData<PagingData<T>> sourceLiveData;

    private Q pagingQuery;

    public PagingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PagingData<T>> getItems() {
        return items;
    }

    /**
     * 当前查询参数；首次访问时通过 [createPagingQuery] 初始化。
     */
    @NonNull
    public Q getPagingQuery() {
        if (pagingQuery == null) {
            pagingQuery = createPagingQuery();
        }
        return pagingQuery;
    }

    /**
     * 仅赋值，不触发请求。
     */
    public void setPagingQuery(@NonNull Q query) {
        this.pagingQuery = query;
    }

    /**
     * 提供初始查询参数。
     */
    @NonNull
    protected abstract Q createPagingQuery();

    /**
     * 更新查询参数。
     *
     * @param query   新条件
     * @param refresh 是否立即 [refreshData]；默认请传 false，由调用方决定何时刷新
     */
    public void updatePagingQuery(@NonNull Q query, boolean refresh) {
        setPagingQuery(query);
        if (refresh) {
            refreshData();
        }
    }

    /**
     * 仅更新参数，不请求。
     */
    public void updatePagingQuery(@NonNull Q query) {
        updatePagingQuery(query, false);
    }

    @SuppressWarnings("unchecked")
    public LiveData<PagingData<T>> createPagingData() {
        PagingRepositoryImpl<?, T, V, Q> repo = (PagingRepositoryImpl<?, T, V, Q>) iRepository;
        Q query = getPagingQuery();
        return PagingLiveData.getLiveData(new Pager<>(getPagingConfig(),
                () -> new PagingSource<>(repo, getStartPage(), query)));
    }

    @Override
    public void createRepository(V baseView) {
        super.createRepository(baseView);
        refreshData();
    }

    @Override
    public void refreshData() {
        if (sourceLiveData != null) {
            items.removeSource(sourceLiveData);
        }
        sourceLiveData = createPagingData();
        items.addSource(sourceLiveData, items::setValue);
    }

    public int getStartPage() {
        return DEFAULT_START_PAGE;
    }

    /**
     * PagingConfig 中 pageSize 与 initialLoadSize 建议一致，便于 offset 计算。
     */
    public PagingConfig getPagingConfig() {
        return new PagingConfig(DEFAULT_PAGE_SIZE, DEFAULT_PREFETCH_DISTANCE, false, DEFAULT_PAGE_SIZE);
    }
}
