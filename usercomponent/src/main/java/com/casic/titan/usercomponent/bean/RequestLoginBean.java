package com.casic.titan.usercomponent.bean;

import androidx.databinding.BaseObservable;

import com.casic.titan.usercomponent.api.UserAccountHelper;


/**
 * Create by CherishTang on 2019/10/18 0018
 * describe:
 */
public class RequestLoginBean extends BaseObservable {
    private String username = UserAccountHelper.getAccount();
    private String password;
    private int loginWay = 1;//登录方式：1、账号密码登录；2、验证码登录

    public RequestLoginBean() {
    }

    public RequestLoginBean(String username, String password, int loginWay) {
        this.username = username;
        this.password = password;
        this.loginWay = loginWay;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLoginWay() {
        return loginWay;
    }

    public void setLoginWay(int loginWay) {
        this.loginWay = loginWay;
    }
}
