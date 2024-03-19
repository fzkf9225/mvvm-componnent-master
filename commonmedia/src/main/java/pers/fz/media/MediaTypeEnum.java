package pers.fz.media;

/**
 * Created by fz on 2019/9/2.
 * describe：媒体类型
 */
public enum MediaTypeEnum {
    /**
     * 选择文件
     */
    IMAGE(1),
    VIDEO(2),
    AUDIO(3),
    FILE(0);
    private final int mediaType;

    MediaTypeEnum(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }
}
