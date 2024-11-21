package pers.fz.mvvm.bean;

import androidx.databinding.BaseObservable;

import java.util.List;


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
}
