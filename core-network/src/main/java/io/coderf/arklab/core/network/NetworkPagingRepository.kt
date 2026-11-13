package io.coderf.arklab.core.network

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.core.bean.PagingQuery
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.request.AppErrorThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 新版分页仓库基类：用 [request] 拉一页数据，供 [NetworkPagingSource] 使用。
 *
 * 分页默认不弹 loading（与历史分页仓库行为一致）；详情等非 Paging 请求请经 ViewModel `launchRequest`。
 *
 * 业务筛选条件通过 [Q] 从 ViewModel 经 PagingSource 快照传入 [fetchPage]，
 * **禁止**在 [fetchPage] 内强转 Fragment/Activity 取参。
 *
 * @param T  列表元素类型
 * @param Q  分页查询参数，继承 [PagingQuery]
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/11/13
 */
abstract class NetworkPagingRepository<T : Any, Q : PagingQuery>(
    tokenRefresher: TokenRefresher? = null,
    boundApiService: BaseApiService? = null
) : BaseNetworkRepository(tokenRefresher, boundApiService) {

    protected open val pagingRequestOptions: RequestOptions =
        RequestOptions.builder().showLoading(false).deliverErrorToUi(false).build()

    /**
     * 请求一页数据；实现内应调用 suspend API；本层只做重试 / 鉴权，不含 UI。
     */
    protected abstract suspend fun fetchPage(page: Int, pageSize: Int, query: Q): List<T>

    /**
     * 供 [NetworkPagingSource] 调用：成功发射列表；失败转为异常供 Paging 展示。
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
     * 带完整 [RequestResult] 的请求（详情页等非 Paging 场景）；UI 由 ViewModel launchRequest 处理。
     */
    fun <R> requestPage(
        options: RequestOptions = RequestOptions(showLoading = false, deliverErrorToUi = false),
        block: suspend () -> R
    ): Flow<RequestResult<R>> = request(options, block)
}
