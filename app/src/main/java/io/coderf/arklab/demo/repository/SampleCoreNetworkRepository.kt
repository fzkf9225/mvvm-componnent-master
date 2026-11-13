package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.core.network.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import kotlinx.coroutines.flow.Flow

/**
 * 示例 NetworkRepository：只返回 Flow，不含 UI。
 *
 * ```
 * launchRequest(RequestOptions.cancellable()) {
 *   sample.requestNewsDetail(id).collectSuccess { data -> ... }
 * }
 * ```
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/11/13
 */
class SampleCoreNetworkRepository(
    private val api: ApiServiceHelper,
    tokenRefresher: TokenRefresher? = null
) : BaseNetworkRepository(
    tokenRefresher = tokenRefresher,
    boundApiService = api
) {

    /**
     * Loading / 错误 UI 由 ViewModel `launchRequest` 处理；此处 options 仅 retry/timeout/auth 生效。
     */
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
        options: RequestOptions = RequestOptions.defaults()
    ): Flow<RequestResult<List<NotificationMessageBean>>> {
        return request(options) {
            api.getNewListSuspend(
                page,
                pageSize,
                RequestNotificationBean().apply { type = "5" }
            ).list ?: emptyList()
        }
    }
}
