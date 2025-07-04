package pers.fz.mvvm.bean;

import java.util.List;

/**
 * created by fz on 2025/7/4 16:05
 * describe:
 */
public class PopupMultiBean<T extends PopupWindowBean> {
    private boolean isMulti;
    private String name;
    private List<T> dataList;

    public boolean isMulti() {
        return isMulti;
    }

    public void setMulti(boolean multi) {
        isMulti = multi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }
}

