package io.coderf.arklab.demo.viewmodel

import android.app.Application
import io.coderf.arklab.common.api.RepositoryFactory
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.viewmodel.FlowPagingViewModel
import io.coderf.arklab.demo.api.ApiServiceHelper
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.repository.DemoFlowPagingRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by fz on 2023/4/27 14:58
 * describe :
 */
@HiltViewModel
class DemoFlowPagingViewModel @Inject constructor(application: Application) :
    FlowPagingViewModel<DemoFlowPagingRepositoryImpl, NotificationMessageBean, BaseView>(application) {

    @Inject
    lateinit var apiServiceHelper: ApiServiceHelper

    override fun createRepository(): DemoFlowPagingRepositoryImpl {
        return RepositoryFactory.createFlow(
            DemoFlowPagingRepositoryImpl::class.java,
            baseView,
            apiServiceHelper
        )
    }

    suspend fun getInfoById(id:String): Flow<NotificationMessageBean>? {
        return iRepository.getInfoById(id)
    }

}
