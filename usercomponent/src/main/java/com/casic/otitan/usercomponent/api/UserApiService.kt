package com.casic.otitan.usercomponent.api

import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.otitan.usercomponent.bean.RequestLoginBean
import com.casic.otitan.usercomponent.bean.TokenBean
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import com.casic.otitan.common.api.BaseApiService
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

    /**
     * 获取token
     */
    @POST("user/loginApp")
    suspend fun getTokenSuspend(@Body requestLoginBean: RequestLoginBean): TokenBean

    @GET("code/getCode")
    fun getImageCode(@Query("num") randomNumber: String): Observable<GraphicVerificationCodeBean>

    @GET("code/getCode")
    suspend fun getImageCodeSuspend(@Query("num") randomNumber: String): GraphicVerificationCodeBean

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
    @FormUrlEncoded
    suspend fun refreshTokenSuspend(
        @Field("refreshToken") refreshToken: String?
    ): TokenBean

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
    suspend fun getUserInfoSuspend(): UserInfo

    @GET("user/getLoginUserInfo")
    fun getUserInfoFlow(): Flowable<UserInfo>

    @PUT("user")
    fun modifyUserInfo(@Body userInfo: UserInfo): Observable<Any>

    @GET("user/logoutApp")
    fun logout(): Observable<Any>

    @GET("user/logoutApp")
    suspend fun logoutSuspend(): Any?
}
