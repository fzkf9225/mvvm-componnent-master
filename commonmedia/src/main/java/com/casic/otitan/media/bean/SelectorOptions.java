package com.casic.otitan.media.bean;

import com.casic.otitan.media.enums.MediaTypeEnum;

/**
 * created by fz on 2025/8/7 9:20
 * describe:
 */
public class SelectorOptions {
    private String[] type;
    private MediaTypeEnum mediaTypeEnum;

    public SelectorOptions(String[] type, MediaTypeEnum mediaTypeEnum) {
        this.type = type;
        this.mediaTypeEnum = mediaTypeEnum;
    }

    public SelectorOptions() {
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public MediaTypeEnum getMediaTypeEnum() {
        return mediaTypeEnum;
    }

    public void setMediaTypeEnum(MediaTypeEnum mediaTypeEnum) {
        this.mediaTypeEnum = mediaTypeEnum;
    }
}

