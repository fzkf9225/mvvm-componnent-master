package com.casic.otitan.googlegps.socket;

/**
 * Created by 方舟 on 2023/5/5 09:52
 * 服务器返回码
 */

public class ResponseCode {

    /**
     * 长链接成功
     */
    public static final String SOCKET_SUCCESS = "200";
    /**
     * 长链接连接失败
     */
    public static final String SOCKET_CONNECT_FAILURE = "201";
    /**
     * 握手失败
     */
    public static final String SOCKET_SHAKE_HAND_FAILURE = "202";
    /**
     * 发送消息失败
     */
    public static final String SEND_MESSAGE_FAILURE = "203";
    /**
     * 其他异常
     */
    public static final String OTHER_FAILURE = "204";
    /**
     * 收到消息
     */
    public static final String RECEIVER_MESSAGE = "205";
    /**
     * 发送消息成功
     */
    public static final String SEND_MESSAGE_SUCCESS = "206";
    /**
     * Service断开连接
     */
    public static final String SERVICE_DISCONNECTING = "207";
}
