package com.casic.otitan.media.enums;

/**
 * created by fz on 2025/8/5 11:38
 * describe: 视频质量等级
 */
public enum VideoQualityEnum {
    /**
     * 低质量
     */
    LOW("low", 3),
    /**
     * 中等质量
     */
    MEDIUM("medium", 2),
    /**
     * 高质量
     */
    HIGH("high", 1);

    public final String describe;
    public final int value;

    VideoQualityEnum(String describe, int value) {
        this.describe = describe;
        this.value = value;
    }

    public static int getValue(String describe) {
        if (describe == null) {
            return HIGH.value;
        }
        for (VideoQualityEnum value : values()) {
            if (value.describe.equalsIgnoreCase(describe)) {
                return value.value;
            }
        }
        return HIGH.value;
    }

    public static String getDescribe(int value) {
        for (VideoQualityEnum qualityEnum : values()) {
            if (qualityEnum.value == value) {
                return qualityEnum.describe;
            }
        }
        return HIGH.describe;
    }

    public static VideoQualityEnum getInfo(int value) {
        for (VideoQualityEnum qualityEnum : values()) {
            if (qualityEnum.value == value) {
                return qualityEnum;
            }
        }
        return HIGH;
    }
}
