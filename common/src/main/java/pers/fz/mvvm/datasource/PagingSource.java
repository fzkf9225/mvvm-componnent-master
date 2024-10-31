package pers.fz.mvvm.datasource;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.repository.PagingRepositoryImpl;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/8/7 9:17
 * describe :
 */
public class PagingSource<T,BV extends BaseView> extends RxPagingSource<Integer, T> {
    private final String TAG = PagingSource.class.getSimpleName();
    private int startPage = 0;
    private final PagingRepositoryImpl<T,BV> pagingRepository;

    public PagingSource(PagingRepositoryImpl<T,BV> pagingRepository, int startPage) {
        this.pagingRepository = pagingRepository;
        this.startPage = startPage;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, T>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        try {
            Integer nextPageNumber = loadParams.getKey();
            if (nextPageNumber == null) {
                nextPageNumber = startPage;
            }
            Integer finalNextPageNumber = nextPageNumber;
            return Single.fromObservable(
                            pagingRepository.requestPaging(nextPageNumber, loadParams.getLoadSize())
                                    .map(mBeans -> toLoadResult(mBeans, finalNextPageNumber))
                    .doOnError(pagingRepository.catchException())
                    .onErrorReturn(LoadResult.Error::new));
        } catch (Exception e) {
            LogUtil.show(TAG, "异常：" + e);
            e.printStackTrace();
            pagingRepository.onError(e);
            return Single.just(new LoadResult.Error<>(e));
        }
    }
    /**
     * 功能描述 将获取的集合对象转化为需加载的结果对象
     *
     * @param mBeans 待加载的实体
     * @param page  对应的页数
     * @return: androidx.paging.PagingSource.LoadResult<java.lang.Integer, com.xxx.xxx.Bean>
     * @since 1.0
     */
    private LoadResult<Integer, T> toLoadResult(@NonNull List<T> mBeans, Integer page) {
        Integer prevKey = page == 1 ? null : page - 1;
        Integer nextKey = mBeans.isEmpty() ? null : page + 1;
        return new LoadResult.Page<>(mBeans, prevKey, nextKey, LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED);
    }
    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, T> state) {
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
