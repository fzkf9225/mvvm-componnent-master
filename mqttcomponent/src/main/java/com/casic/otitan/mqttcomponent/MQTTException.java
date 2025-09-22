package com.casic.otitan.mqttcomponent;


/**
 * Create by CherishTang on 2019/8/1
 * describe:自定义异常封装
 */
public class MQTTException extends Exception {

    private String errorMsg;
    private int errorCode;

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MQTTException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorMsg = errorMsg;
    }

    public MQTTException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public MQTTException(String message, int errorCode) {
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

}
