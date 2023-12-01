package com.casic.titan.demo.api;

import com.casic.titan.mqttcomponent.MqttBean;
import com.casic.titan.usercomponent.bean.UserInfo;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

/**
 * Created by fz on 2020/2/7
 * describe:接口相关配置
 */
public interface ApiServiceHelper {

    @GET("/cloud-drone/config/getCloudConfig")
    Observable<MqttBean> getCloudConfig();
}
