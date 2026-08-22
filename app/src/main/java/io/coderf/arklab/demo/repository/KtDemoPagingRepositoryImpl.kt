package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.bean.EmptyPagingQuery
import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean

/**
 * Kotlin 分页 Demo（已迁到新版 [NetworkPagingRepository]，替代旧 Rx [PagingRepositoryImpl]）。
 */
class KtDemoPagingRepositoryImpl(
    private val api: ApiServiceHelper
) : NetworkPagingRepository<NotificationMessageBean, io.coderf.arklab.common.base.BaseView,EmptyPagingQuery>() {

    override suspend fun fetchPage(
        page: Int,
        pageSize: Int,
        query: EmptyPagingQuery
    ): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(
            page,
            pageSize,
            NotificationMessageBean().apply { type = "5" }
        )
        return pageBean.list ?: emptyList()
    }
}
