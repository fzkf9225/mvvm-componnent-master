package io.coderf.arklab.common.bean.base;

import androidx.databinding.BaseObservable;

/**
 * BasePagingBean 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/10/14 13:14
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

