package com.casic.titan.demo.repository

import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.bean.RegionBean
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
    PagingRepositoryImpl<RegionBean, BaseView>(retryService, baseView) {
    override fun requestPaging(
        currentPage: Int,
        pageSize: Int
    ): Observable<List<RegionBean>> {
        return sendRequest<List<RegionBean>>(
            apiServiceHelper.getRegionTree(),
            requestConfigEntity
        ) as Observable<List<RegionBean>>
    }
}
