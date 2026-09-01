package io.coderf.arklab.wscomponent;

/**
 * msgId
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/10 10:53
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
