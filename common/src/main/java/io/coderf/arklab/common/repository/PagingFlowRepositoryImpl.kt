package io.coderf.arklab.common.repository

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.bean.ApiRequestOptions
import io.coderf.arklab.common.inter.FlowRetryService
import kotlinx.coroutines.flow.Flow

/**
 * Created by fz on 2023/12/1 11:14
 * describe :
 */
abstract class PagingFlowRepositoryImpl<API : BaseApiService, T : Any, BV : BaseView> :
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

    // 或者使用挂起函数版本（更推荐）
    abstract suspend fun requestPaging(currentPage: Int, pageSize: Int): Flow<List<T>>?

    fun onError(exception: Throwable) {
        baseView?.onErrorCode(
            BaseResponse<Any?>(
                BaseException.ErrorType.OTHER.code,
                exception.message
            )
        )
    }

}
