package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 新闻分页仓库（新版 [NetworkPagingRepository]）。
 * 自动解析 data、错误 UI、Token 刷新均由框架 + TokenRefresher 完成。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/13
 */
class DemoFlowPagingRepositoryImpl @Inject constructor(
    private val api: ApiServiceHelper
) : NetworkPagingRepository<NotificationMessageBean, RequestNotificationBean>(
    boundApiService = api
) {

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

    /**
     * 按 id 查详情；静默 UI（Loading / 错误由 ViewModel launchRequest 处理）。
     */
    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return requestPage(
            RequestOptions.silent()
        ) {
            api.getNewInfoByIdSuspend(id)
        }
    }
}
