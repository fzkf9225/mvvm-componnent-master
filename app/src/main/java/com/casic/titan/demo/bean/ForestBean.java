package com.casic.titan.demo.bean;

import androidx.databinding.BaseObservable;

/**
 * Created by fz on 2023/12/1 15:28
 * describe :
 */
public class ForestBean extends BaseObservable {
    private String id;
    private String certificate;
    private String landHaveRight;
    private String registeDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getLandHaveRight() {
        return landHaveRight;
    }

    public void setLandHaveRight(String landHaveRight) {
        this.landHaveRight = landHaveRight;
    }

    public String getRegisteDate() {
        return registeDate;
    }

    public void setRegisteDate(String registeDate) {
        this.registeDate = registeDate;
    }
}
