package com.casic.titan.demo.api

import com.casic.titan.demo.bean.RegionBean
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

/**
 * Created by fz on 2020/2/7
 * describe:接口相关配置
 */
interface ApiServiceHelper {

    /**
     * 获取行政区划树
     */
    @GET("area/findAreaTree")
    fun getRegionTree(): Observable<List<RegionBean>>
}