package io.coderf.arklab.base.enums

import android.text.TextUtils

/**
 * 通用附件/媒体类型 code（与多数 Blade 风格后端约定一致；可按项目调整）。
 */
enum class FileTypeEnum(
    val type: String,
    val describe: String
) {
    IMAGE("001", "图片"),
    VIDEO("002", "视频"),
    AUDIO("003", "音频"),
    OTHER("004", "其他"),
    ;

    companion object {
        @JvmStatic
        fun getDescribe(type: String?): String {
            if (type == null) return ""
            for (item in entries) {
                if (item.type == type) return item.describe
            }
            return ""
        }

        @JvmStatic
        fun isMedia(type: String?): Boolean {
            if (TextUtils.isEmpty(type)) return false
            return type == IMAGE.type || type == VIDEO.type || type == AUDIO.type
        }
    }
}
