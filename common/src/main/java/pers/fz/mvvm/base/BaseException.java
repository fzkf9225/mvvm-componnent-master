package pers.fz.mvvm.base;

import java.io.IOException;

/**
 * Create by CherishTang on 2019/8/1
 * describe:自定义异常封装
 */
public class BaseException extends IOException {
    /**
     * 解析数据失败
     */
    public static final String PARSE_ERROR = "1001";
    public static final String PARSE_ERROR_MSG = "解析数据时发生异常！";

    /**
     * 网络问题
     */
    public static final String BAD_NETWORK = "1002";
    public static final String BAD_NETWORK_MSG = "服务器或网络异常，请检查后再试！";
    /**
     * 连接错误
     */
    public static final String CONNECT_ERROR = "1003";
    public static final String CONNECT_ERROR_MSG = "服务器连接失败或未知主机异常！";
    /**
     * 连接超时
     */
    public static final String CONNECT_TIMEOUT = "1004";
    public static final String CONNECT_TIMEOUT_MSG = "请求超时，请检查网络环境或稍后再试！";
    /**
     * 未知错误
     */
    public static final String OTHER = "1005";
    public static final String OTHER_MSG = "检测未知错误";

    /**
     * 其他问题，即服务器返回的请求失败
     */
    public static final String REQUEST_ERROR = "1006";

    /**
     * 登录超时
     */
    public static final String TOKEN_ERROR = "1007";
    public static final String TOKEN_ERROR_MSG = "登录超时";

    /**
     * DJI错误
     */
    public static final String DJI_ERROR = "1008";
    /**
     * 大疆媒体管理异常
     */
    public static final String DJI_MEDIA_ERROR = "1009";
    /**
     * OSS异常
     */
    public static final String OSS_ERROR = "1010";
    /**
     * STS异常
     */
    public static final String STS_ERROR = "1011";
    /**
     * OSS上传时发现图片、录像等文件不存在
     */
    public static final String OSS_FILE_NOT_FIND_ERROR = "1012";
    /**
     * MQTT连接初始化异常
     */
    public static final String MQTT_CONNECTING_ERROR = "1013";
    /**
     * MQTT断开连接异常
     */
    public static final String MQTT_DIS_CONNECTING_ERROR = "1014";
    /**
     * WebSocket订阅消息异常
     */
    public static final String WEBSOCKET_SUBSCRIBE_ERROR = "1015";
    /**
     * WebSocket未授权，即token过期
     */
    public static final String WEBSOCKET_NO_PERMISSION_ERROR = "1016";

    /**
     * 暂无数据
     */
    public static final String NOT_FOUND = "1017";
    public static final String NOT_FOUND_MSG = "暂无数据";
    /**
     * 删除成功
     */
    public static final String DELETE_SUCCESS = "1018";
    public static final String DELETE_SUCCESS_MSG = "删除成功";

    /**
     * 下载地址错误
     */
    public static final String DOWNLOAD_URL_404 = "1019";
    public static final String DOWNLOAD_URL_404_MSG = "下载地址错误";

    /**
     * 保存暂无权限
     */
    public static final String DOWNLOAD_NOT_PERMISSION = "1020";
    public static final String DOWNLOAD_NOT_PERMISSION_MSG = "请先同意文件管理权限";

    /**
     * 当前url已存在下载任务
     */
    public static final String DOWNLOADING_ERROR = "1021";
    public static final String DOWNLOADING_ERROR_MSG = "检测到当前文件已有下载任务正在进行中";

    private final String errorMsg;
    private String errorCode;

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public BaseException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorMsg = errorMsg;
    }

    public BaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    public BaseException(String message, String errorCode) {
        this.errorCode = errorCode;
        this.errorMsg = message;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "errorMsg='" + errorMsg + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
