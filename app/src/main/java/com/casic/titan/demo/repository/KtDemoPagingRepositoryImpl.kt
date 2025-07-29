package com.casic.titan.demo.repository

import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.bean.NotificationMessageBean
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.inter.RetryService
import pers.fz.mvvm.repository.PagingRepositoryImpl

/**
 * Created by fz on 2023/12/1 15:25
 * describe :注意，如果通过工厂创建的，这里必须是baseView不可是对应的接口，使用的时候可以转换一下
 *             如果这里传递了retryService那么就以这个为准
 */
class KtDemoPagingRepositoryImpl :
    PagingRepositoryImpl<ApiServiceHelper, NotificationMessageBean, BaseView> {
    constructor(
        retryService: RetryService?,
        baseView: BaseView,
        apiService: ApiServiceHelper?
    ) : super(
        retryService,
        baseView,
        apiService
    )

    constructor(baseView: BaseView, apiService: ApiServiceHelper?) : super(
        baseView,
        apiService
    )

    override fun requestPaging(
        currentPage: Int,
        pageSize: Int
    ): Observable<List<NotificationMessageBean>> {
        val notificationMessageBean: NotificationMessageBean = NotificationMessageBean().apply {
            type = "1"
        }
        return sendRequest(
            apiService.getNewList(currentPage, pageSize, notificationMessageBean).map {
                if (it == null) {
                    return@map emptyList<NotificationMessageBean>()
                }
                return@map it.list
            },
            apiRequestOptions
        ) as Observable<List<NotificationMessageBean>>
    }
}
