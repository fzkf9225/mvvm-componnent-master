package com.casic.titan.demo.bean;

import pers.fz.mvvm.bean.base.BasePagingBean;

/**
 * Created by fz on 2023/12/1 15:28
 * describe :
 */
public class ForestBean extends BasePagingBean {
    private String chineseName;
    private String createUser;
    private String maintainClassName;
    private String maintenanceTypeName;
    private String caretaker;
    private String createDate;
    private String conservationMeasure;

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getMaintainClassName() {
        return maintainClassName;
    }

    public void setMaintainClassName(String maintainClassName) {
        this.maintainClassName = maintainClassName;
    }

    public String getMaintenanceTypeName() {
        return maintenanceTypeName;
    }

    public void setMaintenanceTypeName(String maintenanceTypeName) {
        this.maintenanceTypeName = maintenanceTypeName;
    }

    public String getCaretaker() {
        return caretaker;
    }

    public void setCaretaker(String caretaker) {
        this.caretaker = caretaker;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getConservationMeasure() {
        return conservationMeasure;
    }

    public void setConservationMeasure(String conservationMeasure) {
        this.conservationMeasure = conservationMeasure;
    }
}
