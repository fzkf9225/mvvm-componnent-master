package com.casic.otitan.googlegps.socket;

/**
 * Created by fz on 2023/10/18 10:56
 * describe :连接回调
 */
public interface SocketCallbackListener {
    void callBack(boolean isConnect,String errorMsg);
}
