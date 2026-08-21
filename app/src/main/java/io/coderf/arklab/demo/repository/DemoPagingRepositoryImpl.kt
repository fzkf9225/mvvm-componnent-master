package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean

/**
 * Java 侧遗留的 Demo 分页仓库，已迁到新版 [NetworkPagingRepository]。
 * （原 Rx [PagingRepositoryImpl] 实现已移除。）
 */
class DemoPagingRepositoryImpl(
    private val api: ApiServiceHelper,
    tokenRefresher: TokenRefresher? = null
) : NetworkPagingRepository<NotificationMessageBean, io.coderf.arklab.common.base.BaseView>(
    tokenRefresher = tokenRefresher
) {

    override suspend fun fetchPage(page: Int, pageSize: Int): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(
            page,
            pageSize,
            NotificationMessageBean().apply { type = "1" }
        )
        return pageBean?.list ?: emptyList()
    }
}
