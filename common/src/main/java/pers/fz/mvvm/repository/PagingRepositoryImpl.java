package pers.fz.mvvm.repository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.base.BaseModelEntity;
import pers.fz.mvvm.api.ErrorConsumer;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.inter.RetryService;

/**
 * Created by fz on 2023/12/1 11:14
 * describe :
 */
public abstract class PagingRepositoryImpl<T, BV extends BaseView> extends RepositoryImpl {
    private RequestConfigEntity requestConfigEntity;

    public PagingRepositoryImpl(RetryService retryService, BV baseView) {
        super(retryService, baseView);
    }

    public PagingRepositoryImpl(BV baseView) {
        super(baseView);
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
