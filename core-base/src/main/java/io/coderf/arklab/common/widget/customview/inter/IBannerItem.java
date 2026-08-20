package io.coderf.arklab.common.widget.customview.inter;

/**
 * Banner 轮播数据契约，与具体 Bean 解耦，便于业务自定义数据模型接入 {@link io.coderf.arklab.common.widget.customview.BannerView}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:05
 */
public interface IBannerItem {

    /**
     * 图片资源：支持 URL 字符串、Uri、Drawable ResId 等。
     */
    Object getBannerUrl();

    /**
     * 点击跳转链接；为空则不跳转。
     */
    String getLinkUrl();

    /**
     * 是否在 App 内 WebView 打开链接。
     */
    boolean isLinkInside();
}
