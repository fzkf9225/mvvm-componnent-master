package pers.fz.mvvm.listener;

import android.app.Dialog;

import pers.fz.mvvm.bean.PopupWindowBean;

import java.util.List;


/**
 * Created by CherishTang on 2019/10/31.
 * 监听事件
 */
public interface OnOptionBottomMenuClickListener<T extends PopupWindowBean> {
    void onOptionBottomMenuClick(Dialog dialog, List<T> list, int pos);
}
