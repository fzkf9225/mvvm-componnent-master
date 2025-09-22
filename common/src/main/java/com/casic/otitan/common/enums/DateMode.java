package com.casic.otitan.common.enums;

/**
 * created by fz on 2024/12/2 11:08
 * describe:日期选择日模式
 */
public enum DateMode {
    /**
     * BaseActivity的toolbar样式
     */
    YEAR_MONTH_DAY(0, "dialog模式为年月日"),
    YEAR_MONTH(1, "dialog模式为年月"),
    YEAR(2, "dialog模式为年"),
    YEAR_MONTH_DAY_HOUR_MINUTE_SECOND(3, "dialog模式为年月日时分秒"),
    YEAR_MONTH_DAY_HOUR_MINUTE(4, "dialog模式为年月日时分"),
    YEAR_MONTH_DAY_HOUR(5, "dialog模式为年月日时"),
    HOUR_MINUTE_SECOND(6, "dialog模式为时分秒"),
    HOUR_MINUTE(7, "dialog模式为时分"),
    ;

    private DateMode(int model, String describe) {
        this.model = model;
        this.describe = describe;
    }

    public final int model;
    public final String describe;

    public static DateMode getMode(int model) {
        for (DateMode value : values()) {
            if (value.model == model) {
                return value;
            }
        }
        return YEAR_MONTH_DAY;
    }
}

