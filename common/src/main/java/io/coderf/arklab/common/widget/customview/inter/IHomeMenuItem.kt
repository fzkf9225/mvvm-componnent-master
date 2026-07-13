package io.coderf.arklab.common.widget.customview.inter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * 首页九宫格菜单项数据契约，与 {@link io.coderf.arklab.common.bean.HomeMenuBean} 解耦。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:05
 */
interface IHomeMenuItem {
    val id: Int

    @get:DrawableRes
    val icon: Int

    val title: String

    @get:ColorInt
    val labelColor: Int?

    val labelSize: Float?

    val iconWidth: Float?

    val iconHeight: Float?

    val iconTextMargin: Float?

    val isGray: Boolean?
}
