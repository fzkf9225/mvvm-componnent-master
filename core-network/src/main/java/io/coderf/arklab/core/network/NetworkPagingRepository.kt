package io.coderf.arklab.core.network

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.inter.ApiRetrofitService
import io.coderf.arklab.core.request.AppErrorThrowable
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.TokenRefresher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 新版分页仓库基类：用 [request] 拉一页数据，供 [NetworkPagingSource] 使用。
 *
 * 对齐旧 [io.coderf.arklab.common.repository.PagingFlowRepositoryImpl]：分页默认不弹 loading。
 *
 * [boundApiService] 用于按当前 ApiService 实例解析 TokenRefresher（见 [DefaultNetworkRepository]）。
 */
abstract class NetworkPagingRepository<T : Any, BV : BaseView>(
    requestUi: RequestUi = NoOpRequestUi,
    tokenRefresher: TokenRefresher? = null,
    boundApiService: ApiRetrofitService? = null
) : BaseNetworkRepository<BV>(requestUi, tokenRefresher, boundApiService) {

    protected open val pagingRequestOptions: RequestOptions =
        RequestOptions.builder().showLoading(false).build()

    /**
     * 请求一页数据；实现内应调用 suspend API，框架负责 Loading/错误/鉴权刷新。
     */
    protected abstract suspend fun fetchPage(page: Int, pageSize: Int): List<T>

    /**
     * 供 [NetworkPagingSource] 调用：成功发射列表；失败已交付 UI，并转为异常供 Paging 展示。
     */
    open fun loadPage(page: Int, pageSize: Int): Flow<List<T>> {
        return request(pagingRequestOptions) { fetchPage(page, pageSize) }
            .map { result ->
                when (result) {
                    is RequestResult.Success -> result.data
                    is RequestResult.Error -> throw AppErrorThrowable(result.error)
                }
            }
    }

    /**
     * 带完整 [RequestResult] 的请求（详情页等非 Paging 场景可用）。
     */
    fun <R> requestPage(
        options: RequestOptions = RequestOptions.defaults(),
        block: suspend () -> R
    ): Flow<RequestResult<R>> = request(options, block)
}
