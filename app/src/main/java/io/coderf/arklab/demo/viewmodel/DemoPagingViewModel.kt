package io.coderf.arklab.demo.viewmodel

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.core.network.NetworkFlowPagingViewModel
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import io.coderf.arklab.demo.repository.KtDemoPagingRepositoryImpl
import javax.inject.Inject

/**
 * 新闻分页 ViewModel（新版 [NetworkFlowPagingViewModel]，替代旧 Rx PagingViewModel）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/13
 */
@HiltViewModel
class DemoPagingViewModel @Inject constructor(
    application: Application,
    pagingRepository: KtDemoPagingRepositoryImpl
) : NetworkFlowPagingViewModel<NotificationMessageBean, RequestNotificationBean>(
    application,
    pagingRepository
) {

    override fun createPagingQuery() = RequestNotificationBean().apply {
        type = "5"
    }
}
