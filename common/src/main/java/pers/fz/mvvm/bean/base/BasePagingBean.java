package pers.fz.mvvm.bean.base;

import androidx.databinding.BaseObservable;

/**
 * created by fz on 2024/10/14 13:14
 * describe:
 */
public class BasePagingBean extends BaseObservable {
    private String id;

    public BasePagingBean(String id) {
        this.id = id;
    }

    public BasePagingBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

