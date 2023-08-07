package pers.fz.mvvm.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.List;

/**
 * Created by fz on 2018/1/26.
 * 反馈
 */
public class FeedBackBean extends BaseObservable {
    public  String feedbackType;
    public  String problemDescribe;
    public  String linkman;
    public  String linkway;
    public List<String> urlBase64;
    private String ipAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        ipAddress = ipAddress;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        feedbackType = feedbackType;
    }

    @Bindable
    public String getProblemDescribe() {
        return problemDescribe;
    }

    public void setProblemDescribe(String problemDescribe) {
        problemDescribe = problemDescribe;
    }

    @Bindable
    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        linkman = linkman;
    }

    @Bindable
    public String getLinkway() {
        return linkway;
    }

    public void setLinkway(String linkway) {
        linkway = linkway;
    }

    public List<String> getUrlBase64() {
        return urlBase64;
    }

    public void setUrlBase64(List<String> urlBase64) {
        urlBase64 = urlBase64;
    }
}
