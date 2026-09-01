package io.coderf.arklab.wscomponent;

/**
 * 长链接消息类型
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/5 9:52
 */
public enum PushEnum {
    /**
     * 动态url
     */
    PING("ping",1,"心跳"),
    PONG("pong",2,"心跳返回"),
    PONG_SYMBOL("\"pong\"",2,"心跳返回"),
    PUSH("push",3,"信息推送")
    ;
    private String key;
    private int messageType;
    private String describe;

    PushEnum(String key,int messageType, String describe) {
        this.key = key;
        this.messageType = messageType;
        this.describe = describe;
    }

    public String getKey() {
        return key;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getDescribe() {
        return describe;
    }
}
