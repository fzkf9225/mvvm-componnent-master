package io.coderf.arklab.wscomponent;

/**
 * 发送消息监听
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/10 14:36
 */
public interface SendMessageListener {
    void sendResult(String code,String message);
}
