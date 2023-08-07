package com.casic.titan.usercomponent.bean;

/**
 * Create by CherishTang on 2019/10/18 0018
 * describe:登录返回值token
 */
public class LoginResultBean {
    private String token;
    private String refreshToken;//登录过期使用

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
