package com.casic.titan.wscomponent;

/**
 * Created by fz on 2023/5/10 10:53
 * describe :msgId
 */
public enum MsgIdEnum {
    /**
     * 订阅消息id
     */
    DRONE_DEVICE_WS("drone-device-ws"),
    /**
     * ping
     */
    DRONE_DEVICE_WS_PP("drone-cloud-ws-pp");

    private String value;

    MsgIdEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
