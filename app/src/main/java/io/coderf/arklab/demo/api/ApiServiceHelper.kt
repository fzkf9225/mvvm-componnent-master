package io.coderf.arklab.demo.api

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.common.bean.base.PageBean
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.bean.RequestNotificationBean
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 接口相关配置
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2020/2/7
 */
interface ApiServiceHelper : BaseApiService {


    /**
     * 获取行政区划树
     */
    @POST("news/findPage/{page}/{size}")
    suspend fun getNewListSuspend(
        @Path("page") page: Int,
        @Path("size") size: Int,
        @Body request: RequestNotificationBean
    ): PageBean<NotificationMessageBean>

    /**
     * 获取行政区划树
     */
    @GET("news/findOne/{id}")
    suspend fun getNewInfoByIdSuspend(
        @Path("id") id: String
    ): NotificationMessageBean
}