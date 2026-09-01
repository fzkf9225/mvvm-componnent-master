package io.coderf.arklab.common.bean;

import androidx.databinding.BaseObservable;
import androidx.fragment.app.Fragment;

/**
 * PagerInfo 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/2/12 9:17
 */
public class PagerInfo extends BaseObservable {
    /**
     * tab上的title
     */
    private String title;
    /**
     * 页面page路由
     */
    private Fragment toFragment;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public Fragment getToFragment() {
        return toFragment;
    }

    public void setToFragment(Fragment toFragment) {
        this.toFragment = toFragment;
    }
}

