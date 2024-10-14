package pers.fz.mvvm.bean;

/**
 * Created by fz on 2017/6/14.
 * 首页banner图
 */

public class BannerBean {

    private String id;
    /**
     * 图片地址
     */
    private Object path;
    /**
     * 点击图片跳转的页面地址
     */
    private String linkPath;

    /**
     * 是否在app内部预览
     */
    private boolean linkInside = false;

    public BannerBean(Object path) {
        this.path = path;
    }

    public BannerBean(String id, Object path) {
        this.id = id;
        this.path = path;
    }

    public BannerBean(Object path, String linkPath) {
        this.path = path;
        this.linkPath = linkPath;
    }

    public BannerBean(String id, Object path, String linkPath) {
        this.id = id;
        this.path = path;
        this.linkPath = linkPath;
    }

    public BannerBean(Object path, String linkPath, boolean linkInside) {
        this.path = path;
        this.linkPath = linkPath;
        this.linkInside = linkInside;
    }

    public BannerBean() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }


    public Object getPath() {
        return path;
    }

    public void setPath(Object path) {
        this.path = path;
    }

    public boolean isLinkInside() {
        return linkInside;
    }

    public void setLinkInside(boolean linkInside) {
        this.linkInside = linkInside;
    }
}
