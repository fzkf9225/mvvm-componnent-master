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
        OTHER("1005", "检测到未知错误"),
        REQUEST_ERROR("1006", "请求失败，请稍后再试！"),
        TOKEN_ERROR("1007", "登录超时或登录信息已过期！"),
        DJI_ERROR("1008", "大疆设备异常"),
        DJI_MEDIA_ERROR("1009", "大疆媒体管理异常"),
        OSS_ERROR("1010", "OSS服务异常"),
        STS_ERROR("1011", "STS服务异常"),
        OSS_FILE_NOT_FIND_ERROR("1012", "未查找到本地文件信息或本地文件不存在"),
        MQTT_CONNECTING_ERROR("1013", "mqtt服务连接异常！"),
        MQTT_DIS_CONNECTING_ERROR("1014", "mqtt失去连接！"),
        WEBSOCKET_SUBSCRIBE_ERROR("1015", "socket订阅异常！"),
        WEBSOCKET_NO_PERMISSION_ERROR("1016", "暂无权限"),
        NOT_FOUND("1017", "暂无数据"),
        DELETE_SUCCESS("1018", "删除成功"),
        DOWNLOAD_URL_404("1019", "下载地址错误"),
        DOWNLOAD_NOT_PERMISSION("1020", "请先同意文件管理权限"),
        DOWNLOADING_ERROR("1021", "检测到当前文件已有下载任务正在进行中"),
        // 认证授权相关
        UNAUTHORIZED("1101", "未授权访问"),
        FORBIDDEN("1102", "访问被禁止"),
        LOGIN_FAILED("1103", "登录失败"),
        ACCOUNT_LOCKED("1104", "账号已被锁定"),
        SESSION_EXPIRED("1105", "会话已过期"),

        // 数据验证相关
        VALIDATION_ERROR("1201", "数据验证失败"),
        PARAMETER_MISSING("1202", "参数缺失"),
        PARAMETER_INVALID("1203", "参数无效"),
        DATA_DUPLICATE("1204", "数据重复"),

        // 文件操作相关
        FILE_UPLOAD_ERROR("1301", "文件上传失败"),
        FILE_DOWNLOAD_ERROR("1302", "文件下载失败"),
        FILE_SIZE_EXCEEDED("1303", "文件大小超出限制"),
        FILE_FORMAT_UNSUPPORTED("1304", "文件格式不支持"),
        FILE_NOT_FOUND("1305", "文件不存在"),
        FILE_READ_ERROR("1306", "文件读取失败"),
        FILE_WRITE_ERROR("1307", "文件写入失败"),

        // 数据库相关
        DATABASE_ERROR("1401", "数据库操作异常"),
        RECORD_NOT_FOUND("1402", "记录不存在"),
        CONSTRAINT_VIOLATION("1403", "数据约束冲突"),

        // 业务逻辑相关
        BUSINESS_ERROR("1501", "业务逻辑异常"),
        OPERATION_NOT_ALLOWED("1502", "操作不允许"),
        INSUFFICIENT_BALANCE("1503", "余额不足"),
        RESOURCE_EXHAUSTED("1504", "资源已耗尽"),
        SERVICE_UNAVAILABLE("1505", "服务暂不可用"),

        // 第三方服务相关
        THIRD_PARTY_ERROR("1601", "第三方服务异常"),
        PAYMENT_ERROR("1602", "支付服务异常"),
        SMS_SEND_ERROR("1603", "短信发送失败"),
        EMAIL_SEND_ERROR("1604", "邮件发送失败"),

        // 设备硬件相关
        CAMERA_ERROR("1701", "相机异常"),
        GPS_ERROR("1702", "GPS定位异常"),
        SENSOR_ERROR("1703", "传感器异常"),
        STORAGE_ERROR("1704", "存储空间不足"),
        BATTERY_LOW("1705", "电量不足"),

        // 网络状态相关
        NETWORK_UNAVAILABLE("1801", "网络不可用"),
        SSL_ERROR("1802", "SSL证书错误"),
        PROXY_ERROR("1803", "代理服务器错误"),

        // 系统资源相关
        MEMORY_OVERFLOW("1901", "内存溢出"),
        CPU_OVERLOAD("1902", "CPU过载"),
        SYSTEM_BUSY("1903", "系统繁忙"),

        // 用户操作相关
        USER_CANCELED("2001", "用户取消操作"),
        OPERATION_TIMEOUT("2002", "操作超时"),
        RETRY_EXCEEDED("2003", "重试次数超限"),

        // 版本更新相关
        VERSION_TOO_LOW("2101", "版本过低，请升级"),
        UPDATE_REQUIRED("2102", "需要强制更新"),
        UPDATE_AVAILABLE("2103", "有新版本可用"),

        // 权限相关
        PERMISSION_DENIED("2201", "权限不足"),
        LOCATION_PERMISSION_DENIED("2202", "位置权限未授权"),
        CAMERA_PERMISSION_DENIED("2203", "相机权限未授权"),
        STORAGE_PERMISSION_DENIED("2204", "存储权限未授权"),

        // 配置相关
        CONFIG_ERROR("2301", "配置错误"),
        ENVIRONMENT_ERROR("2302", "环境配置异常");

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
    // 认证相关
    public static BaseException asUnauthorized() {
        return new BaseException(ErrorType.UNAUTHORIZED);
    }

    public static BaseException asLoginFailed() {
        return new BaseException(ErrorType.LOGIN_FAILED);
    }

    // 文件操作相关
    public static BaseException asFileUploadError(Throwable cause) {
        return new BaseException(ErrorType.FILE_UPLOAD_ERROR, cause);
    }

    public static BaseException asFileNotFound() {
        return new BaseException(ErrorType.FILE_NOT_FOUND);
    }

    // 权限相关
    public static BaseException asPermissionDenied() {
        return new BaseException(ErrorType.PERMISSION_DENIED);
    }

    public static BaseException asLocationPermissionDenied() {
        return new BaseException(ErrorType.LOCATION_PERMISSION_DENIED);
    }

    // 业务相关
    public static BaseException asBusinessError(String customMsg) {
        return new BaseException(ErrorType.BUSINESS_ERROR, customMsg);
    }

    public static BaseException asOperationNotAllowed() {
        return new BaseException(ErrorType.OPERATION_NOT_ALLOWED);
    }

    // 网络相关
    public static BaseException asNetworkUnavailable() {
        return new BaseException(ErrorType.NETWORK_UNAVAILABLE);
    }

    // 系统相关
    public static BaseException asSystemBusy() {
        return new BaseException(ErrorType.SYSTEM_BUSY);
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