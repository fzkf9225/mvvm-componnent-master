package com.casic.otitan.common.enums;


/**
 * created by fz on 2025/6/27 09:08
 * describe:WebView的地址类型
 */
public enum WebViewUrlTypeEnum {
    /**
     * WebView的地址类型，
     */
    INTERNET(1, "网络地址"),
    ASSETS(2, "本地assets"),
    AUTO(3, "智能判断"),
    ;

    private WebViewUrlTypeEnum(int type, String describe) {
        this.type = type;
        this.describe = describe;
    }

    public final int type;
    public final String describe;

}

