package com.casic.titan.demo.api

import com.casic.titan.demo.bean.NotificationMessageBean
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.api.BaseApiService
import pers.fz.mvvm.bean.base.PageBean
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by fz on 2020/2/7
 * describe:接口相关配置
 */
interface ApiServiceHelper : BaseApiService {

    /**
     * 获取行政区划树
     */
    @POST("news/{page}/{size}")
    fun getNewList(
        @Path("page") page: Int,
        @Path("size") size: Int,
        @Body notificationMessageBean: NotificationMessageBean
    ): Observable<PageBean<NotificationMessageBean>>
}