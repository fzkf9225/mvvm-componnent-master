package com.casic.titan.usercomponent.impl;

import com.casic.titan.userapi.UserService;
import com.casic.titan.usercomponent.api.UserAccountHelper;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;

import java.util.List;

import javax.inject.Inject;

import pers.fz.mvvm.util.apiUtil.StringUtil;


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
        return UserAccountHelper.getUser().getUser_id();
    }

    @Override
    public String getWorkSpaceId() {
        if (!UserAccountHelper.isLogin()) {
            return null;
        }
        if (UserAccountHelper.getWorkSpace() == null) {
            return null;
        }
        return UserAccountHelper.getWorkSpace().getWorkspaceId();
    }

    @Override
    public String getWorkSpaceName() {
        if (!UserAccountHelper.isLogin()) {
            return null;
        }
        if (UserAccountHelper.getWorkSpace() == null) {
            return null;
        }
        return UserAccountHelper.getWorkSpace().getWorkspaceName();
    }

    @Override
    public String getWorkerSpaceSn() {
        List<WebSocketSubscribeBean> subscribeBeanList = UserAccountHelper.getWebSocketSubscribe();
        if (subscribeBeanList == null || subscribeBeanList.isEmpty()) {
            return null;
        }
        StringBuilder sb = null;
        for (WebSocketSubscribeBean subscribeBean : subscribeBeanList) {
            if (sb == null) {
                sb = new StringBuilder();
                sb.append(subscribeBean.getDeviceSn())
                        .append(",")
                        .append(subscribeBean.getChildSn());
            } else {
                sb.append(",").append(subscribeBean.getDeviceSn())
                        .append(",")
                        .append(subscribeBean.getChildSn());
            }
        }
        return sb.toString();
    }

    @Override
    public boolean workSpaceContainSn(String sn) {
        if (StringUtil.isEmpty(sn)) {
            return false;
        }
        List<WebSocketSubscribeBean> subscribeBeanList = UserAccountHelper.getWebSocketSubscribe();
        if (subscribeBeanList == null || subscribeBeanList.isEmpty()) {
            return false;
        }
        for (WebSocketSubscribeBean subscribeBean : subscribeBeanList) {
            if (sn.equalsIgnoreCase(subscribeBean.getDeviceSn())) {
                return true;
            }
        }
        return false;
    }


}
