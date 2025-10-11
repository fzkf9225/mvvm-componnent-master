package com.casic.otitan.common.base;

import androidx.annotation.NonNull;

/**
 * Create by CherishTang on 2019/8/1
 * describe: 自定义异常封装
 */
public class BaseException extends RuntimeException {

    public enum ErrorType {
        PARSE_ERROR("1001", "解析数据时发生异常！"),
        BAD_NETWORK("1002", "服务器或网络异常，请检查后再试！"),
        CONNECT_ERROR("1003", "服务器连接失败或未知主机异常！"),
        CONNECT_TIMEOUT("1004", "请求超时，请检查网络环境或稍后再试！"),
        OTHER("1005", "检测未知错误"),
        REQUEST_ERROR("1006", "请求失败，请稍后再试！"),
        TOKEN_ERROR("1007", "登录超时"),
        DJI_ERROR("1008", null),
        DJI_MEDIA_ERROR("1009", null),
        OSS_ERROR("1010", ""),
        STS_ERROR("1011", null),
        OSS_FILE_NOT_FIND_ERROR("1012", null),
        MQTT_CONNECTING_ERROR("1013", null),
        MQTT_DIS_CONNECTING_ERROR("1014", null),
        WEBSOCKET_SUBSCRIBE_ERROR("1015", null),
        WEBSOCKET_NO_PERMISSION_ERROR("1016", null),
        NOT_FOUND("1017", "暂无数据"),
        DELETE_SUCCESS("1018", "删除成功"),
        DOWNLOAD_URL_404("1019", "下载地址错误"),
        DOWNLOAD_NOT_PERMISSION("1020", "请先同意文件管理权限"),
        DOWNLOADING_ERROR("1021", "检测到当前文件已有下载任务正在进行中");

        private final String code;
        private final String msg;

        ErrorType(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    private final String errorMsg;
    private final String errorCode;

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // 基本构造方法
    public BaseException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorMsg = errorMsg;
        this.errorCode = ErrorType.OTHER.getCode();
    }

    public BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    // 使用 ErrorType 构造
    public BaseException(ErrorType errorType) {
        this(errorType.getMsg(), errorType.getCode());
    }

    public BaseException(ErrorType errorType, Throwable cause) {
        this(errorType.getMsg(), cause, errorType.getCode());
    }

    public BaseException(ErrorType errorType, String customMsg) {
        this(customMsg, errorType.getCode());
    }

    public BaseException(ErrorType errorType, String customMsg, Throwable cause) {
        this(customMsg, cause, errorType.getCode());
    }

    // 快速创建方法
    public static BaseException asParseError(Throwable cause) {
        return new BaseException(ErrorType.PARSE_ERROR, cause);
    }

    public static BaseException asNetworkError(Throwable cause) {
        return new BaseException(ErrorType.BAD_NETWORK, cause);
    }

    public static BaseException asConnectError(Throwable cause) {
        return new BaseException(ErrorType.CONNECT_ERROR, cause);
    }

    public static BaseException asTimeoutError(Throwable cause) {
        return new BaseException(ErrorType.CONNECT_TIMEOUT, cause);
    }

    public static BaseException asTokenError() {
        return new BaseException(ErrorType.TOKEN_ERROR);
    }

    @NonNull
    @Override
    public String toString() {
        return "BaseException{" +
                "errorMsg='" + errorMsg + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}