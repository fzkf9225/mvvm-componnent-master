package com.casic.otitan.mqttcomponent;

/**
 * Created by fz on 2023/5/29 10:30
 * describe :
 */
public enum MqttMethodEnum {
    /**
     * 更新osd
     */
    UPDATE_TOPO("update_topo"),
    /**
     * 开始直播
     */
    LIVE_START_PUSH("live_start_push"),
    /**
     * 关闭直播
     */
    LIVE_STOP_PUSH("live_stop_push"),

    ;

    public String method;

    MqttMethodEnum(String method) {
        this.method = method;
    }
}
