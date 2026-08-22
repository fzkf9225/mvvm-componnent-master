package io.coderf.arklab.common.datasource;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.dao.BaseRoomDao;
import io.coderf.arklab.common.repository.RoomRepositoryImpl;
import io.coderf.arklab.core.bean.RoomPagingQuery;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 基于本地 Room 数据库的 Paging3 分页源（RxJava3）。
 *
 * <p><b>分页模型：</b>使用页码 {@link Integer} 作为 key，{@code offset = page × pageSize}，
 * 数据来自 {@link RoomRepositoryImpl#findPageList} 同步查询。</p>
 *
 * <p>查询条件由 {@link RoomPagingQuery} 在创建 Source 时快照传入（与网络分页
 * {@code PagingQuery} 同一套约定）。ViewModel 持有 query，变更后 {@code refreshData()}。</p>
 *
 * <p><b>用法示例：</b></p>
 * <pre>{@code
 * // ViewModel
 * @Override
 * protected RoomPagingQuery createPagingQuery() {
 *     RoomPagingQuery q = new RoomPagingQuery();
 *     q.getKeywordsKey().add("name");
 *     q.setOrderBy("id");
 *     return q;
 * }
 *
 * @Override
 * public LiveData<PagingData<Person>> createPagingData() {
 *     return PagingLiveData.getLiveData(new Pager<>(getPagingConfig(),
 *         () -> new RxRoomPagingSource<>(iRepository, getPagingQuery())));
 * }
 * }</pre>
 *
 * @param <T>  列表项实体
 * @param <DB> 继承 {@link BaseRoomDao} 的 Dao
 * @param <BV> 页面 View 类型
 * @author fz
 * @see RoomRepositoryImpl#findPageList
 * @see RoomPagingQuery
 */
public class RxRoomPagingSource<T, DB extends BaseRoomDao<T>, BV extends BaseView>
        extends RxPagingSource<Integer, T> {

    private final RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl;
    /** 创建本 Source 时的查询参数快照 */
    private final RoomPagingQuery query;

    /**
     * @param roomRepositoryImpl 已注入 RequestUi 的 Room 仓库
     * @param query              ViewModel 当前 {@link RoomPagingQuery}（建议传入副本或不可变快照）
     */
    public RxRoomPagingSource(
            @NonNull RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
            @NonNull RoomPagingQuery query
    ) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.query = query;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        try {
            final int limit = loadParams.getLoadSize();
            final int page = loadParams.getKey() == null ? 0 : loadParams.getKey();
            final int offset = page * limit;
            final String sortColumn = TextUtils.isEmpty(query.getOrderBy()) ? "id" : query.getOrderBy();
            final Map<String, Object> params = query.getQueryParams() != null
                    ? query.getQueryParams()
                    : Collections.emptyMap();
            final Set<String> keywordKeys = query.getKeywordsKey();
            final String keywords = query.getKeywords();
            return Single.fromCallable(() ->
                            roomRepositoryImpl.findPageList(
                                    params, keywordKeys, keywords, sortColumn, limit, offset))
                    .subscribeOn(Schedulers.io())
                    .map(list -> toLoadResult(list, page, limit))
                    .onErrorReturn(LoadResult.Error::new);
        } catch (Exception e) {
            return Single.just(new LoadResult.Error<>(e));
        }
    }

    private LoadResult<Integer, T> toLoadResult(@NonNull List<T> items, int page, int limit) {
        Integer prevKey = page <= 0 ? null : page - 1;
        Integer nextKey = items.size() < limit ? null : page + 1;
        return new LoadResult.Page<>(items, prevKey, nextKey,
                LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, T> pagingState) {
        Integer anchor = pagingState.getAnchorPosition();
        if (anchor == null) {
            return null;
        }
        LoadResult.Page<Integer, T> closest = pagingState.closestPageToPosition(anchor);
        if (closest == null) {
            return null;
        }
        int prev = closest.getPrevKey() != null ? closest.getPrevKey() : 0;
        int next = closest.getNextKey() != null ? closest.getNextKey() : 0;
        return (prev + next) / 2;
    }
}
