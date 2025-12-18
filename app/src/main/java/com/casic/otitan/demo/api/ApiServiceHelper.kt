package com.casic.otitan.demo.api

import com.casic.otitan.demo.bean.NotificationMessageBean
import io.reactivex.rxjava3.core.Observable
import com.casic.otitan.common.api.BaseApiService
import com.casic.otitan.common.bean.base.PageBean
import retrofit2.http.Body
import retrofit2.http.GET
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
    @POST("news/findPage/{page}/{size}")
    fun getNewList(
        @Path("page") page: Int,
        @Path("size") size: Int,
        @Body notificationMessageBean: NotificationMessageBean
    ): Observable<PageBean<NotificationMessageBean>>

    /**
     * 获取行政区划树
     */
    @POST("news/findPage/{page}/{size}")
    suspend fun getNewListSuspend(
        @Path("page") page: Int,
        @Path("size") size: Int,
        @Body notificationMessageBean: NotificationMessageBean
    ): PageBean<NotificationMessageBean>

    /**
     * 获取行政区划树
     */
    @GET("news/findOne/{id}")
    suspend fun getNewInfoByIdSuspend(
        @Path("id") id: String
    ): NotificationMessageBean
}