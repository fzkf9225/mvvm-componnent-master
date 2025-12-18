package com.casic.otitan.demo.viewmodel

import android.app.Application
import com.casic.otitan.common.api.RepositoryFactory
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.viewmodel.FlowPagingViewModel
import com.casic.otitan.common.viewmodel.PagingViewModel
import com.casic.otitan.demo.api.ApiServiceHelper
import com.casic.otitan.demo.bean.NotificationMessageBean
import com.casic.otitan.demo.repository.DemoFlowPagingRepositoryImpl
import com.casic.otitan.demo.repository.KtDemoPagingRepositoryImpl
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
