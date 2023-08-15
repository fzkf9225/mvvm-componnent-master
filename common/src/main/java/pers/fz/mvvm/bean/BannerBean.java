package pers.fz.mvvm.bean;


import androidx.annotation.DrawableRes;

/**
 * Created by fz on 2017/6/14.
 * 首页banner图
 */

public class BannerBean {

    private long modifyTime;
    private Object path;//图片地址
    private String imgDiscribe;//图片描述
    private String labelImage;//图片文字地址
    private String linkPath;//点击图片跳转的页面地址

    public BannerBean(Object path) {
        this.path = path;
    }

    public BannerBean(Object path, String linkPath) {
        this.path = path;
        this.linkPath = linkPath;
    }

    public BannerBean(Object path, String labelImage, String linkPath) {
        this.path = path;
        this.labelImage = labelImage;
        this.linkPath = linkPath;
    }

    public BannerBean() {
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }

    public String getImgDiscribe() {
        return imgDiscribe;
    }

    public void setImgDiscribe(String imgDiscribe) {
        this.imgDiscribe = imgDiscribe;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Object getPath() {
        return path;
    }

    public void setPath(Object path) {
        this.path = path;
    }

    public String getLabelImage() {
        return labelImage;
    }

    public void setLabelImage(String labelImage) {
        this.labelImage = labelImage;
    }
}
