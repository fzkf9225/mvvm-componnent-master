package com.casic.titan.demo.bean;

import pers.fz.annotation.annotation.VerifyEntity;
import pers.fz.annotation.annotation.VerifyParams;
import pers.fz.annotation.enums.VerifyType;

/**
 * created by fz on 2024/8/12 17:01
 * describe:
 */
@VerifyEntity(sort = true)
public class Family {
    @VerifyParams(type = VerifyType.NOTNULL,  errorMsg = "您填填写您的妻子！")
    private String wife;

    @VerifyParams(type = VerifyType.NOTNULL,  errorMsg = "您填填写您的丈夫！")
    private String husband;
    private String son;
    private String daughter;

    public Family(String wife, String husband) {
        this.wife = wife;
        this.husband = husband;
    }

    public Family() {
    }

    public String getWife() {
        return wife;
    }

    public void setWife(String wife) {
        this.wife = wife;
    }

    public String getHusband() {
        return husband;
    }

    public void setHusband(String husband) {
        this.husband = husband;
    }

    public String getSon() {
        return son;
    }

    public void setSon(String son) {
        this.son = son;
    }

    public String getDaughter() {
        return daughter;
    }

    public void setDaughter(String daughter) {
        this.daughter = daughter;
    }

    @Override
    public String toString() {
        return "Family{" +
                "wife='" + wife + '\'' +
                ", husband='" + husband + '\'' +
                ", son='" + son + '\'' +
                ", daughter='" + daughter + '\'' +
                '}';
    }
}

