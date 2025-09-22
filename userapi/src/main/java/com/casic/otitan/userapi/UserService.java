package com.casic.otitan.userapi;

/**
 * Created by fz on 2021/6/30 9:23
 * describe:
 */
public interface UserService{
    /**
     * 是否登陆
     * @return true：已登陆
     */
    boolean isLogin();

    /**
     * 是否登陆超时
     * @param code 错误码
     * @return true：登陆超时
     */
    boolean isLoginPast(String code);

    /**
     * 是否没有借口访问权限
     * @param code 错误码
     * @return true：没有借口访问权限
     */
    boolean isNoPermission(String code);

    /**
     * 获取token
     * @return token值
     */
    String getToken();

    /**
     * 保存登陆状态
     * @param isSuccess
     */
    void saveLoginPast(boolean isSuccess);

    /**
     * 获取用户名
     * @return
     */
    String getAccount();

    /**
     * 获取密码
     * @return
     */
    String getPassword();

    /**
     * 保存token
     * @param token
     */
    void setToken(String token);
    String getUserId();

}
