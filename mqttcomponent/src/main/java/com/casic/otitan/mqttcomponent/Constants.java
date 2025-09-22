package com.casic.otitan.mqttcomponent;

/**
 * Created by fz on 2023/4/28 14:29
 * describe :默认值，如果配置文件中没有则取值这里的，如果配置文件中有就以配置文件的为准
 */
public class Constants {
    public final static String MQTT_DATA = "mqtt_data";
    public static final String CHANNEL_ID = "10001";
    public static final String CHANNEL_NAME = "服务端消息推送";
    public static final int NOTIFY_ID = 5001;
    public final static String CLIENT_ID = "client_id";
    /**
     * mqtt版本，我们自定义的版本用于适配，并非官方mqtt版本
     */
    public final static String VERSION= "V1.0";
    /**
     * 配置文件中的Key
     */
    public static final String MQTT_CHANNEL_ID = "MQTT_CHANNEL_ID";
    public static final String MQTT_CHANNEL_NAME = "MQTT_CHANNEL_NAME";
    public static final String MQTT_NOTIFY_ID = "MQTT_NOTIFY_ID";

}
