package io.coderf.arklab.core.network

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.inter.ApiRetrofitService
import io.coderf.arklab.core.bean.PagingQuery
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
 * 业务筛选条件通过 [Q] 从 ViewModel 经 PagingSource 快照传入 [fetchPage]，
 * **禁止**在 [fetchPage] 内通过 [getBaseView] 强转 Fragment/Activity 取参。
 *
 * [boundApiService] 用于按当前 ApiService 实例解析 TokenRefresher（见 [DefaultNetworkRepository]）。
 *
 * @param T  列表元素类型
 * @param BV BaseView
 * @param Q  分页查询参数，继承 [PagingQuery]
 */
abstract class NetworkPagingRepository<T : Any, BV : BaseView, Q : PagingQuery>(
    requestUi: RequestUi = NoOpRequestUi,
    tokenRefresher: TokenRefresher? = null,
    boundApiService: ApiRetrofitService? = null
) : BaseNetworkRepository<BV>(requestUi, tokenRefresher, boundApiService) {

    protected open val pagingRequestOptions: RequestOptions =
        RequestOptions.builder().showLoading(false).build()

    /**
     * 请求一页数据；实现内应调用 suspend API，框架负责 Loading/错误/鉴权刷新。
     *
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param query    来自 ViewModel 的查询参数快照（创建 PagingSource 时固定）
     */
    protected abstract suspend fun fetchPage(page: Int, pageSize: Int, query: Q): List<T>

    /**
     * 供 [NetworkPagingSource] 调用：成功发射列表；失败已交付 UI，并转为异常供 Paging 展示。
     */
    open fun loadPage(page: Int, pageSize: Int, query: Q): Flow<List<T>> {
        return request(pagingRequestOptions) { fetchPage(page, pageSize, query) }
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
