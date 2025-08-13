package com.casic.titan.usercomponent.enums;

/**
 * Created by fz on 2023/5/17 10:24
 * describe :
 */
public enum GrantType {
    /**
     * 登录
     */
    LOGIN("captcha"),
    /**
     * 刷新token
     */
    REFRESH_TOKEN("refresh_token");
    private final String value;

    GrantType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
