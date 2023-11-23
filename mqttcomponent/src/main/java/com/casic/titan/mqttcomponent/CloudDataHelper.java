package com.casic.titan.mqttcomponent;

/**
 * Created by fz on 2023/4/28 18:26
 * describe :获取mqtt缓存信息
 */
public class CloudDataHelper {

    public static void saveMqttData(MqttBean mqttBean) {
        MMKVHelper.getInstance().put(Constants.MQTT_DATA, mqttBean);
    }

    public static MqttBean getMqttData() {
        return (MqttBean) MMKVHelper.getInstance().getParcelable(Constants.MQTT_DATA, MqttBean.class);
    }

    public static void saveClientId(String clientId) {
        MMKVHelper.getInstance().put(Constants.CLIENT_ID, clientId);
    }

    public static String getClientId() {
        return  MMKVHelper.getInstance().getString(Constants.CLIENT_ID);
    }

    public static String getAddress() {
        return getMqttData() == null ? null : getMqttData().getMqtt_addr();
    }

    public static String getUserName() {
        return getMqttData() == null ? null : getMqttData().getMqtt_username();
    }

    public static String getPassword() {
        return getMqttData() == null ? null : getMqttData().getMqtt_password();
    }
}
