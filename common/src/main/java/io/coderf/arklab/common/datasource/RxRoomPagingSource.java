package io.coderf.arklab.common.datasource;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.dao.BaseRoomDao;
import io.coderf.arklab.common.repository.RoomRepositoryImpl;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 基于本地 Room 数据库的 Paging3 分页源（RxJava3）。
 *
 * <p><b>分页模型：</b>使用页码 {@link Integer} 作为 key，{@code offset = page × pageSize}，
 * 数据来自 {@link RoomRepositoryImpl#findPageList} 同步查询。</p>
 *
 * <p><b>用法示例：</b></p>
 * <pre>{@code
 * RxRoomPagingSource<Person, PersonDao, BaseView> source = new RxRoomPagingSource<>(
 *         personRepository,
 *         queryParams,           // 等值条件，可为 emptyMap
 *         Set.of("name", "mobile"), // 模糊列，可为 null
 *         keyword,
 *         "id"                   // 排序列，空则默认 "id"
 * );
 * Pager<Integer, Person> pager = new Pager<>(
 *         new PagingConfig(20),
 *         () -> source
 * );
 * }</pre>
 *
 * @param <T>  列表项实体
 * @param <DB> 继承 {@link BaseRoomDao} 的 Dao
 * @param <BV> 页面 View 类型
 * @author fz
 * @see RoomRepositoryImpl#findPageList
 */
public class RxRoomPagingSource<T, DB extends BaseRoomDao<T>, BV extends BaseView> extends RxPagingSource<Integer, T> {

    /** 提供同步分页查询的仓库 */
    private final RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl;
    /** 等值查询条件：列名 -> 值 */
    private final Map<String, Object> queryParams;
    /** 参与 LIKE 模糊搜索的列名集合 */
    private final Set<String> keywordsKey;
    /** 模糊搜索关键字 */
    private final String keywords;
    /** 排序列名，为空时 load 内使用 "id" */
    private String orderBy;

    /**
     * 全参数构造：条件 + 关键字 + 排序。
     *
     * @param roomRepositoryImpl 已注入 RequestUi 的 Room 仓库
     * @param queryParams        等值条件 Map
     * @param keywordsKey        模糊列集合
     * @param keywords           搜索词
     * @param orderBy            排序字段
     */
    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Map<String, Object> queryParams,
                              Set<String> keywordsKey,
                              String keywords,
                              String orderBy) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = queryParams;
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
        this.orderBy = orderBy;
    }

    /**
     * 无等值条件，仅关键字 + 排序。
     */
    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Set<String> keywordsKey,
                              String keywords,
                              String orderBy) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = new HashMap<>();
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
        this.orderBy = orderBy;
    }

    /**
     * 仅排序，无条件与关键字。
     */
    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl, String orderBy) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = new HashMap<>();
        this.keywords = null;
        this.keywordsKey = null;
        this.orderBy = orderBy;
    }

    /**
     * 条件 + 关键字，无排序（load 时默认按 id）。
     */
    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Map<String, Object> queryParams,
                              Set<String> keywordsKey,
                              String keywords) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = queryParams;
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
    }

    /**
     * 加载一页数据。
     * <ul>
     *   <li>key 为 null 表示第一页（page = 0）</li>
     *   <li>本页条数 &lt; loadSize 时认为没有下一页</li>
     * </ul>
     */
    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        try {
            final int limit = loadParams.getLoadSize();
            final int page = loadParams.getKey() == null ? 0 : loadParams.getKey();
            final int offset = page * limit;
            final String sortColumn = TextUtils.isEmpty(orderBy) ? "id" : orderBy;
            return Single.fromCallable(() ->
                            roomRepositoryImpl.findPageList(
                                    queryParams, keywordsKey, keywords, sortColumn, limit, offset))
                    .subscribeOn(Schedulers.io())
                    .map(list -> toLoadResult(list, page, limit))
                    .onErrorReturn(LoadResult.Error::new);
        } catch (Exception e) {
            return Single.just(new LoadResult.Error<>(e));
        }
    }

    /**
     * 将查询结果转为 Paging {@link LoadResult.Page}。
     *
     * @param items 当前页数据
     * @param page  当前页码（从 0 开始）
     * @param limit 请求的 pageSize
     */
    private LoadResult<Integer, T> toLoadResult(@NonNull List<T> items, int page, int limit) {
        Integer prevKey = page <= 0 ? null : page - 1;
        Integer nextKey = items.size() < limit ? null : page + 1;
        return new LoadResult.Page<>(items, prevKey, nextKey,
                LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED);
    }

    /**
     * 刷新时根据锚点位置估算应加载的页码。
     */
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
