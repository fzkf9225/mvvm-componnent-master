package com.casic.titan.googlegps.socket;

/**
 * Created by fz on 2023/10/19 13:59
 * describe :
 */
public interface OnConnectionChangedListener {
    /**
     * 连接成功
     */
    void onConnected();

    /**
     * 连接中断
     */
    void onDisconnection();

    /**
     * 连接关闭
     */
    void onClosed();

    /**
     * handler异常
     * @param socketException 自定义异常信息
     */
    void onException(SocketException socketException);
}
