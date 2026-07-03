package io.coderf.arklab.ui.enums;

/**
 * 输入框文本格式化类型，对应 {@code FormUI_formatText}。
 *
 * @author fz
 */
public enum FormTextFormatEnum {
    /** 不格式化（默认） */
    NORMAL(0),
    /** 整数 */
    INTEGER(1),
    /** 小数 */
    DOUBLE(2),
    /** 两位小数 */
    DOUBLE_00(3),
    /** 浮点数 */
    FLOAT(4),
    /** 长整数 */
    LONG(5);

    public final int value;

    FormTextFormatEnum(int value) {
        this.value = value;
    }

    public static FormTextFormatEnum fromValue(int value) {
        for (FormTextFormatEnum item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return NORMAL;
    }
}
