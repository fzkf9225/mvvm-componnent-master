package com.casic.otitan.common.bean.base;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by fz on 2024/11/22.
 *
 */
public class PageBean<T> implements Parcelable {
    @SerializedName(value = "data", alternate = {"list","records"})
    private List<T> data;
    private String nextPageToken;
    private String prevPageToken;
    private int requestCount;
    private int responseCount;
    /**
     * 返回的总条数
     */
    private long total;

    public PageBean() {
    }

    protected PageBean(Parcel in) {
        nextPageToken = in.readString();
        prevPageToken = in.readString();
        requestCount = in.readInt();
        responseCount = in.readInt();
        total = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nextPageToken);
        dest.writeString(prevPageToken);
        dest.writeInt(requestCount);
        dest.writeInt(responseCount);
        dest.writeLong(total);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PageBean> CREATOR = new Creator<>() {
        @Override
        public PageBean createFromParcel(Parcel in) {
            return new PageBean(in);
        }

        @Override
        public PageBean[] newArray(int size) {
            return new PageBean[size];
        }
    };

    public List<T> getList() {
        return data;
    }

    public void setList(List<T> list) {
        this.data = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getPrevPageToken() {
        return prevPageToken;
    }

    public void setPrevPageToken(String prevPageToken) {
        this.prevPageToken = prevPageToken;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }
}