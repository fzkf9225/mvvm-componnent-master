package pers.fz.mvvm.bean;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;

import java.util.List;

import pers.fz.mvvm.util.common.CollectionUtil;


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
     * 名称
     */
    private String popupName;
    /**
     * id
     */
    private String parentPopupId;
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

    public PopupWindowBean(String popupId, String popupName, boolean isSingleSelected) {
        this.popupId = popupId;
        this.popupName = popupName;
        this.isSingleSelected = isSingleSelected;
    }

    public PopupWindowBean(String popupId, String popupName, List<T> childList) {
        this.popupId = popupId;
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

}
