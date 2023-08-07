package pers.fz.mvvm.util.media;

/**
 * Created by fz on 2019/9/2.
 * describe：媒体类型
 */
public enum MediaTypeEnum {
    /**
     * 首页菜单
     */
    IMAGE(0),
    VIDEO(1);
    private final int mediaType;

    MediaTypeEnum(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getMediaType() {
        return mediaType;
    }
}
