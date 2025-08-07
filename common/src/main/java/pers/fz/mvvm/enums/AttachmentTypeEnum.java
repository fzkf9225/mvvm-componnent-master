package pers.fz.mvvm.enums;

import android.text.TextUtils;

/**
 * created by fz on 2025/8/6 11:11
 * describe:
 */
public enum AttachmentTypeEnum {
    /**
     * 图片
     */
    IMAGE("image"),
    /**
     * 视频
     */
    VIDEO("video"),
    /**
     * 音频
     */
    AUDIO("audio"),
    /**
     * 附件文件
     */
    FILE("file");

    public final String typeValue;

    AttachmentTypeEnum(String typeValue) {
        this.typeValue = typeValue;
    }

    /**
     * 根据类型名称获取对应的枚举值
     * @param typeValue 类型名称
     * @return 对应的枚举值，默认返回IMAGE
     */
    public static AttachmentTypeEnum getMediaType(String typeValue) {
        if (TextUtils.isEmpty(typeValue)) {
            return FILE;
        }
        for (AttachmentTypeEnum type : values()) {
            if (type.typeValue.equalsIgnoreCase(typeValue)) {
                return type;
            }
        }
        return FILE;
    }

    /**
     * 判断当前类型是否为图片
     */
    public boolean isImage() {
        return this == IMAGE;
    }

    /**
     * 判断当前类型是否为视频
     */
    public boolean isVideo() {
        return this == VIDEO;
    }

    /**
     * 判断当前类型是否为音频
     */
    public boolean isAudio() {
        return this == AUDIO;
    }

    /**
     * 判断当前类型是否为文件
     */
    public boolean isFile() {
        return this == FILE;
    }
}
