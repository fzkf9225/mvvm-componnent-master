package com.casic.otitan.usercomponent.impl;

import com.casic.otitan.userapi.UserService;
import com.casic.otitan.usercomponent.api.UserAccountHelper;

import javax.inject.Inject;


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
