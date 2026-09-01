package io.coderf.arklab.wscomponent;

/**
 * WebSocketEvent 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/6/13 11:36
 */
public class WebSocketEvent<T>{
    private T message;
    private PushEnum messageType;
    public WebSocketEvent(PushEnum messageType, T message) {
        this.messageType = messageType;
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public PushEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(PushEnum messageType) {
        this.messageType = messageType;
    }
}
