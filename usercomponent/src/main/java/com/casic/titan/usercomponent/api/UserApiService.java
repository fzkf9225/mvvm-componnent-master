package com.casic.titan.usercomponent.api;

import com.casic.titan.mqttcomponent.MqttBean;
import com.casic.titan.usercomponent.bean.TokenBean;
import com.casic.titan.usercomponent.bean.UserInfo;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;
import com.casic.titan.usercomponent.bean.WorkSpaceBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    @POST("blade-auth/oauth/token")
    @FormUrlEncoded
    Observable<TokenBean> getToken(@Field("username") String username, @Field("password") String password,
                                   @Field("grant_type") String grantType, @Field("scope") String scope,
                                   @Field("tenantId") String tenantId,@Field("type") String type);

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

    @GET("blade-system/user/info/{userId}")
    Observable<UserInfo> getUserInfo(@Path("userId") String userId);

}
