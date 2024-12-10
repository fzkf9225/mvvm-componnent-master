package pers.fz.mvvm.datasource;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.google.android.gms.common.api.Api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.database.BaseRoomDao;
import pers.fz.mvvm.repository.RoomRepositoryImpl;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2024/11/1 17:36
 * describe:
 */
public class RxRoomPagingSource<T, DB extends BaseRoomDao<T>, BV extends BaseView> extends RxPagingSource<Integer, T> {

    private final RoomRepositoryImpl<T, DB, BV> roomRepositoryImpl;
    private final Map<String, Object> queryParams;

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

    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull PagingSource.LoadParams<Integer> loadParams) {
        try {
            int limit = loadParams.getLoadSize();
            int offset = loadParams.getKey() == null ? 0 : (limit * loadParams.getKey());
            return Flowable.create((FlowableOnSubscribe<List<T>>) emitter -> {
                        emitter.onNext(roomRepositoryImpl.findPageList(queryParams, keywordsKey, keywords, TextUtils.isEmpty(orderBy) ? "id" : orderBy, limit, offset));
                        emitter.onComplete();
                    }, BackpressureStrategy.LATEST)
                    .subscribeOn(Schedulers.io())
                    .map(mBeans -> toLoadResult(mBeans, loadParams.getKey() == null ? 0 : loadParams.getKey()))
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
    public Integer getRefreshKey(@NonNull PagingState<Integer, T> pagingState) {
        return null;
    }
}
