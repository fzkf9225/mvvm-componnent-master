package com.casic.otitan.commonui.enums;

import android.text.TextUtils;

/**
 * Created by fz on 2024/2/28 10:46
 * describe :表单中label文字对齐方式，默认为左侧
 */
public enum TextAlignEnum {
    /**
     * 正文文字在左侧
     */
    LEFT("left", 0),
    /**
     * 正文文字在右侧
     */
    RIGHT("right", 1),
    ;
    public final String align;
    public final int value;

    TextAlignEnum(String align, int value) {
        this.align = align;
        this.value = value;
    }

    public static int getValue(String align){
        if(TextUtils.isEmpty(align)){
            return LEFT.value;
        }
        for (TextAlignEnum value : values()){
            if(value.align.equalsIgnoreCase(align)){
                return value.value;
            }
        }
        return LEFT.value;
    }

    public static String getAlign(int value){
        for (TextAlignEnum alignEnum : values()){
            if(alignEnum.value == value){
                return alignEnum.align;
            }
        }
        return LEFT.align;
    }
}
