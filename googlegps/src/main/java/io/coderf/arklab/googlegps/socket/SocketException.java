package io.coderf.arklab.googlegps.socket;


/**
 * Create by fz on 2019/8/1
 * 自定义异常封装
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
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
