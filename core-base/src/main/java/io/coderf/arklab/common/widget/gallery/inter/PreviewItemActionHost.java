package io.coderf.arklab.common.widget.gallery.inter;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

/**
 * 大图预览 item 右下角操作区（下载 / 查看全部）的对外契约。
 * <p>
 * Dialog 实现本接口，{@link io.coderf.arklab.common.widget.gallery.PreviewItemActionBinder}
 * 在任意 item 布局上绑定；后续新增 item 布局只需 include
 * {@code include_preview_item_actions} 并调用 Binder，无需改多处业务。
 *
 * @author fz
 * @since 1.0
 * @created 2026/11/26
 */
public interface PreviewItemActionHost {

    /** 是否显示下载按钮 */
    boolean isDownloadActionVisible();

    /**
     * 点击下载是否先弹确认框。
     * {@code false}（默认）直接下载；{@code true} 走 ImageSaveDialog。
     */
    boolean isDownloadConfirmEnabled();

    /** 当前是否允许保存（总开关，关闭则下载按钮无效） */
    boolean canSaveCurrentMedia();

    /**
     * 下载当前页媒体。
     *
     * @param adapterPosition ViewPager 数据下标
     */
    void onDownloadAction(int adapterPosition);

    /** 是否显示「查看全部」 */
    boolean isViewAllActionVisible();

    /**
     * 点击「查看全部」。
     *
     * @param adapterPosition 当前页下标
     */
    void onViewAllAction(int adapterPosition);

    /** 自定义「查看全部」图标，null 用布局默认 */
    @Nullable
    default Drawable getViewAllActionIcon() {
        return null;
    }

    /** 自定义下载图标，null 用布局默认 */
    @Nullable
    default Drawable getDownloadActionIcon() {
        return null;
    }

    /**
     * 操作按钮边长（px），null 或 &lt;=0 表示保持布局默认（30dp）。
     */
    @Nullable
    default Integer getActionIconSizePx() {
        return null;
    }

    /** 是否给操作按钮加圆形底，默认 true */
    default boolean isActionCircleBackgroundEnabled() {
        return true;
    }

    /** 自定义圆形底，null 用 {@code bg_preview_nav_circle} */
    @Nullable
    default Drawable getActionCircleBackground() {
        return null;
    }

    /**
     * 额外底部 inset（如系统导航栏高度 px），加到操作区 marginBottom 上。
     * 默认 0。
     */
    default int getActionBottomInsetPx() {
        return 0;
    }
}
