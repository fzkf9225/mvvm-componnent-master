package com.casic.otitan.common.bean;

import androidx.databinding.BaseObservable;
import androidx.fragment.app.Fragment;

/**
 * created by fz on 2025/2/12 9:17
 * describe:
 */
public class PagerInfo extends BaseObservable {
    /**
     * tab上的title
     */
    private final String title;
    /**
     * 页面page路由
     */
    private final Fragment toFragment;

    public PagerInfo(String title, Fragment toFragment) {
        this.title = title;
        this.toFragment = toFragment;
    }

    public String getTitle() {
        return title;
    }

    public Fragment getFragment() {
        return toFragment;
    }
}

