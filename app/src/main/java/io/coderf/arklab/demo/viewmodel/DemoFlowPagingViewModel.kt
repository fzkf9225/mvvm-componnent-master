package io.coderf.arklab.demo.viewmodel

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.repository.DemoFlowPagingRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]）。
 */
@HiltViewModel
class DemoFlowPagingViewModel @Inject constructor(
    application: Application,
    private val apiServiceHelper: ApiServiceHelper
) : NetworkFlowPagingViewModel<DemoFlowPagingRepositoryImpl, NotificationMessageBean, BaseView>(
    application
) {

    override fun createRepository(): DemoFlowPagingRepositoryImpl {
        return DemoFlowPagingRepositoryImpl(apiServiceHelper)
    }

    fun getInfoById(id: String): Flow<RequestResult<NotificationMessageBean>> {
        return iRepository.getInfoById(id)
    }
}
