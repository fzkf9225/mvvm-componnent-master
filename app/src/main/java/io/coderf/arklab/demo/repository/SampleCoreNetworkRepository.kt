package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import kotlinx.coroutines.flow.Flow

/**
 * 新版 NetworkRepository 用法示例（真实 API）。
 *
 * ```
 * viewModelScope.launch {
 *   sample.requestNewsDetail(id).collect { result ->
 *     result.onSuccess { data -> ... }
 *           .onError { /* 一般已由 RequestUi 展示；鉴权失败会自动刷 token 后重试 */ }
 *   }
 * }
 * ```
 */
class SampleCoreNetworkRepository(
    private val api: ApiServiceHelper,
    tokenRefresher: TokenRefresher? = null
) : BaseNetworkRepository<io.coderf.arklab.common.base.BaseView>(tokenRefresher = tokenRefresher) {

    fun requestNewsDetail(
        id: String,
        options: RequestOptions = RequestOptions.defaults()
    ): Flow<RequestResult<NotificationMessageBean>> {
        return request(options) {
            api.getNewInfoByIdSuspend(id)
        }
    }

    fun requestNewsPage(
        page: Int,
        pageSize: Int,
        options: RequestOptions = RequestOptions.builder().showLoading(false).build()
    ): Flow<RequestResult<List<NotificationMessageBean>>> {
        return request(options) {
            api.getNewListSuspend(
                page,
                pageSize,
                NotificationMessageBean().apply { type = "5" }
            ).list ?: emptyList()
        }
    }
}
