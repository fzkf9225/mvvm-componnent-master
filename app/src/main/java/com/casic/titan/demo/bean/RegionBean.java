package com.casic.titan.demo.bean;

import java.util.List;

import pers.fz.mvvm.bean.base.BasePagingBean;

/**
 * Created by fz on 2023/12/1 15:28
 * describe :
 */
public class RegionBean extends BasePagingBean {
    private String areaCode;
    private String areaName;
    private String areaNameAll;
    private Integer grade;
    private String orderNo;
    private String parentAreaCode;
    private String superiorName;
    private String uneffectDate;
    private List<RegionBean> children;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaNameAll() {
        return areaNameAll;
    }

    public void setAreaNameAll(String areaNameAll) {
        this.areaNameAll = areaNameAll;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getParentAreaCode() {
        return parentAreaCode;
    }

    public void setParentAreaCode(String parentAreaCode) {
        this.parentAreaCode = parentAreaCode;
    }

    public String getSuperiorName() {
        return superiorName;
    }

    public void setSuperiorName(String superiorName) {
        this.superiorName = superiorName;
    }

    public String getUneffectDate() {
        return uneffectDate;
    }

    public void setUneffectDate(String uneffectDate) {
        this.uneffectDate = uneffectDate;
    }

    public List<RegionBean> getChildren() {
        return children;
    }

    public void setChildren(List<RegionBean> children) {
        this.children = children;
    }
}
