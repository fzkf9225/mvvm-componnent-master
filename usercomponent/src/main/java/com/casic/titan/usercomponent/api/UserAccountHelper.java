package com.casic.titan.usercomponent.api;

import android.text.TextUtils;

import com.casic.titan.mqttcomponent.CloudDataHelper;
import com.casic.titan.usercomponent.bean.UserInfo;
import com.casic.titan.usercomponent.bean.WebSocketSubscribeBean;
import com.casic.titan.usercomponent.bean.WorkSpaceBean;

import java.util.List;

import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.MMKVHelper;
import pers.fz.mvvm.bean.Code.ResponseCode;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2017/5/23.
 * 用户登录等相关信息
 */

public class UserAccountHelper {
    public static final String TOKEN_DATA = "TOKEN_DATA";
    static final String TOKEN_DATA_STRING_REFRESH = "TOKEN_DATA_STRING_REFRESH";

    /**
     * 用户登录是否成功
     */
    private static final String USER_STATE = "USER_STATE";
    /**
     * 用户信息
     */
    private static final String USER_INFO = "USER_INFO";

    /**
     * 登录账号
     */
    private static final String USER_ACCOUNT = "USER_ACCOUNT";
    /**
     * 登录密码
     */
    private static final String USER_PASSWORD = "USER_PASSWORD";
    /**
     * api基本参数
     */
    private static final String API_PARAMS = "api_params";
    /**
     * baseUrl地址
     */
    private static final String BASE_URL = "base_url";
    /**
     * WORK_SPACE_ID
     */
    private static final String WORK_SPACE_ID = "WORK_SPACE_ID";
    /**
     * WebSocket订阅消息
     */
    private static final String WEBSOCKET_SUBSCRIBE = "WEBSOCKET_SUBSCRIBE";

    /**
     * 判断是否登录
     *
     * @return 登录状态
     */
    public static boolean isLogin() {
        if (getToken() == null) {
            return false;
        }
        if (getRefreshToken() == null) {
            return false;
        }
        if (getUser() == null) {
            return false;
        }
        if (!getLoginState()) {
            return false;
        }
        if (getWorkSpace() == null) {
            return false;
        }
        if (getWebSocketSubscribe() == null) {
            return false;
        }
        if (CloudDataHelper.getMqttData() == null) {
            return false;
        }
        if (TextUtils.isEmpty(CloudDataHelper.getAddress())) {
            return false;
        }
        return true;
    }

    /**
     * 登录保存用户信息
     *
     * @param userInfo  用户相关信息
     * @param isSuccess 是否登录成功
     */
    public static void saveLoginState(UserInfo userInfo, boolean isSuccess) {
        MMKVHelper.getInstance().put(USER_INFO, userInfo);
        MMKVHelper.getInstance().put(USER_STATE, isSuccess);
    }

    /**
     * 获取登录状态
     *
     * @return 是否登录成功
     */
    private static boolean getLoginState() {
        return MMKVHelper.getInstance().getBoolean(USER_STATE, false);
    }

    /**
     * 设置用户登录信息为失败，用来下次打开app的时候状态为登录失败
     *
     * @param isSuccess 设置用户登录过期，本地版判断
     */
    public static void saveLoginPast(boolean isSuccess) {
        MMKVHelper.getInstance().put(USER_STATE, isSuccess);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static UserInfo getUser() {
        return (UserInfo) MMKVHelper.getInstance().getParcelable(USER_INFO, UserInfo.class);
    }

    /**
     * 退出登录
     */
    public static void exit() {
        MMKVHelper.getInstance().removeValueForKey(USER_INFO);
        MMKVHelper.getInstance().put(USER_STATE, false);
        clearToken();
        clearWorkSpace();
    }

    /**
     * 保存用户账号和密码
     *
     * @param account  账号
     * @param password 密码
     */
    public static void saveUserMessage(String account, String password) {
        MMKVHelper.getInstance().put(USER_ACCOUNT, account);
        MMKVHelper.getInstance().put(USER_PASSWORD, password);
    }

    /**
     * 保存用户账号和密码
     *
     * @param account 账号
     */
    public static void saveAccount(String account) {
        MMKVHelper.getInstance().put(USER_ACCOUNT, account);
    }

    /**
     * 保存用户账号和密码
     *
     * @param password 密码
     */
    public static void savePassword(String password) {
        MMKVHelper.getInstance().put(USER_PASSWORD, password);
    }

    public static String getAccount() {
        return MMKVHelper.getInstance().getString(USER_ACCOUNT, null);
    }

    public static String getPassword() {
        return MMKVHelper.getInstance().getString(USER_PASSWORD, null);
    }

    /**
     * 本地保存token值
     *
     * @param token token值
     */
    public static void setToken(String token) {
        MMKVHelper.getInstance().put(TOKEN_DATA, token);
    }

    /**
     * 清理token值
     */
    static void clearToken() {
        MMKVHelper.getInstance().removeValuesForKeys(new String[]{TOKEN_DATA_STRING_REFRESH, TOKEN_DATA});
    }

    /**
     * 获取token值
     */
    public static String getToken() {
        return MMKVHelper.getInstance().getString(TOKEN_DATA, null);
    }

    /**
     * 本地保存workSpaceId值
     *
     * @param workSpaceBean workSpaceId值
     */
    public static void setWorkSpace(WorkSpaceBean workSpaceBean) {
        MMKVHelper.getInstance().put(WORK_SPACE_ID, workSpaceBean);
    }

    /**
     * 清理token值
     */
    static void clearWorkSpace() {
        MMKVHelper.getInstance().removeValueForKey(WORK_SPACE_ID);
    }

    /**
     * 获取token值
     */
    public static WorkSpaceBean getWorkSpace() {
        return (WorkSpaceBean) MMKVHelper.getInstance().getParcelable(WORK_SPACE_ID, WorkSpaceBean.class);
    }

    /**
     * WebSocket订阅信息保存
     *
     * @param subscribeBeanList 订阅信息
     */
    public static void setWebSocketSubscribe(List<WebSocketSubscribeBean> subscribeBeanList) {
        MMKVHelper.getInstance().setArray(WEBSOCKET_SUBSCRIBE, subscribeBeanList);
    }

    /**
     * 清理WebSocket订阅信息
     */
    static void clearWebSocketSubscribe() {
        MMKVHelper.getInstance().removeValueForKey(WEBSOCKET_SUBSCRIBE);
    }

    /**
     * 获取WebSocket订阅信息
     */
    public static List<WebSocketSubscribeBean> getWebSocketSubscribe() {
        return MMKVHelper.getInstance().getArray(WEBSOCKET_SUBSCRIBE, WebSocketSubscribeBean.class);
    }

    /**
     * 本地保存token值
     *
     * @param token token值
     */
    public static void setRefreshToken(String token) {
        MMKVHelper.getInstance().put(TOKEN_DATA_STRING_REFRESH, token);
    }

    /**
     * 获取token值
     */
    public static String getRefreshToken() {
        return MMKVHelper.getInstance().getString(TOKEN_DATA_STRING_REFRESH);
    }

    public static boolean isLoginPast(String code) {
        return ResponseCode.LOGIN_PAST.equals(code)
                || ResponseCode.TOKEN_ERROR.equals(code)
                || ResponseCode.FAILURE.equals(code);
    }

    public static boolean isNoPermission(String code) {
        return ResponseCode.NOPERMISSION.equals(code);
    }


    /**
     * 保存baseUrl
     *
     * @param baseUrl
     */
    public static void saveBaseUrl(String baseUrl) {
        MMKVHelper.getInstance().put(BASE_URL, baseUrl);
    }

    /**
     * 获取baseUrl请求地址
     */
    public static String getBaseUrl() {
        return MMKVHelper.getInstance().getString(BASE_URL, null);
    }

}
