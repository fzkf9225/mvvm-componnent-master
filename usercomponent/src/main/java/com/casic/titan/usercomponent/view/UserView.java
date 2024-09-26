package com.casic.titan.usercomponent.view;


import pers.fz.mvvm.base.BaseView;

/**
 * Create by CherishTang on 2020/3/27 0027
 * describe:
 */
public interface UserView extends BaseView {
    /**
     * 直接返回上个页面
     */
    void toLast();

    /**
     * 是否有需要跳转的目标页面
     * @return true有目标页面
     */
    boolean hasTarget();

    /**
     * 跳转到目标页面，结合hasTarget使用
     */
    void toTarget();

    /**
     * 跳转到主页
     */
    void toMain();

    /**
     * 关闭键盘
     */
    void hideKeyboard();
}
