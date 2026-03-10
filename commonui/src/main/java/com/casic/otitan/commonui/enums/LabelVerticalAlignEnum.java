package com.casic.otitan.commonui.enums;

import android.text.TextUtils;

/**
 * 表单中label文字对齐方式，当对齐方式为左侧时，他的垂直方向对齐方式
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/10 20:07
 */
public enum LabelVerticalAlignEnum {
    /**
     * 顶部对齐
     */
    TOP("top", 0),
    /**
     * 跟value文字顶部一样高，这是默认方式
     */
    TOP_TO_VALUE("topToValue", 1),
    /**
     * 居中对齐
     */
    CENTER("center", 2),
    /**
     * 底部对齐
     */
    BOTTOM("bottom", 3),
    ;
    public final String align;
    public final int value;

    LabelVerticalAlignEnum(String align, int value) {
        this.align = align;
        this.value = value;
    }

    public static int getValue(String align){
        if(TextUtils.isEmpty(align)){
            return TOP_TO_VALUE.value;
        }
        for (LabelVerticalAlignEnum value : values()){
            if(value.align.equalsIgnoreCase(align)){
                return value.value;
            }
        }
        return TOP_TO_VALUE.value;
    }

    public static String getAlign(int value){
        for (LabelVerticalAlignEnum alignEnum : values()){
            if(alignEnum.value == value){
                return alignEnum.align;
            }
        }
        return TOP_TO_VALUE.align;
    }
}
