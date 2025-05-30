package com.casic.titan.demo.repository

import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.bean.NotificationMessageBean
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.inter.RetryService
import pers.fz.mvvm.repository.PagingRepositoryImpl

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
class KtDemoPagingRepositoryImpl(
    private val apiServiceHelper: ApiServiceHelper,
    retryService: RetryService,
    baseView: BaseView
) :
    PagingRepositoryImpl<NotificationMessageBean, BaseView>(retryService, baseView) {
    override fun requestPaging(
        currentPage: Int,
        pageSize: Int
    ): Observable<List<NotificationMessageBean>> {
        val notificationMessageBean : NotificationMessageBean = NotificationMessageBean().apply {
            type = "1"
        }
        return sendRequest<List<NotificationMessageBean>>(
            apiServiceHelper.getNewList(currentPage,pageSize,notificationMessageBean).map {
                if (it==null) {
                    return@map emptyList<NotificationMessageBean>()
                }
                return@map it.list
            },
            requestConfigEntity
        ) as Observable<List<NotificationMessageBean>>
    }
}
