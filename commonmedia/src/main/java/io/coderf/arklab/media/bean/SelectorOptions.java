package io.coderf.arklab.media.bean;

import io.coderf.arklab.media.enums.MediaTypeEnum;

/**
 * SelectorOptions 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/7 9:20
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

