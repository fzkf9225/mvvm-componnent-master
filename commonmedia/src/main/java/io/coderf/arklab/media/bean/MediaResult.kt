package io.coderf.arklab.media.bean

import io.coderf.arklab.media.enums.MediaTypeEnum

/**
 * 媒体操作统一结果：选择 / 压缩 / 水印。
 * <p>
 * 与历史 {@code mutableLiveData} 等并存，便于逐步迁移到单路观察。
 */
sealed class MediaResult(open val mediaBean: MediaBean) {

    /** 选择、拍照、拍摄、文件选择等 */
    data class Pick(override val mediaBean: MediaBean) : MediaResult(mediaBean)

    /** 压缩结果 */
    data class Compress(override val mediaBean: MediaBean) : MediaResult(mediaBean)

    /** 水印结果 */
    data class WaterMark(override val mediaBean: MediaBean) : MediaResult(mediaBean)

    val mediaType: MediaTypeEnum
        get() = mediaBean.mediaType

    val isEmpty: Boolean
        get() = mediaBean.mediaList.isEmpty()
}
