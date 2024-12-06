package pers.fz.mvvm.datasource;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.database.BaseRoomDao;
import pers.fz.mvvm.repository.RoomRepositoryImpl;

/**
 * created by fz on 2024/11/1 17:36
 * describe:
 */
public class RxRoomPagingSource<T, DB extends BaseRoomDao<T>, BV extends BaseView> extends RxPagingSource<Integer, T> {

    private final RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl;
    private final Map<String, Object> queryParams;
    private int startPage = 0;

    private final Set<String> keywordsKey;
    private final String keywords;
    private String orderBy;

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

    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Map<String, Object> queryParams,
                              Set<String> keywordsKey,
                              String keywords) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = queryParams;
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
    }

    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Map<String, Object> queryParams,
                              Set<String> keywordsKey,
                              String keywords,
                              int startPage) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = queryParams;
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
        this.startPage = startPage;
    }

    public RxRoomPagingSource(RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl,
                              Map<String, Object> queryParams,
                              Set<String> keywordsKey,
                              String keywords,
                              String orderBy,
                              int startPage) {
        this.roomRepositoryImpl = roomRepositoryImpl;
        this.queryParams = queryParams;
        this.keywords = keywords;
        this.keywordsKey = keywordsKey;
        this.startPage = startPage;
        this.orderBy = orderBy;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull PagingSource.LoadParams<Integer> loadParams) {
        try {
            Integer nextPageNumber = loadParams.getKey();
            if (nextPageNumber == null) {
                nextPageNumber = startPage;
            }
            int limit = loadParams.getLoadSize();
            int offset = nextPageNumber * limit;
            Integer finalNextPageNumber = nextPageNumber;
            return Flowable.create((FlowableOnSubscribe<List<T>>) emitter -> {
                        emitter.onNext(roomRepositoryImpl.findPageList(queryParams, keywordsKey, keywords, TextUtils.isEmpty(orderBy) ? "id" : orderBy, limit, offset));
                        emitter.onComplete();
                    }, BackpressureStrategy.LATEST)
                    .subscribeOn(Schedulers.io())
                    .map(mBeans -> toLoadResult(mBeans, finalNextPageNumber))
                    .onErrorReturn(LoadResult.Error::new)
                    .singleOrError();
        } catch (Exception e) {
            e.printStackTrace();
            return Single.just(new LoadResult.Error<>(e));
        }
    }

    /**
     * 功能描述 将获取的集合对象转化为需加载的结果对象
     *
     * @param mBeans 待加载的实体
     * @param page   对应的页数
     * @return: androidx.paging.PagingSource.LoadResult<java.lang.Integer, T>
     * @since 1.0
     */
    private LoadResult<Integer, T> toLoadResult(@NonNull List<T> mBeans, Integer page) {
        Integer prevKey = page == 0 ? null : page - 1;
        Integer nextKey = mBeans.isEmpty() ? null : page + 1;
        return new LoadResult.Page<>(mBeans, prevKey, nextKey, LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, T> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, T> anchorPage = state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }
        return null;
    }
}
