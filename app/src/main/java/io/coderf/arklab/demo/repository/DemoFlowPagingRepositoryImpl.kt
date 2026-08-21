package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.network.NetworkPagingRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import kotlinx.coroutines.flow.Flow

/**
 * 新闻分页仓库（新版 [NetworkPagingRepository]）。
 * 自动解析 data、错误 UI、Token 刷新均由框架 + [TokenRefresher] 完成。
 */
class DemoFlowPagingRepositoryImpl(
    private val api: ApiServiceHelper
) : NetworkPagingRepository<NotificationMessageBean, io.coderf.arklab.common.base.BaseView>() {

    override suspend fun fetchPage(page: Int, pageSize: Int): List<NotificationMessageBean> {
        val pageBean = api.getNewListSuspend(
            page,
            pageSize,
            NotificationMessageBean().apply { type = "5" }
        )
        return pageBean?.list ?: emptyList()
    }

    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return requestPage(
            RequestOptions.builder().showLoading(true).build()
        ) {
            api.getNewInfoByIdSuspend(id)
        }
    }
}
