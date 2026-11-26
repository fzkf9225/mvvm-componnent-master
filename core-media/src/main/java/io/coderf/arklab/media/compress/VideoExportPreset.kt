package io.coderf.arklab.media.compress

/**
 * 视频导出档位：分辨率长边 + 目标码率（非旧 w×h×系数公式）。
 *
 * 兼容 core-form `compress_video` attr：high=1 / medium=2 / low=3。
 *
 * @property attrValue 与 FormUI `compress_video` 枚举值一致，供 XML 属性解析
 * @property maxLongSidePx 输出长边上限（像素）
 * @property targetBitrateBps 视频目标码率（bps）量级，真机可再标定
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:47
 */
enum class VideoExportPreset(
    val attrValue: Int,
    val maxLongSidePx: Int,
    val targetBitrateBps: Int,
) {
    /** 低清：长边约 720，约 1.2 Mbps */
    LOW(attrValue = 3, maxLongSidePx = 720, targetBitrateBps = 1_200_000),

    /** 中清（默认）：长边约 1280，约 2.5 Mbps */
    MEDIUM(attrValue = 2, maxLongSidePx = 1280, targetBitrateBps = 2_500_000),

    /** 高清：长边约 1920，约 6 Mbps */
    HIGH(attrValue = 1, maxLongSidePx = 1920, targetBitrateBps = 6_000_000);

    companion object {
        /**
         * 由 FormUI / 样式属性整型值解析档位。
         *
         * @param value attr 枚举值（1=HIGH，2=MEDIUM，3=LOW）
         * @return 匹配档位；未知值回退 [MEDIUM]
         */
        @JvmStatic
        fun fromAttrValue(value: Int): VideoExportPreset {
            return entries.firstOrNull { it.attrValue == value } ?: MEDIUM
        }
    }
}
