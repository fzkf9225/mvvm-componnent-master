package com.casic.otitan.common.enums;

import android.text.TextUtils;

/**
 * created by fz on 2024/10/24 11:08
 * describe:BaseActivity的toolbar样式,暂时未实现，后续添加
 */
public enum ToolbarStyleEnum {
    /**
     * BaseActivity的toolbar样式
     */
    NONE("none", "没有toolbar"),
    DEFAULT("default", "默认其实就是基础布局为约束布局"),
    COORDINATOR("coordinator", "基础布局为协调布局"),
    CONSTRAINT("constraint", "基础布局为约束布局");

    private ToolbarStyleEnum(String style, String describe) {
        this.style = style;
        this.describe = describe;
    }

    public final String style;
    public final String describe;

    /**
     * 判断style的设置是有有效
     */
    public static boolean isAvailable(String style) {
        if (TextUtils.isEmpty(style)) {
            return false;
        }
        boolean isAvailable = false;
        for (ToolbarStyleEnum value : values()) {
            if (value.style.equals(style)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }
}

