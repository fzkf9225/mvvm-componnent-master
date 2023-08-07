package pers.fz.mvvm.util.media;

import android.net.Uri;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by fz on 2021/2/5 15:48
 * describe:媒体工具类
 */
public class MediaBean {
    private List<Uri> mediaList;
    /**
     * 0-图片，1-视频
     */
    private int mediaType;

    public MediaBean() {
    }

    public MediaBean(List<Uri> mediaList, int mediaType) {
        this.mediaList = mediaList;
        this.mediaType = mediaType;
    }

    public List<Uri> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Uri> mediaList) {
        this.mediaList = mediaList;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "MediaBean{" +
                "mediaList=" + new Gson().toJson(mediaList) +
                ", mediaType=" + mediaType +
                '}';
    }
}
