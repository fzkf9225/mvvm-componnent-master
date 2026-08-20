package io.coderf.arklab.common.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import io.coderf.arklab.common.widget.customview.inter.IBannerItem;

/**
 * 首页 Banner 数据模型，实现 {@link IBannerItem} 以接入 {@link io.coderf.arklab.common.widget.customview.BannerView}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/6/14 00:00
 */
public class BannerBean extends BaseObservable implements IBannerItem {
    /**
     * 图片地址
     */
    private Object bannerUrl;
    /**
     * 点击图片跳转的页面地址
     */
    private String linkUrl;

    /**
     * 是否在app内部预览
     */
    private boolean linkInside = false;

    public BannerBean(Object bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public BannerBean(Object bannerUrl, String linkUrl) {
        this.bannerUrl = bannerUrl;
        this.linkUrl = linkUrl;
    }

    public BannerBean(Object bannerUrl, String linkUrl, boolean linkInside) {
        this.bannerUrl = bannerUrl;
        this.linkUrl = linkUrl;
        this.linkInside = linkInside;
    }

    public BannerBean() {

    }
    @Bindable
    @Override
    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @Bindable
    @Override
    public Object getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(Object bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    @Bindable
    @Override
    public boolean isLinkInside() {
        return linkInside;
    }

    public void setLinkInside(boolean linkInside) {
        this.linkInside = linkInside;
    }
}
