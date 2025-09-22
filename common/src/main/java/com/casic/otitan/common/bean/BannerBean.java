package com.casic.otitan.common.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by fz on 2017/6/14.
 * 首页banner图
 */

public class BannerBean extends BaseObservable {
    /**
     * 图片地址
     */
    @Bindable
    private Object bannerUrl;
    /**
     * 点击图片跳转的页面地址
     */
    @Bindable
    private String linkUrl;

    /**
     * 是否在app内部预览
     */
    @Bindable
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

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public Object getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(Object bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public boolean isLinkInside() {
        return linkInside;
    }

    public void setLinkInside(boolean linkInside) {
        this.linkInside = linkInside;
    }
}
