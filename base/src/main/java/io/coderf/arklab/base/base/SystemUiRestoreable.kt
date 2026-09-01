package io.coderf.arklab.base.base

/**
 * 视频等场景进入沉浸式全屏后，由播放器回调以恢复 Activity 原有状态栏/系统 UI 样式。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
interface SystemUiRestoreable {
    fun restoreSystemUiAfterFullscreen()
}
