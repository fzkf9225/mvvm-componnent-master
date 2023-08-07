package com.casic.titan.mqttcomponent;

/**
 * Created by fz on 2023/4/28 14:27
 * describe :
 */
public class MQTTModelEntity<T>{
    private String tid;
    private String bid;
    private long timestamp;
    private String method;
    /**
     * 网关SN
     */
    private String gateway;
    private T data;

    public MQTTModelEntity() {
        this.bid =java.util.UUID.randomUUID().toString();
        this.tid = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public MQTTModelEntity(String method) {
        this.bid =java.util.UUID.randomUUID().toString();
        this.tid = java.util.UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.method = method;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
