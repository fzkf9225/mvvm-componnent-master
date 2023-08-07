package com.casic.titan.usercomponent.api;

import com.casic.titan.mqttcomponent.MqttBean;
import com.casic.titan.usercomponent.bean.UserInfo;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;
import com.casic.titan.usercomponent.bean.WorkSpaceBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
    Observable<UserInfo> getToken(@Field("username") String username, @Field("password") String password,
                                  @Field("grant_type") String grantType, @Field("scope") String scope,
                                  @Field("tenantId") String tenantId);

    /**
     * 获取token
     *
     * @return
     */
    @POST("blade-auth/oauth/token")
    @FormUrlEncoded
    Observable<UserInfo> refreshToken(@Field("grant_type") String grantType, @Field("scope") String scope,
                                    @Field("refresh_token") String refreshToken);

    @GET("/cloud-drone/config/getCloudConfig")
    Observable<MqttBean> getCloudConfig();

    @GET("/cloud-drone/manage/api/v1/workspaces/current")
    Observable<WorkSpaceBean> getWorkSpace();

    @GET("/cloud-drone/manage/api/v1/devices/getDevicesByWorkspaceId")
    Observable<List<WebSocketSubscribeBean>> getWebSocketSubscribeInfo(@Query("workspaceId") String workspaceId);
}
