package pers.fz.mvvm.bean;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.List;


/**
 * Created by CherishTang on 2018/7/11.
 * popupwindow
 */

public class PopupWindowBean<T> extends BaseObservable {
    private String id;
    private String name;

    private Boolean isSelected;
    private Boolean isSingleSelected;
    private List<T> childList;

    public PopupWindowBean() {
    }

    public PopupWindowBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PopupWindowBean(String id, String name, boolean isSingleSelected) {
        this.id = id;
        this.name = name;
        this.isSingleSelected = isSingleSelected;
    }
    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Bindable
    public boolean isSingleSelected() {
        return isSingleSelected;
    }

    public void setSingleSelected(boolean singleSelected) {
        isSingleSelected = singleSelected;
    }
    @Bindable
    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
    @Bindable
    public Boolean getSingleSelected() {
        return isSingleSelected;
    }

    public void setSingleSelected(Boolean singleSelected) {
        isSingleSelected = singleSelected;
    }
    @Bindable
    public List<T> getChildList() {
        return childList;
    }

    public void setChildList(List<T> childList) {
        this.childList = childList;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
