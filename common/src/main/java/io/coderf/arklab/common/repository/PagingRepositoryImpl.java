package io.coderf.arklab.common.repository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.coderf.arklab.common.api.BaseApiService;
import io.coderf.arklab.common.api.ErrorConsumer;
import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.common.inter.RetryService;

/**
 * Created by fz on 2023/12/1 11:14
 * describe :
 */
public abstract class PagingRepositoryImpl<API extends BaseApiService,T, BV extends BaseView> extends RepositoryImpl<API,BV> {
    private ApiRequestOptions apiRequestOptions;

    public PagingRepositoryImpl(RetryService retryService, BV baseView) {
        super(retryService, baseView);
    }

    public PagingRepositoryImpl(API apiService) {
        super(apiService);
    }

    public PagingRepositoryImpl(BV baseView, API apiService) {
        super(baseView, apiService);
    }

    public PagingRepositoryImpl(RetryService retryService, API apiService) {
        super(retryService, apiService);
    }

    public PagingRepositoryImpl(RetryService retryService, BV baseView, API apiService) {
        super(retryService, baseView, apiService);
    }

    public PagingRepositoryImpl() {
    }

    public PagingRepositoryImpl(RetryService retryService) {
        super(retryService);
    }

    public PagingRepositoryImpl(BV baseView) {
        super(baseView);
    }

    public abstract Observable<List<T>> requestPaging(int currentPage, int pageSize);

    public ApiRequestOptions getApiRequestOptions() {
        if (apiRequestOptions == null) {
            apiRequestOptions = new ApiRequestOptions.Builder().setShowDialog(false).build();
        }
        return apiRequestOptions;
    }

    public ErrorConsumer catchException() {
        return new ErrorConsumer(baseView, getApiRequestOptions());
    }

    public void onError(Exception exception) {
        if (baseView != null) {
            baseView.onErrorCode(new BaseResponse(BaseException.ErrorType.OTHER.getCode(), exception.getMessage()));
        }
    }

}
