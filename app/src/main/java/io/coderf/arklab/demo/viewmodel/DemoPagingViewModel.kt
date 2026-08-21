package io.coderf.arklab.demo.viewmodel

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.repository.KtDemoPagingRepositoryImpl
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]，替代旧 Rx PagingViewModel）。
 */
@HiltViewModel
class DemoPagingViewModel @Inject constructor(
    application: Application,
    private val apiServiceHelper: ApiServiceHelper
) : NetworkFlowPagingViewModel<KtDemoPagingRepositoryImpl, NotificationMessageBean, BaseView>(
    application
) {

    override fun createRepository(): KtDemoPagingRepositoryImpl {
        return KtDemoPagingRepositoryImpl(apiServiceHelper)
    }
}
