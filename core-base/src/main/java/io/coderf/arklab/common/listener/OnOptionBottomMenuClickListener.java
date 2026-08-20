package io.coderf.arklab.common.listener;

import android.app.Dialog;

import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;


/**
 * Created by fz on 2019/10/31.
 * 监听事件
 */
public interface OnOptionBottomMenuClickListener<T extends PopupWindowBean> {
    void onOptionBottomMenuClick(Dialog dialog, List<T> list, int pos);
}
