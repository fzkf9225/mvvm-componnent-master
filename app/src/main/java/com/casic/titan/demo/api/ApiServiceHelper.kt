package com.casic.titan.demo.api

import com.casic.titan.demo.bean.ForestBean
import com.casic.titan.mqttcomponent.MqttBean
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.bean.base.PageBean
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by fz on 2020/2/7
 * describe:接口相关配置
 */
interface ApiServiceHelper {
    @get:GET("/cloud-drone/config/getCloudConfig")
    val cloudConfig: Observable<MqttBean>

    @GET("/gzy-resource-app/res/forest/list")
    fun forestList(@Query("current") current: Int, @Query("size") size: Int): Observable<PageBean<ForestBean>>
}