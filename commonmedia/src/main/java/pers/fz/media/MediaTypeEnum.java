package pers.fz.media;

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
    FILE(0);
    private final int mediaType;

    MediaTypeEnum(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }
}
