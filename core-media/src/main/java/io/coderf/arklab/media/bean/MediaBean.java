package io.coderf.arklab.media.bean;

import android.net.Uri;

import java.util.List;

import io.coderf.arklab.media.enums.MediaTypeEnum;

/**
 * 一次选择/压缩/水印的结果载体，经 LiveData 或 {@link MediaResult} 分发。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2021/2/5 15:48
 * @updated 2026/10/28
 */
public class MediaBean {
    /** 选中或处理后的 Uri 列表 */
    private List<Uri> mediaList;
    /**
     * 媒体类型，见 {@link MediaTypeEnum}
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
