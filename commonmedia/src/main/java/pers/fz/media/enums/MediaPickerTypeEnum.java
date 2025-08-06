package pers.fz.media.enums;

/**
 * created by fz on 2025/8/5 11:39
 * describe:媒体选择器类型
 */
public enum MediaPickerTypeEnum {
    /**
     * 默认的选择器
     */
    DEFAULT("default", 0),
    /**
     * 新版Pick选择器
     */
    PICK("pick", 1);

    public final String describe;
    public final int value;

    MediaPickerTypeEnum(String describe, int value) {
        this.describe = describe;
        this.value = value;
    }

    public static int getValue(String describe) {
        if (describe == null) {
            return DEFAULT.value;
        }
        for (MediaPickerTypeEnum value : values()) {
            if (value.describe.equalsIgnoreCase(describe)) {
                return value.value;
            }
        }
        return DEFAULT.value;
    }

    public static String getDescribe(int value) {
        for (MediaPickerTypeEnum typeEnum : values()) {
            if (typeEnum.value == value) {
                return typeEnum.describe;
            }
        }
        return DEFAULT.describe;
    }
}
