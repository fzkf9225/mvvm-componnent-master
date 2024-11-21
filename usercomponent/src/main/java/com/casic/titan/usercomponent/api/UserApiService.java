package com.casic.titan.usercomponent.api;

import com.casic.titan.usercomponent.bean.TokenBean;
import com.casic.titan.usercomponent.bean.UserInfo;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by fz on 2023/4/25 13:08
 * describe :
 */
public interface UserApiService {
    /**
     * 获取token
     *
     * @return
     */
    @POST("http://192.168.0.23:19901/blade-auth/oauth/token")
    @FormUrlEncoded
    Observable<TokenBean> getToken(@Field("userName") String username, @Field("password") String password,
                                   @Field("code") String code, @Field("num") String num);

    /**
     * 获取token
     *
     * @return
     */
    @POST("blade-auth/oauth/token")
    @FormUrlEncoded
    Observable<TokenBean> refreshToken(@Field("refresh_token") String refreshToken,@Field("tenantld") String tenantld,
                                       @Field("grant_type") String grant_type, @Field("scope") String scope,
                                       @Field("type") String type);

    @GET("http://192.168.0.23:19901/pms/user/getLoginUserInfo")
    Observable<UserInfo> getUserInfo();

}
