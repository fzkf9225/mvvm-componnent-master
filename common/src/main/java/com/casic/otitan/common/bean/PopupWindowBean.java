package com.casic.otitan.common.bean;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;

import java.util.List;

import com.casic.otitan.common.utils.common.CollectionUtil;


/**
 * updated by fz on 2024/11/21.
 * describe:弹框
 */
public class PopupWindowBean<T> extends BaseObservable {
    /**
     * id
     */
    private String popupId;
    /**
     * 编码
     */
    private String popupCode;
    /**
     * 名称
     */
    private String popupName;
    /**
     * id
     */
    private String parentPopupId;
    /**
     * 父编码
     */
    private String parentPopupCode;
    /**
     * 是否选中
     */
    private Boolean isSelected;
    /**
     * 是否单选
     */
    private Boolean isSingleSelected;
    /**
     * 子集
     */
    private List<T> childList;

    public PopupWindowBean() {
    }

    public PopupWindowBean(String popupName) {
        this.popupName = popupName;
    }

    public PopupWindowBean(String popupId, String popupName) {
        this.popupId = popupId;
        this.popupName = popupName;
    }

    public PopupWindowBean(String popupId, String popupCode, String popupName) {
        this.popupId = popupId;
        this.popupCode = popupCode;
        this.popupName = popupName;
    }

    public PopupWindowBean(String popupId, String popupName, boolean isSingleSelected) {
        this.popupId = popupId;
        this.popupName = popupName;
        this.isSingleSelected = isSingleSelected;
    }

    public PopupWindowBean(String popupId, String popupCode, String popupName, boolean isSingleSelected) {
        this.popupId = popupId;
        this.popupCode = popupCode;
        this.popupName = popupName;
        this.isSingleSelected = isSingleSelected;
    }

    public PopupWindowBean(String popupId, String popupName, List<T> childList) {
        this.popupId = popupId;
        this.popupName = popupName;
        this.childList = childList;
    }

    public PopupWindowBean(String popupId, String popupCode, String popupName, List<T> childList) {
        this.popupId = popupId;
        this.popupCode = popupCode;
        this.popupName = popupName;
        this.childList = childList;
    }

    public String getPopupId() {
        return popupId;
    }

    public void setPopupId(String popupId) {
        this.popupId = popupId;
    }

    public String getPopupName() {
        return popupName;
    }

    public void setPopupName(String popupName) {
        this.popupName = popupName;
    }

    public String getParentPopupId() {
        return parentPopupId;
    }

    public void setParentPopupId(String parentPopupId) {
        this.parentPopupId = parentPopupId;
    }

    public String getPopupCode() {
        return popupCode;
    }

    public void setPopupCode(String popupCode) {
        this.popupCode = popupCode;
    }

    public String getParentPopupCode() {
        return parentPopupCode;
    }

    public void setParentPopupCode(String parentPopupCode) {
        this.parentPopupCode = parentPopupCode;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public Boolean getSingleSelected() {
        return isSingleSelected;
    }

    public void setSingleSelected(Boolean singleSelected) {
        isSingleSelected = singleSelected;
    }

    public List<T> getChildList() {
        return childList;
    }

    public void setChildList(List<T> childList) {
        this.childList = childList;
    }

    public static <T extends PopupWindowBean<?>> boolean containsById(List<T> list, String popupId) {
        if (CollectionUtil.isEmpty(list) || TextUtils.isEmpty(popupId)) {
            return false;
        }
        return list.stream().anyMatch(item -> item.getPopupId() != null && item.getPopupId().equals(popupId));
    }
    public static <T extends PopupWindowBean<?>> boolean containsByCode(List<T> list, String popupCode) {
        if (CollectionUtil.isEmpty(list) || TextUtils.isEmpty(popupCode)) {
            return false;
        }
        return list.stream().anyMatch(item -> item.getPopupCode() != null && item.getPopupId().equals(popupCode));
    }
}
