package io.coderf.arklab.common.datasource;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.coderf.arklab.common.api.ApiRetrofit;
import io.coderf.arklab.common.api.BaseApiService;
import io.coderf.arklab.common.repository.PagingRepositoryImpl;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.core.bean.PagingQuery;
import io.reactivex.rxjava3.core.Single;

/**
 * Rx PagingSource；[query] 为创建时快照，一轮分页内不变。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/8/7 9:17
 * @updated 2026/9/12
 */
public class PagingSource<T, Q extends PagingQuery> extends RxPagingSource<Integer, T> {
    private Integer startPage = 1;
    private final PagingRepositoryImpl<?, T, Q> pagingRepository;
    private final Q query;

    public <API extends BaseApiService> PagingSource(
            PagingRepositoryImpl<API, T, Q> pagingRepository,
            Q query
    ) {
        this.pagingRepository = pagingRepository;
        this.query = query;
    }

    public <API extends BaseApiService> PagingSource(
            PagingRepositoryImpl<API, T, Q> pagingRepository,
            Integer startPage,
            Q query
    ) {
        this.pagingRepository = pagingRepository;
        this.startPage = startPage;
        this.query = query;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        try {
            int currentPage = loadParams.getKey() == null ? startPage : loadParams.getKey();
            int loadSize = loadParams.getLoadSize();
            return Single.fromObservable(
                    pagingRepository.requestPaging(currentPage, loadSize, query)
                            .map(mBeans -> toLoadResult(mBeans, currentPage, loadSize))
                            .doOnError(pagingRepository.catchException())
                            .onErrorReturn(LoadResult.Error::new));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logger(ApiRetrofit.TAG, "PagingSource请求错误：" + e);
            pagingRepository.onError(e);
            return Single.just(new LoadResult.Error<>(e));
        }
    }

    /**
     * 功能描述 将获取的集合对象转化为需加载的结果对象
     *
     * @param mBeans            待加载的实体
     * @param page              对应的页数
     * @param requestedLoadSize 本次请求的条数上限，用于判断末页（少于该条数则无下一页）
     */
    private LoadResult<Integer, T> toLoadResult(@NonNull List<T> mBeans, Integer page, int requestedLoadSize) {
        Integer prevKey = page == 1 ? null : page - 1;
        boolean endReached = mBeans.isEmpty() || mBeans.size() < requestedLoadSize;
        Integer nextKey = endReached ? null : page + 1;
        return new LoadResult.Page<>(mBeans, prevKey, nextKey, LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED);
    }

    /**
     * 调用 adapter.refresh() 时触发；默认从起始页刷新。
     */
    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, T> state) {
        return startPage;
    }
}
