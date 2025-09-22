package com.casic.otitan.wscomponent;

/**
 * Created by fz on 2023/5/5 09:52
 * describe:服务器回调消息
 */
public class PushMessageBean<T> {
    private String biz_code;
    private String message;
    private String version;
    private String timestamp;
    private T data;

    public PushMessageBean() {
    }

    public PushMessageBean(String biz_code, String message) {
        this.biz_code = biz_code;
        this.message = message;
    }

    public PushMessageBean(String biz_code, String message, T data) {
        this.biz_code = biz_code;
        this.message = message;
        this.data = data;
    }

    public String getBiz_code() {
        return biz_code;
    }

    public void setBiz_code(String biz_code) {
        this.biz_code = biz_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PushMessageBean{" +
                "biz_code='" + biz_code + '\'' +
                ", message='" + message + '\'' +
                ", version='" + version + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", data=" + data +
                '}';
    }
}
