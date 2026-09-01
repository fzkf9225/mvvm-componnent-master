package io.coderf.arklab.googlegps.socket;

/**
 * 连接回调
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/10/18 10:56
 */
public interface SocketCallbackListener {
    void callBack(boolean isConnect,String errorMsg);
}
