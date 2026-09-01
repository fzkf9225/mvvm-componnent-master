package io.coderf.arklab.demo.viewmodel

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import io.coderf.arklab.demo.repository.DemoFlowPagingRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@HiltViewModel
class DemoFlowPagingViewModel @Inject constructor(
    application: Application,
    private val apiServiceHelper: ApiServiceHelper
) : NetworkFlowPagingViewModel<DemoFlowPagingRepositoryImpl, NotificationMessageBean, BaseView, RequestNotificationBean>(
    application
) {

    override fun createRepository(): DemoFlowPagingRepositoryImpl {
        return DemoFlowPagingRepositoryImpl(apiServiceHelper)
    }

    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return iRepository.getInfoById(id)
    }

    override fun createPagingQuery() = RequestNotificationBean().apply {
        type = "5"
    }
}
