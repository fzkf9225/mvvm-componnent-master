package pers.fz.mvvm.listener;

import android.app.Dialog;

import java.util.List;

import pers.fz.mvvm.bean.PopupWindowBean;


/**
 * Created by CherishTang on 2019/10/31.
 * 监听事件
 */
public interface OnOptionBottomMenuClickListener<T extends PopupWindowBean> {
    void onOptionBottomMenuClick(Dialog dialog, List<T> list, int pos);
}
