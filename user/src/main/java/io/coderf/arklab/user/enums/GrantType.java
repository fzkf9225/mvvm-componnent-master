package io.coderf.arklab.user.enums;

/**
 * GrantType 枚举。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/17 10:24
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
