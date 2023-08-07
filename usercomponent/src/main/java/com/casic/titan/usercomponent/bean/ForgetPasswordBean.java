package com.casic.titan.usercomponent.bean;

/**
 * Create by CherishTang on 2019/10/21 0021
 * describe:忘记密码
 */
public class ForgetPasswordBean {
    private String userName;
    private String idCardNo;
    private String mobile;
    private String newPassword;

    public ForgetPasswordBean(String userName, String idCardNo, String mobile, String newPassword) {
        this.userName = userName;
        this.idCardNo = idCardNo;
        this.mobile = mobile;
        this.newPassword = newPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
