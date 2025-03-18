package com.casic.titan.commonui.enums;

import android.text.TextUtils;

/**
 * Created by fz on 2024/2/28 10:46
 * describe :表单中label文字对齐方式，默认为左侧
 */
public enum LabelAlignEnum {
    /**
     * label文字在左侧
     */
    LEFT("left", 0),    /**
     * 命名规则，实体类（或表名）_字段名
     */
    TOP("top", 1),
    ;
    public final String align;
    public final int value;

    LabelAlignEnum(String align, int value) {
        this.align = align;
        this.value = value;
    }

    public static int getValue(String align){
        if(TextUtils.isEmpty(align)){
            return LEFT.value;
        }
        for (LabelAlignEnum value : values()){
            if(value.align.equalsIgnoreCase(align)){
                return value.value;
            }
        }
        return LEFT.value;
    }

    public static String getAlign(int value){
        for (LabelAlignEnum alignEnum : values()){
            if(alignEnum.value == value){
                return alignEnum.align;
            }
        }
        return LEFT.align;
    }
}
