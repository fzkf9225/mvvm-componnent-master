package com.casic.titan.wscomponent;

/**
 * Created by fz on 2023/5/10 10:38
 * describe :订阅
 */
public class SubscribeBean {
    /**
     * 是否取消订阅，默认是false，可不传，如果需要取消订阅传true
     */
    private boolean cancel;
    /**
     * 要订阅的设备sn号，遥控器和飞行器的sn号都要传，多个直接通过逗号分割。如果需要追加订阅，只需要再发送一次订阅消息，
     * deviceSns传要追加订阅的sn号即可，不需要将之前订阅过的sn号再发一遍
     */
    private String deviceSns;
    /**
     * 订阅的通知类型，逗号拼接，所有类型见SubscribeEnum
     */
    private String topics;

    public SubscribeBean() {
    }

    public SubscribeBean(boolean cancel, String deviceSns, String topics) {
        this.cancel = cancel;
        this.deviceSns = deviceSns;
        this.topics = topics;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public String getDeviceSns() {
        return deviceSns;
    }

    public void setDeviceSns(String deviceSns) {
        this.deviceSns = deviceSns;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }
}
