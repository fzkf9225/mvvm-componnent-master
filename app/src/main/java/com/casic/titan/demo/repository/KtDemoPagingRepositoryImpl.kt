package com.casic.titan.demo.repository

import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.bean.RegionBean
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.api.ApiRetrofit
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.inter.RetryService
import pers.fz.mvvm.repository.PagingRepositoryImpl
import pers.fz.mvvm.util.log.LogUtil

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
            apiServiceHelper.getRegionTree().map {
                if (it.isNullOrEmpty()) {
                    return@map emptyList<RegionBean>()
                }
                return@map it
            },
            requestConfigEntity
        ) as Observable<List<RegionBean>>
    }
}
