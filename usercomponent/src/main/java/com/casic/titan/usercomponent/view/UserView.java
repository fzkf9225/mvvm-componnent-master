package com.casic.titan.usercomponent.view;

import androidx.annotation.ColorRes;

import pers.fz.mvvm.base.BaseView;

/**
 * Create by CherishTang on 2020/3/27 0027
 * describe:
 */
public interface UserView extends BaseView {
    void loginSuccess();
    void hideKeyboard();
    void showMainActivity();
}
