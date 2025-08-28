package com.casic.titan.usercomponent.api

import com.casic.titan.userapi.bean.UserInfo
import com.casic.titan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.titan.usercomponent.bean.RequestLoginBean
import com.casic.titan.usercomponent.bean.TokenBean
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import pers.fz.mvvm.api.BaseApiService
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Created by fz on 2023/4/25 13:08
 * describe :
 */
interface UserApiService : BaseApiService {
    /**
     * 获取token
     */
    @POST("user/loginApp")
    fun getToken(@Body requestLoginBean: RequestLoginBean): Observable<TokenBean>

    @GET("code/getCode")
    fun getImageCode(@Query("num") randomNumber: String): Observable<GraphicVerificationCodeBean>

    /**
     * 获取token
     */
    @POST("auth/refresh-token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("refreshToken") refreshToken: String?
    ): Observable<TokenBean>
    /**
     * 获取token
     */
    @POST("auth/refresh-token")
    fun refreshTokenFlow(
        @Field("refreshToken") refreshToken: String?
    ): Flowable<TokenBean>

    @GET("user/getLoginUserInfo")
    fun getUserInfo(): Observable<UserInfo>

    @GET("user/getLoginUserInfo")
    fun getUserInfoFlow(): Flowable<UserInfo>

    @PUT("user")
    fun modifyUserInfo(@Body userInfo: UserInfo): Observable<Any>

    @GET("user/logoutApp")
    fun logout(): Observable<Any>
}
