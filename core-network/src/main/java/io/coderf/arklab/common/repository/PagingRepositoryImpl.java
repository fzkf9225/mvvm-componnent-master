package io.coderf.arklab.common.repository;

import java.util.List;

import io.coderf.arklab.common.api.BaseApiService;
import io.coderf.arklab.common.api.ErrorConsumer;
import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.core.bean.PagingQuery;
import io.reactivex.rxjava3.core.Observable;

/**
 * Created by fz on 2023/12/1 11:14
 * describe : Rx 分页仓库。查询参数由 ViewModel 经 PagingSource 快照传入，禁止在 requestPaging 内强转 BaseView 取参。
 *
 * @param API ApiService
 * @param T   列表元素
 * @param BV  BaseView
 * @param Q   分页查询参数
 */
public abstract class PagingRepositoryImpl<API extends BaseApiService, T, BV extends BaseView, Q extends PagingQuery>
        extends RepositoryImpl<API, BV> {
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

    /**
     * 请求一页数据。
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param query       来自 ViewModel 的查询参数快照
     */
    public abstract Observable<List<T>> requestPaging(int currentPage, int pageSize, Q query);

    public ApiRequestOptions getApiRequestOptions() {
        if (apiRequestOptions == null) {
            apiRequestOptions = new ApiRequestOptions.Builder().setShowDialog(false).build();
        }
        return apiRequestOptions;
    }

    public ErrorConsumer catchException() {
        return new ErrorConsumer(getRequestUi(), getApiRequestOptions());
    }

    public void onError(Exception exception) {
        RequestUiCallback ui = getRequestUi();
        if (ui != null) {
            ui.onErrorCode(new BaseResponse(BaseException.ErrorType.OTHER.getCode(), exception.getMessage()));
        }
    }

}
