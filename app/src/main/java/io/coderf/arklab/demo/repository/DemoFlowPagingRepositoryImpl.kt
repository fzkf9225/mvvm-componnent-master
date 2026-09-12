package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import kotlinx.coroutines.flow.Flow

/**
 * 新闻分页仓库（新版 [NetworkPagingRepository]）。
 * 自动解析 data、错误 UI、Token 刷新均由框架 + TokenRefresher 完成。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/12
 */
class DemoFlowPagingRepositoryImpl(
    private val api: ApiServiceHelper
) : NetworkPagingRepository<NotificationMessageBean, RequestNotificationBean>() {

    override suspend fun fetchPage(
        page: Int,
        pageSize: Int,
        query: RequestNotificationBean
    ): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(
            page,
            pageSize,
            query
        )
        return pageBean.list ?: emptyList()
    }

    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return requestPage(
            RequestOptions.defaults()
        ) {
            api.getNewInfoByIdSuspend(id)
        }
    }
}
