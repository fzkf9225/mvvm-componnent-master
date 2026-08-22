package io.coderf.arklab.common.repository

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.inter.FlowRetryService
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.flow.Flow

/**
 * Created by fz on 2023/12/1 11:14
 * describe : 旧 Flow 分页仓库。查询参数由 ViewModel 经 FlowPagingSource 快照传入，
 * 禁止在 requestPaging 内强转 BaseView 取参。
 *
 * @param API ApiService
 * @param T   列表元素
 * @param BV  BaseView
 * @param Q   分页查询参数
 */
abstract class PagingFlowRepositoryImpl<API : BaseApiService, T : Any, BV : BaseView, Q : PagingQuery> :
    FlowRepositoryImpl<API, BV> {
    val apiRequestOptions: ApiRequestOptions by lazy {
        ApiRequestOptions.Builder().setShowDialog(false).build()
    }

    constructor(retryService: FlowRetryService, baseView: BV) : super(retryService, baseView)

    constructor(apiService: API) : super(apiService)

    constructor(baseView: BV, apiService: API) : super(baseView, apiService)

    constructor(retryService: FlowRetryService, apiService: API) : super(retryService, apiService)

    constructor(retryService: FlowRetryService, baseView: BV, apiService: API) : super(
        retryService,
        baseView,
        apiService
    )

    constructor()

    constructor(retryService: FlowRetryService) : super(retryService)

    constructor(baseView: BV) : super(baseView)

    /**
     * 请求一页数据。
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param query       来自 ViewModel 的查询参数快照
     */
    abstract suspend fun requestPaging(currentPage: Int, pageSize: Int, query: Q): Flow<List<T>>?

    fun onError(exception: Throwable) {
        getRequestUi()?.onErrorCode(
            BaseResponse<Any?>(
                BaseException.ErrorType.OTHER.code,
                exception.message
            )
        )
    }

}
