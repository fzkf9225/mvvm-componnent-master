package com.casic.titan.demo.api

import com.casic.titan.demo.bean.ForestBean
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.bean.base.PageBean
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by fz on 2020/2/7
 * describe:接口相关配置
 */
interface ApiServiceHelper {

    @POST("maintain/list/{page}/{size}")
    fun forestList(@Path("page") current: Int, @Path("size") size: Int,@Body body: Map<String,String>): Observable<PageBean<ForestBean>>
}