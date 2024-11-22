package pers.fz.mvvm.bean.base;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by fz on 2024/11/22.
 *
 */
public class PageBean<T> implements Serializable {
    @SerializedName(value = "data", alternate = {"list","records"})
    private List<T> data;
    private String nextPageToken;
    private String prevPageToken;
    private int requestCount;
    private int responseCount;
    private int rowCount;

    public List<T> getList() {
        return data;
    }

    public void setList(List<T> list) {
        this.data = list;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
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