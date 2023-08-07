package pers.fz.mvvm.bean.Code;

/**
 * Created by CherishTang on 2018/5/25.
 * app版本检测
 */

public class VersionMessage {
    private int VersionCode;
    private Boolean isMustUpdate;
    private String VersionName;
    private String AppUrl;
    private String Content;
    private String Status;
    private String AddUser;
    private String AddTime;
    private Integer SeqId;

    public Boolean getMustUpdate() {
        return isMustUpdate;
    }

    public void setMustUpdate(Boolean mustUpdate) {
        isMustUpdate = mustUpdate;
    }

    public int getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(int versionCode) {
        VersionCode = versionCode;
    }

    public String getVersionName() {
        return VersionName;
    }

    public void setVersionName(String versionName) {
        VersionName = versionName;
    }

    public String getAppUrl() {
        return AppUrl;
    }

    public void setAppUrl(String appUrl) {
        AppUrl = appUrl;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getAddUser() {
        return AddUser;
    }

    public void setAddUser(String addUser) {
        AddUser = addUser;
    }

    public String getAddTime() {
        return AddTime;
    }

    public void setAddTime(String addTime) {
        AddTime = addTime;
    }

    public Integer getSeqId() {
        return SeqId;
    }

    public void setSeqId(Integer seqId) {
        SeqId = seqId;
    }
}
