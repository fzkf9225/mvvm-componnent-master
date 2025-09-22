package com.casic.otitan.common.enums;

/**
 * created by fz on 2025/8/6 11:11
 * describe:
 */
public enum UploadStatusEnum {
    /**
     * 默认状态，也就是无任何状态的情况下
     */
    DEFAULT(0),
    /**
     * 上传中
     */
    UPLOADING(1),
    /**
     * 上传成功
     */
    SUCCESS(2),
    /**
     * 上传失败
     */
    FAILURE(3),
    /**
     * 取消
     */
    CANCELED(4);

    public final int typeValue;

    UploadStatusEnum(int typeValue) {
        this.typeValue = typeValue;
    }

    /**
     * 根据类型名称获取对应的枚举值
     *
     * @param typeValue 类型名称
     * @return 对应的枚举值，默认返回IMAGE
     */
    public static UploadStatusEnum getInfo(Integer typeValue) {
        if (typeValue == null) {
            return DEFAULT;
        }
        for (UploadStatusEnum type : values()) {
            if (type.typeValue == typeValue) {
                return type;
            }
        }
        return DEFAULT;
    }

}
