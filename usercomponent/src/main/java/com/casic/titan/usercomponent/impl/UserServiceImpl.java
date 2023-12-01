package com.casic.titan.usercomponent.impl;

import com.casic.titan.userapi.UserService;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;

import java.util.List;

import javax.inject.Inject;

import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2021/6/30 9:24
 * describe:
 */
public class UserServiceImpl implements UserService {
    @Inject
    public UserServiceImpl() {
    }

    @Override
    public boolean isLogin() {
        return UserAccountHelper.isLogin();
    }

    @Override
    public boolean isLoginPast(String code) {
        return UserAccountHelper.isLoginPast(code);
    }

    @Override
    public boolean isNoPermission(String code) {
        return UserAccountHelper.isNoPermission(code);
    }

    @Override
    public String getToken() {
        return UserAccountHelper.getToken();
    }

    @Override
    public void saveLoginPast(boolean isSuccess) {
        UserAccountHelper.saveLoginPast(isSuccess);
    }

    @Override
    public String getAccount() {
        return UserAccountHelper.getAccount();
    }

    @Override
    public String getPassword() {
        return UserAccountHelper.getPassword();
    }

    @Override
    public void setToken(String token) {
        UserAccountHelper.setToken(token);
    }

    @Override
    public String getUserId() {
        if (!UserAccountHelper.isLogin()) {
            return null;
        }
        return UserAccountHelper.getUser().getId();
    }


}
