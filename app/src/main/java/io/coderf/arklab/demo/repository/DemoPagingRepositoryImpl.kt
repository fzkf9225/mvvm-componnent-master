package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean

/**
 * Java 侧遗留的 Demo 分页仓库，已迁到新版 [NetworkPagingRepository]。
 * （原 Rx [PagingRepositoryImpl] 实现已移除。）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
class DemoPagingRepositoryImpl(
    private val api: ApiServiceHelper,
    tokenRefresher: TokenRefresher? = null
) : NetworkPagingRepository<NotificationMessageBean, RequestNotificationBean>(
    tokenRefresher = tokenRefresher
) {

    override suspend fun fetchPage(page: Int, pageSize: Int, query: RequestNotificationBean): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(
            page,
            pageSize,
            query
        )
        return pageBean.list ?: emptyList()
    }
}
