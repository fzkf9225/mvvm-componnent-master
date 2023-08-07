package pers.fz.mvvm.wight.picDialog.bean;



/**
 * 圈子：九宫格图片展示，Image封装
 * Created by xiaoke on 2016/5/9.
 */
public class ImageInfo {

    private Object url;
    private int width;
    private int height;

    public ImageInfo(Object url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public Object getUrl() {
        return url;
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {

        return "image---->>url=" + url + "width=" + width + "height" + height;
    }
}
