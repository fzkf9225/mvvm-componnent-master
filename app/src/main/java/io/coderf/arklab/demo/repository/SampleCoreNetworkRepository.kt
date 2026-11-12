package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.network.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import kotlinx.coroutines.flow.Flow

/**
 * 新版 NetworkRepository 用法示例（真实 API）。
 *
 * ```
 * // ViewModel：launchUiRequest + RequestOptions.cancellable()
 * launchUiRequest {
 *   sample.requestNewsDetail(id).collect { result ->
 *     result.onSuccess { data -> ... }
 *   }
 * }
 * ```
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/12
 */
class SampleCoreNetworkRepository(
    private val api: ApiServiceHelper,
    tokenRefresher: TokenRefresher? = null
) : BaseNetworkRepository(
    tokenRefresher = tokenRefresher,
    boundApiService = api
) {

    /**
     * 详情默认 [RequestOptions.cancellable]（可取消 Loading）；无 Loading 场景请显式传入 Options。
     */
    fun requestNewsDetail(
        id: String,
        options: RequestOptions = RequestOptions.cancellable()
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
                RequestNotificationBean().apply { type = "5" }
            ).list ?: emptyList()
        }
    }
}
