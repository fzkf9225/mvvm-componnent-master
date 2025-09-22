package com.casic.otitan.commonui.enums;

import android.text.TextUtils;

/**
 * Created by fz on 2025/6/5 14:25
 * describe :label文字是否加粗
 */
public enum LabelTextStyleEnum {
    /**
     * 正常
     */
    NORMAL("normal", 0),
    /**
     * 加粗
     */
    BOLD("bold", 1),
    ;
    public final String describe;
    public final int value;

    LabelTextStyleEnum(String describe, int value) {
        this.describe = describe;
        this.value = value;
    }

    public static int getValue(String describe){
        if(TextUtils.isEmpty(describe)){
            return NORMAL.value;
        }
        for (LabelTextStyleEnum value : values()){
            if(value.describe.equalsIgnoreCase(describe)){
                return value.value;
            }
        }
        return NORMAL.value;
    }

    public static String getDescribe(int value){
        for (LabelTextStyleEnum alignEnum : values()){
            if(alignEnum.value == value){
                return alignEnum.describe;
            }
        }
        return NORMAL.describe;
    }
}
