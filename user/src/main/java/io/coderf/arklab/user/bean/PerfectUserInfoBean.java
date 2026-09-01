package io.coderf.arklab.user.bean;

/**
 * Create by fz on 2019/10/18 0018
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
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
