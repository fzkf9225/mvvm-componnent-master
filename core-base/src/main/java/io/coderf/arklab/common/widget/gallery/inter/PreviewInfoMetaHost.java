package io.coderf.arklab.common.widget.gallery.inter;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.widget.gallery.bean.PreviewInfoBean;
import io.coderf.arklab.common.widget.gallery.config.PreviewInfoConfig;

/**
 * 信息大图预览 item 元数据区（定位图标 + 底部三行文案）契约。
 * <p>
 * {@link io.coderf.arklab.common.widget.gallery.PreviewInfoPhotoDialog} 实现；
 * {@link io.coderf.arklab.common.widget.gallery.PreviewInfoMetaBinder} 在 item 上绑定。
 *
 * @author fz
 * @since 1.0
 * @created 2026/11/26
 */
public interface PreviewInfoMetaHost {

    @NonNull
    PreviewInfoConfig getPreviewInfoConfig();

    /** 未选中定位图标，null 用默认矢量 */
    @Nullable
    Drawable getLocationNormalIcon();

    /** 选中态定位图标，null 用默认/染色逻辑由实现方提供 */
    @Nullable
    Drawable getLocationSelectedIcon();

    /**
     * 定位图标额外 top inset（如状态栏高度 px）。
     */
    default int getMetaTopInsetPx() {
        return 0;
    }

    /**
     * 底部信息区额外 bottom inset（如导航栏高度 px）。
     */
    default int getMetaBottomInsetPx() {
        return 0;
    }

    /**
     * 定位图标点击（可选中切换后的 selected 已写入条目）。
     */
    void onLocationIconClick(@Nullable PreviewInfoBean item, int position, boolean selected);
}
