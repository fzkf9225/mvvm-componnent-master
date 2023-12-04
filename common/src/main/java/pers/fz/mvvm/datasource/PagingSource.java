package pers.fz.mvvm.datasource;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.repository.PagingRepository;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/8/7 9:17
 * describe :
 */
public class PagingSource<T> extends RxPagingSource<Integer, T> {
    private final String TAG = PagingSource.class.getSimpleName();
    private int startPage = 0;
    private final PagingRepository<T> pagingRepository;

    public PagingSource(PagingRepository<T> pagingRepository, int startPage) {
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
            Integer prevKey = nextPageNumber > startPage ? nextPageNumber - 1 : null;
            return Single.fromObservable(
                            pagingRepository.requestPaging(nextPageNumber, loadParams.getLoadSize())
                                    .map((Function<List<T>, LoadResult<Integer, T>>) ts -> {
                                        Integer nextKey = (ts == null || ts.isEmpty()) ? null : ((loadParams.getKey() == null ? 0 : loadParams.getKey()) + 1);
                                        return new LoadResult.Page<>(ts == null ? new ArrayList<>() : ts, prevKey, nextKey);
                                    }))
                    .doOnError(pagingRepository.catchException())
                    .onErrorReturn(LoadResult.Error::new);
        } catch (Exception e) {
            LogUtil.show(TAG, "异常：" + e);
            e.printStackTrace();
            pagingRepository.onError(e);
            return Single.just(new LoadResult.Error<>(e));
        }
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
