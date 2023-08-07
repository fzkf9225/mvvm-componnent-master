package com.casic.titan.usercomponent.bean;

/**
 * Create by CherishTang on 2019/10/18 0018
 * describe:
 */
public class PerfectUserInfoBean {
    private String idCardNo;
    private String userName;

    public PerfectUserInfoBean(String idCardNo, String userName) {
        this.idCardNo = idCardNo;
        this.userName = userName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
