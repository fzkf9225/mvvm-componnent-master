package com.casic.titan.mqttcomponent;

/**
 * created by fz on 2022-4-18 9:48
 * describe:
 **/
public interface MqttCallback {
    /**
     * mqtt链接成功
     */
    void onConnectedSuccess(boolean reconnect);

    /**
     * mqtt断开连接
     */
    void disConnection();

    void mqttException(MQTTException exception);
}
