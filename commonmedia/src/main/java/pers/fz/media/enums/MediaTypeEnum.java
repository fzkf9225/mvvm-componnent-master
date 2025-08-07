package pers.fz.media.enums;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by fz on 2023/9/2.
 * describe：媒体类型
 */
public enum MediaTypeEnum {
    /*
     * 图片
     */
    IMAGE(1),
    /*
     * 视频
     */
    VIDEO(2),
    /*
     * 音频
     */
    AUDIO(3),
    /*
     * 文件、包含所有可选类型的文件，具体那些可选会根据系统api有一定的区别
     */
    FILE(0),
    /*
     * 未知类型
     */
    OTHER(4),
    /*
     * 图片和视频混合一起的
     */
    IMAGE_AND_VIDEO(5);

    private final int mediaType;

    MediaTypeEnum(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }

    /**
     * 根据文件类型、文件地址获取文件类型
     *
     * @param context 上下文
     * @param uri     uri地址
     * @return 文件类型枚举
     */
    public static MediaTypeEnum getMediaType(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (context == null || context.getContentResolver() == null) {
            return null;
        }
        String type = context.getContentResolver().getType(uri);
        if (!TextUtils.isEmpty(type) && (type.startsWith("image") || type.startsWith("IMAGE"))) {
            return MediaTypeEnum.IMAGE;
        } else if ((!TextUtils.isEmpty(type)) && (type.startsWith("video") || type.startsWith("VIDEO"))) {
            return MediaTypeEnum.VIDEO;
        } else {
            return MediaTypeEnum.FILE;
        }
    }

    public static MediaTypeEnum getMediaType(Integer type) {
        if (type == null) {
            return OTHER;
        }
        for (MediaTypeEnum value : values()) {
            if (value.mediaType == type) {
                return value;
            }
        }
        return OTHER;
    }
}
