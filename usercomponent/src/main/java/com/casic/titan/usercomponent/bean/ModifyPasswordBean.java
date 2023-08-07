package com.casic.titan.usercomponent.bean;

/**
 * Create by CherishTang on 2019/10/21 0021
 * describe:
 */
public class ModifyPasswordBean {
    private String oldPassword;
    private String newPassword;

    public ModifyPasswordBean(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public ModifyPasswordBean(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
