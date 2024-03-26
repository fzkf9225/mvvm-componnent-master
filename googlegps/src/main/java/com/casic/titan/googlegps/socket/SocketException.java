package com.casic.titan.googlegps.socket;


/**
 * Create by CherishTang on 2019/8/1
 * describe:自定义异常封装
 */
public class SocketException extends Exception {
    private final String errorMsg;
    private int errorCode;

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public SocketException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorMsg = errorMsg;
    }

    public SocketException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public SocketException(String message, int errorCode) {
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

}
