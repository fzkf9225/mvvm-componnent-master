package com.casic.otitan.mqttcomponent;

/**
 * Created by fz on 2023/5/9 10:12
 * describe :
 */
public class ServerMessage {
    private String topic;
    private String message;

    public ServerMessage() {
    }

    public ServerMessage(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
