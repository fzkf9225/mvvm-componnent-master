package io.coderf.arklab.common.widget.video;

/**
 * 播放器宿主场景：嵌入 View、独立 Activity、Dialog 小窗。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public enum VideoPlayerHostMode {
    /** 嵌入 Activity / Fragment，非全屏仅展示全屏按钮 */
    EMBED,
    /** 独立 Activity，默认横屏并展示完整工具栏 */
    ACTIVITY,
    /** Dialog 小窗，默认仅全屏按钮，展开后展示完整工具栏 */
    DIALOG
}
