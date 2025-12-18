package com.casic.otitan.demo.repository

import com.casic.otitan.demo.api.ApiServiceHelper
import com.casic.otitan.demo.bean.NotificationMessageBean
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.bean.ApiRequestOptions
import com.casic.otitan.common.inter.FlowRetryService
import com.casic.otitan.common.repository.PagingFlowRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by fz on 2023/12/1 15:25
 * describe :注意，如果通过工厂创建的，这里必须是baseView不可是对应的接口，使用的时候可以转换一下
 *             如果这里传递了retryService那么就以这个为准
 */
class DemoFlowPagingRepositoryImpl :
    PagingFlowRepositoryImpl<ApiServiceHelper, NotificationMessageBean, BaseView> {
    constructor(
        retryService: FlowRetryService,
        baseView: BaseView,
        apiService: ApiServiceHelper
    ) : super(
        retryService,
        baseView,
        apiService
    )

    constructor(baseView: BaseView, apiService: ApiServiceHelper) : super(
        baseView,
        apiService
    )

    override suspend fun requestPaging(
        currentPage: Int,
        pageSize: Int
    ): Flow<List<NotificationMessageBean>>? {
        return sendRequest(
            {
                apiService?.getNewListSuspend(currentPage, pageSize, NotificationMessageBean().apply {
                    type = "5"
                })
            },
             apiRequestOptions
        ).map { it?.list?:emptyList() }
    }

    suspend fun getInfoById(id:String): Flow<NotificationMessageBean>? {
        return sendRequest({ apiService!!.getNewInfoByIdSuspend(id)  }, ApiRequestOptions.getDefault().apply {
            isShowDialog = true
        })
    }

}
