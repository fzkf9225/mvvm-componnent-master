package io.coderf.arklab.media.bean;

import io.coderf.arklab.media.enums.MediaTypeEnum;

/**
 * 传统选择器（GetContent / GetMultipleContents）入参：MIME 与业务媒体类型。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/7 9:20
 * @updated 2026/10/28
 */
public class SelectorOptions {
    /** MIME，如 {@code image/*}、{@code video/*}；可多值 */
    private String[] type;
    /** 业务侧类型，用于结果回调归类 */
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

