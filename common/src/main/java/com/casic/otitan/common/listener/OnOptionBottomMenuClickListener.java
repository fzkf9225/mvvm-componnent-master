package com.casic.otitan.common.listener;

import android.app.Dialog;

import java.util.List;

import com.casic.otitan.common.bean.PopupWindowBean;


/**
 * Created by CherishTang on 2019/10/31.
 * 监听事件
 */
public interface OnOptionBottomMenuClickListener<T extends PopupWindowBean> {
    void onOptionBottomMenuClick(Dialog dialog, List<T> list, int pos);
}
