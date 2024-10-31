package pers.fz.mvvm.bean.Code;

/**
 * Created by fz on 2017/10/16.
 * describe：服务器返回码
 */
public class ResponseCode {
    /**
     * 登录过期
     */
    public static final String LOGIN_PAST = "20001";
    /**
     * 未登录
     */
    public static final String UN_LOGIN = "20002";
    /**
     * 成功
     */
    public static final String SUCCESS = "00000";
    /**
     * 成功
     */
    public static final String OK = "0";
    /**
     * 用户令牌信息不一致，请重新登陆
     */
    public static final String TOKEN_ERROR = "401";
    /**
     * 刷新令牌与token不一致
     */
    public static final String REFRESH_TOKEN_ERROR = "20004";
    /**
     * 刷新令牌的有效期已过
     */
    public static final String REFRESH_TOKEN_PAST = "20005";
    /**
     * 请求未授权
     */
    public static final String NO_PERMISSION = "401";
    /**
     * 接口异常
     */
    public static final String FAILURE = "10001";
    /**
     * appid错误
     */
    public static final String APP_ID_ERROR = "10002";
    /**
     * 签名验证失败
     */
    public static final String SIGN_ERROR = "10003";
    /**
     * 请求超时
     */
    public static final String TIME_OUT = "10004";
    /**
     * 未知来源
     */
    public static final String UNKOWN_RESOURCE = "10005";
    /**
     * 禁止访问
     */
    public static final String FORBID = "10006";
    /**
     * 接口停用
     */
    public static final String API_OUT_OF_SERVICE = "10007";
    /**
     * 接口维护中
     */
    public static final String API_UPDATING = "10008";
    /**
     * 无uuid
     */
    public static final String NO_UUID = "10009";
    /**
     * 未知异常
     */
    public static final String UNKNOWN_ERROR = "11000";

    public static boolean isOK(String code) {
        return code.equals(OK);
    }

    public static boolean isSuccess(String code) {
        return code.equals(SUCCESS);
    }
}
