package pers.fz.media;

import android.net.Uri;

import java.util.List;

import pers.fz.media.enums.MediaTypeEnum;

/**
 * Created by fz on 2021/2/5 15:48
 * describe:媒体工具类
 */
public class MediaBean {
    private List<Uri> mediaList;
    /**
     * 0-图片，1-视频，具体值参考枚举MediaType
     */
    private MediaTypeEnum mediaType;

    public MediaBean() {
    }

    public MediaBean(List<Uri> mediaList, MediaTypeEnum mediaType) {
        this.mediaList = mediaList;
        this.mediaType = mediaType;
    }

    public List<Uri> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Uri> mediaList) {
        this.mediaList = mediaList;
    }

    public MediaTypeEnum getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaTypeEnum mediaType) {
        this.mediaType = mediaType;
    }

}
