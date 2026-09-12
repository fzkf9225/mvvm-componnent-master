package io.coderf.arklab.common.repository;

import java.util.List;

import io.coderf.arklab.common.api.BaseApiService;
import io.coderf.arklab.common.api.ErrorConsumer;
import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.bean.ApiRequestOptions;
import io.coderf.arklab.core.request.RequestUi;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.core.bean.PagingQuery;
import io.reactivex.rxjava3.core.Observable;

/**
 * Rx 分页仓库。查询参数由 ViewModel 经 PagingSource 快照传入，禁止在 requestPaging 内强转页面取参。
 *
 * @param API ApiService
 * @param T   列表元素
 * @param Q   分页查询参数
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/12/1 11:14
 * @updated 2026/9/12
 */
public abstract class PagingRepositoryImpl<API extends BaseApiService, T, Q extends PagingQuery>
        extends RepositoryImpl<API> {
    private ApiRequestOptions apiRequestOptions;

    public PagingRepositoryImpl(API apiService) {
        super(apiService);
    }

    public PagingRepositoryImpl(RetryService retryService, API apiService) {
        super(retryService, apiService);
    }

    public PagingRepositoryImpl() {
    }

    public PagingRepositoryImpl(RetryService retryService) {
        super(retryService);
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
        RequestUi ui = getRequestUi();
        if (ui != null) {
            ui.showError(new io.coderf.arklab.core.request.AppError.Business(
                    BaseException.ErrorType.OTHER.getCode(),
                    exception.getMessage() != null ? exception.getMessage() : "",
                    exception));
        }
    }

}
