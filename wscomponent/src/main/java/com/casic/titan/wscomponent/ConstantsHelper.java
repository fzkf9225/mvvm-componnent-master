package com.casic.titan.wscomponent;

/**
 * Created by fz on 2023/5/10 14:08
 * describe :默认值，如果配置文件中没有则取值这里的，如果配置文件中有就以配置文件的为准
 */
public class ConstantsHelper {
    public static final String CHANNEL_ID = "10001";
    public static final String CHANNEL_NAME = "服务端消息推送";
    public static final int NOTIFY_ID = 5001;
    /**
     * 配置文件中的Key
     */
    public static final String WEB_SOCKET_CHANNEL_ID = "WEB_SOCKET_CHANNEL_ID";
    public static final String WEB_SOCKET_CHANNEL_NAME = "WEB_SOCKET_CHANNEL_NAME";
    public static final String WEB_SOCKET_NOTIFY_ID = "WEB_SOCKET_NOTIFY_ID";
}
