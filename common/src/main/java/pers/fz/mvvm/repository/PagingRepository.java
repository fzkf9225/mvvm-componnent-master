package pers.fz.mvvm.repository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.base.BaseModelEntity;
import pers.fz.mvvm.base.ErrorConsumer;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/12/1 11:14
 * describe :
 */
public abstract class PagingRepository<T> extends RepositoryImpl {
    private RequestConfigEntity requestConfigEntity;

    public PagingRepository(RetryService retryService) {
        super(retryService);
    }

    public abstract Observable<List<T>> requestPaging(int currentPage, int pageSize);

    public RequestConfigEntity getRequestConfigEntity() {
        if (requestConfigEntity == null) {
            requestConfigEntity = new RequestConfigEntity.Builder().setShowDialog(false).build();
        }
        return requestConfigEntity;
    }

    public ErrorConsumer catchException() {
        return new ErrorConsumer(baseView, getRequestConfigEntity());
    }

    public void onError(Exception exception) {
        if (baseView != null) {
            baseView.onErrorCode(new BaseModelEntity(BaseException.OTHER, exception.getMessage()));
        }
    }

}
