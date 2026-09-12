package io.coderf.arklab.common.repository

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.inter.FlowRetryService
import io.coderf.arklab.core.bean.PagingQuery
import kotlinx.coroutines.flow.Flow

/**
 * 旧 Flow 分页仓库。查询参数由 ViewModel 经 FlowPagingSource 快照传入，
 * 禁止在 requestPaging 内强转页面取参。
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
abstract class PagingFlowRepositoryImpl<API : BaseApiService, T : Any, Q : PagingQuery> :
    FlowRepositoryImpl<API> {
    val apiRequestOptions: ApiRequestOptions by lazy {
        ApiRequestOptions.Builder().setShowDialog(false).build()
    }

    constructor(apiService: API) : super(apiService)

    constructor(retryService: FlowRetryService, apiService: API) : super(retryService, apiService)

    constructor()

    constructor(retryService: FlowRetryService) : super(retryService)

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
